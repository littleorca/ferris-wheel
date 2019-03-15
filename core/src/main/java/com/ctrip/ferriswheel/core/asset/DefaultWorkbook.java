package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.*;
import com.ctrip.ferriswheel.common.action.ActionContextManager;
import com.ctrip.ferriswheel.common.action.ActionListener;
import com.ctrip.ferriswheel.common.automaton.AsynchronousAutomaton;
import com.ctrip.ferriswheel.common.automaton.Automaton;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.ChartBinder;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Row;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.text.Text;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.bean.TextData;
import com.ctrip.ferriswheel.core.formula.*;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluator;
import com.ctrip.ferriswheel.core.formula.eval.ReferenceResolver;
import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.ref.RangeRef;
import com.ctrip.ferriswheel.core.util.GraphHelper;
import com.ctrip.ferriswheel.core.util.UUIDGen;
import com.ctrip.ferriswheel.core.util.UnmodifiableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class DefaultWorkbook extends NamedAssetNode implements Workbook, ReferenceResolver {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultWorkbook.class);
    private static final VersionImpl VERSION = new VersionImpl(0, 0, 0);
    // revision sequence number is used to mark last update (like last modified timestamp)
    private final Environment environment;
    private final AtomicLong nextSequenceNumber = new AtomicLong(0);
    private final NamedAssetList<DefaultSheet> sheets;
    private final ActionListenerChain listenerChain;
    private final Set<Long> evaluableNodes = new HashSet<>();
    private final AutoFiller autoFiller = new AutoFiller(this);
    private final ActionContextManager actionContextManager = new DefaultActionContextManager();

    private static final int WORKER_THREADS_PER_WORKBOOK = 4;
    private static final long WAIT_PERIOD_IN_MILLI_SECONDS = 5 * 1000L;
    private static final long REFRESH_TIMEOUT_IN_MILLI_SECONDS = 15 * 60 * 1000L;

    DefaultWorkbook(Environment environment) {
        // FIXME workbook name
        super(UUIDGen.generate().toString(), new DefaultAssetManager());
        getAssetManager().employ(this); // self-register
        this.environment = environment;
        this.sheets = new NamedAssetList<>(this);
        this.listenerChain = createListenerChain();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public VersionImpl getVersion() {
        return VERSION;
    }

    @Override
    public int getSheetCount() {
        return sheets.size();
    }

    @Override
    public DefaultSheet getSheet(int index) {
        return sheets.get(index);
    }

    @Override
    public DefaultSheet getSheet(String name) {
        return sheets.get(name);
    }

    @Override
    public DefaultSheet addSheet(String name) {
        checkSheetName(name);
        return listenerChain.publicly(new AddSheet(name, sheets.size()), () -> {
            DefaultSheet sheet = createSheet(name);
            sheets.add(sheet);
            return sheet;
        });
    }

    @Override
    public DefaultSheet addSheet(int index, String name) {
        checkSheetName(name);
        return listenerChain.publicly(new AddSheet(name, index), () -> {
            DefaultSheet sheet = createSheet(name);
            sheets.add(index, sheet);
            return sheet;
        });
    }

    private DefaultSheet createSheet(String name) {
        return new DefaultSheet(name, getAssetManager(), listenerChain);
    }

    @Override
    public void renameSheet(String oldName, String newName) {
        DefaultSheet sheet = getSheet(oldName);
        if (sheet == null) {
            throw new IllegalArgumentException("Invalid sheet name: " + oldName);
        }
        if (newName.equals(sheet.getName())) {
            return; // nothing changed
        }
        checkSheetName(newName);
        withoutRefresh(() -> listenerChain.publicly(new RenameSheet(sheet.getName(), newName),
                () -> {
                    if (!sheets.rename(oldName, newName)) {
                        throw new RuntimeException("Failed to rename sheet.");
                    }
                    for (SheetAsset asset : sheet) {
                        if (asset instanceof DefaultTable) {
                            DefaultTable table = (DefaultTable) asset;
                            updateRangeReferences(table,
                                    0,
                                    0,
                                    table.getColumnCount(),
                                    table.getRowCount(),
                                    null,
                                    null,
                                    null,
                                    null);
                        }
                    }
                }));

        // technically rename a sheet doesn't affect any cell value or chart property value,
        // it just affect formulas.
        refreshIfNeeded();
    }

    @Override
    public void moveSheet(String name, int index) {
        DefaultSheet sheet = getSheet(name);
        if (sheet == null) {
            throw new IllegalArgumentException("Invalid sheet name: " + name);
        }
        listenerChain.publicly(new MoveSheet(name, index), () -> {
            sheets.remove(sheet.getName());
            sheets.add(index, sheet);
        });
    }

    @Override
    public DefaultSheet removeSheet(int index) {
        return doRemoveSheet(getSheet(index));
    }

    @Override
    public DefaultSheet removeSheet(String name) {
        return doRemoveSheet(getSheet(name));
    }

    private DefaultSheet doRemoveSheet(DefaultSheet sheet) {
        if (sheet == null) {
            return null;
        }
        return listenerChain.publicly(new RemoveSheet(sheet.getName()), () -> {
            sheets.remove(sheet.getName());
            return sheet;
        });
    }

    /**
     * A batch ops manipulate workbook without settle formula references down
     * immediately. It is useful in some scenarios like filling data in
     * arbitrary order which may put a formula with cell references that
     * not resolvable yet.
     *
     * @param consumer
     * @param forceRefresh
     */
    void batch(Consumer<Workbook> consumer, boolean forceRefresh) {
        actionContextManager.withContext(new DefaultActionContext(true, isSkipRefresh(), isForceRefresh()),
                this, consumer);
        resettle();
        refresh(forceRefresh);
    }

    /**
     * Scan and update reference anchor.
     * TODO currently dependencies also been update during this procedure, need to split them.
     */
    void resettle() {
        LOG.debug("resettle");

        withoutRefresh(() -> {
            for (Sheet sheet : this) {
                DefaultSheet defaultSheet = (DefaultSheet) sheet;
                for (SheetAsset asset : defaultSheet) {
                    if (asset instanceof DefaultTable) {
                        resolveFormulas((DefaultTable) asset);
                    } else if (asset instanceof DefaultChart) {
                        resolveFormulas((DefaultChart) asset);
                    } else if (asset instanceof DefaultText) {
                        resolveFormulas((DefaultText) asset);
                    }
                }
            }
        });
    }

    private void resolveFormulas(DefaultTable table) {
        if (table.getAutomaton() != null) {
            Automaton auto = table.getAutomaton();
            if (auto instanceof DefaultPivotAutomaton) {
                onValueNodeUpdate(table.getSheet(), ((DefaultPivotAutomaton) auto).getData());
            } else if (auto instanceof DefaultQueryAutomaton) {
                DefaultQueryTemplate template = ((DefaultQueryAutomaton) auto).getTemplate();
                for (String name : template.getBuiltinParamNames()) {
                    ValueNode param = template.getBuiltinParam(name);
                    onValueNodeUpdate(table.getSheet(), param);
                }
            }

        } else {
            for (Map.Entry<Integer, Row> rowEntry : table) {
                for (Map.Entry<Integer, Cell> cellEntry : rowEntry.getValue()) {
                    onValueNodeUpdate(table, (DefaultCell) cellEntry.getValue());
                }
            }
        }
    }

    private void resolveFormulas(DefaultChart chart) {
        DefaultSheet sheet = chart.getSheet();
        if (chart.getBinder() != null) {
            onValueNodeUpdate(sheet, chart.getBinder().getData());
        }
        onValueNodeUpdate(sheet, chart.getTitle());
        onValueNodeUpdate(sheet, chart.getCategories());
        for (int j = 0; j < chart.getSeriesCount(); j++) {
            DefaultDataSeries series = chart.getSeries(j);
            if (series.getName() != null) {
                onValueNodeUpdate(sheet, series.getName());
            }
            if (series.getxValues() != null) {
                onValueNodeUpdate(sheet, series.getxValues());
            }
            if (series.getyValues() != null) {
                onValueNodeUpdate(sheet, series.getyValues());
            }
        }
    }

    private void resolveFormulas(DefaultText text) {
        DefaultSheet sheet = text.getSheet();
        onValueNodeUpdate(sheet, text.getContent());
    }

    @Override
    public void refresh() {
        refresh(false);
    }

    @Override
    public void refresh(boolean force) {
        actionContextManager.withContext(new DefaultActionContext(isSkipWelding(), isSkipRefresh(), force),
                () -> refresh0());
    }

    /**
     * Re-build dependency graph
     */
    void refresh0() {
        LOG.debug("refresh0");
        if (isForceRefresh()) {
            //rebuildDependencies();
        }
        refresh1();
    }

    /**
     * Re-calculate
     */
    void refresh1() {
        LOG.debug("refresh1");

        FormulaEvaluator evaluator = new FormulaEvaluator(this);
        Set<Long> remainedTasks = new HashSet<>(evaluableNodes); // may includes some nodes not in the graph
        DirectedAcyclicGraph<Long, Asset> graph = GraphHelper.buildGraph(this);

        ExecutorService executor = Executors.newFixedThreadPool(WORKER_THREADS_PER_WORKBOOK);
        Set<Long> pendingTasks = new HashSet<>();

        try {
            CompletionService<Long> completionService = new ExecutorCompletionService<>(executor);
            long start = System.currentTimeMillis();

            /**
             * The while-loop will break on exception and thus the whole process will be failed.
             * As one task hang up forever can stop the whole process, this behavior is good to
             * prevent the whole process from hanging forever. However, this can be improved to
             * execute as more tasks as possible by checking there dependency relationships.
             */
            while (!graph.isEmpty()) {
                Set<Long> tasks = graph.collectOutboundEnds();
                boolean processedAnyTask = false;
                for (Long id : tasks) {
                    if (pendingTasks.contains(id)) {
                        continue;
                    }
                    processedAnyTask = true;
                    AssetNode assetNode = (AssetNode) getAsset(id);
                    if (evaluateAssetNode(evaluator, assetNode, completionService)) {
                        graph.removeNode(id);
                        remainedTasks.remove(id);
                    } else {
                        pendingTasks.add(id);
                    }
                }

                // check if any pending task has been done.
                Future<Long> future;
                if (!processedAnyTask && !pendingTasks.isEmpty()) {
                    // calculation blocks on pending tasks, so let's poll with waiting
                    future = completionService.poll(WAIT_PERIOD_IN_MILLI_SECONDS, TimeUnit.MILLISECONDS);
                } else {
                    // calculation can be continued despite possible pending tasks, just poll without waiting.
                    future = completionService.poll();
                }
                if (future != null) {
                    Long id = future.get();
                    graph.removeNode(id);
                    remainedTasks.remove(id);
                    pendingTasks.remove(id);
                }

                if (System.currentTimeMillis() - start >= REFRESH_TIMEOUT_IN_MILLI_SECONDS) {
                    throw new RuntimeException("Refresh procedure timed out.");
                }
            }

            // now graph is empty, deal with remainedTasks (nodes that not occurred in the graph)
            for (Long id : remainedTasks) {
                if (!evaluateAssetNode(evaluator, (AssetNode) getAsset(id), completionService)) {
                    pendingTasks.add(id);
                }
            }
            while (!pendingTasks.isEmpty()) {
                Future<Long> future = completionService.poll(WAIT_PERIOD_IN_MILLI_SECONDS, TimeUnit.MILLISECONDS);
                if (future != null) {
                    pendingTasks.remove(future.get());
                }
                if (System.currentTimeMillis() - start >= REFRESH_TIMEOUT_IN_MILLI_SECONDS) {
                    throw new RuntimeException("Refresh procedure timed out.");
                }
            }

        } catch (InterruptedException e) {
            LOG.warn("Refresh procedure interrupted.", e);
            executor.shutdownNow();
        } catch (ExecutionException e) {
            LOG.warn("Refresh procedure caught an exception while executing a task.", e);
            executor.shutdownNow();
        } catch (RuntimeException e) {
            LOG.warn("Refresh procedure caught an runtime exception.", e);
            executor.shutdownNow();
        } finally {
            executor.shutdown(); // it's ok to shutdown an executor that already shutdown.
        }

//        if (LOG.isDebugEnabled()) {
//            LOG.debug(toString());
//        }
    }

    private boolean evaluateAssetNode(FormulaEvaluator evaluator,
                                      AssetNode asset,
                                      CompletionService<Long> completionService) {
        if (asset instanceof ValueNode) {
            ValueNode node = (ValueNode) asset;
            if (node.isFormula()) {
                evaluateNodeFormula(evaluator, node);
            }

        } else if (asset instanceof Automaton) {
            if (asset instanceof AsynchronousAutomaton) {
                Future<Long> future = ((AsynchronousAutomaton) asset).execute(isForceRefresh(),
                        completionService, asset.getAssetId());
                if (future != null) {
                    return false;
                }
            } else {
                ((Automaton) asset).execute(isForceRefresh());
            }

        } else if (asset instanceof DefaultTable) {
            DefaultTable table = (DefaultTable) asset;
            if (table.getAutomaton() != null) {
                table.fillByAutomaton();
            }

        } else if (asset instanceof DefaultChart) {
            DefaultChart chart = (DefaultChart) asset;
            chart.rebindIfPossible();
        }

        return true;
    }

    @Override
    public void addListener(ActionListener listener) {
        listenerChain.addListener(listener);
    }

    @Override
    public boolean removeListener(ActionListener listener) {
        return listenerChain.removeListener(listener);
    }

    public void evaluateNodeFormula(FormulaEvaluator evaluator, ValueNode node) {
        if (node instanceof DefaultCell) {
            evaluateCellFormula(evaluator, (DefaultCell) node);
            return;
        }

        AssetNode parent = node.getParent();

        if (parent instanceof DefaultDataSeries) {
            evaluateChartProperty(evaluator, ((DefaultDataSeries) parent).getChart(), node);

        } else if (parent instanceof DefaultChart) {
            evaluateChartProperty(evaluator, (DefaultChart) parent, node);

        } else if (parent instanceof DefaultQueryTemplate) {
            evaluateQueryParam(evaluator, (DefaultQueryTemplate) parent, node);

        } else if (parent instanceof DefaultText) {
            evaluateText(evaluator, (DefaultText) parent, node);

        } else if (parent instanceof DefaultChartBinder) {
            ((DefaultChartBinder) parent).getChart().rebindIfPossible();

        } else if (parent instanceof DefaultPivotAutomaton) {
            long sn = node.getLastUpdateSequenceNumber();
            if (node.getDependencies() != null) {
                for (AssetNode dependencyNode : node.getDependencies()) {
                    long depSn = dependencyNode.getLastUpdateSequenceNumber();
                    if (depSn > sn) {
                        sn = depSn;
                    }
                }
            }
            if (sn != node.getLastUpdateSequenceNumber()) {
                node.setLastUpdateSequenceNumber(sn);
            }

        } else {
            throw new RuntimeException("Unsupported value node: " + node);
        }
    }

    private void evaluateCellFormula(FormulaEvaluator evaluator, DefaultCell cell) {
        if (cell.getFormulaElements() == null) {
            throw new IllegalArgumentException();
        }
        DefaultTable table = cell.getRow().getTable();
        evaluator.setCurrentTable(table);
        Variant value = evaluator.evaluate(cell.getFormulaElements());
        if (!value.equals(cell.getData())) {
            CellAction.RefreshCellValue action = new CellAction.RefreshCellValue(
                    table.getSheet().getName(),
                    table.getName(),
                    cell.getRowIndex(),
                    cell.getColumnIndex(),
                    Value.from(value));
            listenerChain.publicly(action, () -> cell.setValue(value));
        }
    }

    private void evaluateChartProperty(FormulaEvaluator evaluator, DefaultChart chart, ValueNode property) {
        if (property.getFormulaElements() == null) {
            throw new IllegalArgumentException();
        }
        DefaultSheet sheet = chart.getSheet();
        evaluator.setCurrentSheet(sheet);
        evaluator.setCurrentTable(null);
        Variant value = evaluator.evaluate(property.getFormulaElements());
        if (!value.equals(property.getData())) {
            UpdateChart action = new UpdateChart(sheet.getName(), chart.getName(), null);
            listenerChain.publicly(action, () -> {
                property.setValue(value);
                return chart;
            });
        }
    }

    private void evaluateQueryParam(FormulaEvaluator evaluator, DefaultQueryTemplate queryTemplate, ValueNode param) {
        if (param.getFormulaElements() == null) {
            throw new IllegalArgumentException();
        }
        DefaultQueryAutomaton automaton = (DefaultQueryAutomaton) queryTemplate.getParent();
        DefaultTable table = automaton.getTable();
        evaluator.setCurrentSheet(table.getSheet());
        evaluator.setCurrentTable(table);
        Variant value = evaluator.evaluate(param.getFormulaElements());
        if (!value.equals(Value.from(param.getData().getVariant()))) {
            param.setValue(value); // TODO notify this action
        }
    }

    private void evaluateText(FormulaEvaluator evaluator, DefaultText text, ValueNode node) {
        if (node.getFormulaElements() == null) {
            throw new IllegalArgumentException();
        }
        DefaultSheet sheet = text.getSheet();
        evaluator.setCurrentSheet(sheet);
        evaluator.setCurrentTable(null);
        Variant value = evaluator.evaluate(node.getFormulaElements());
        if (!value.equals(node.getData())) {
            UpdateText action = new UpdateText(sheet.getName(), text.getName(), null);
            listenerChain.publicly(action, () -> {
                node.setValue(value);
                action.setTextData(new TextData(text.getName(),
                        new DynamicValue(node.getFormulaString(), Value.from(node.getData())),
                        text.getLayout()));
            });
        }
    }

    protected void refreshIfNeeded() {
        if (!isSkipWelding() && !isSkipRefresh()) {
            refresh();
        }
    }

    boolean isSkipRefresh() {
        return actionContextManager.isSkipRefresh();
    }

    boolean isSkipWelding() {
        return actionContextManager.isSkipWelding();
    }

    boolean isForceRefresh() {
        return actionContextManager.isForceRefresh();
    }

    private void checkSheetName(String name) throws IllegalArgumentException {
        if (getSheet(name) != null) {
            throw new IllegalArgumentException("Duplicated sheet name!");
        }
    }

    Asset getAsset(long id) {
        return getAssetManager().get(id);
    }

    ValueNode getValueNodeByAssetId(long id) {
        Asset asset = getAssetManager().get(id);
        if (asset == null) {
            return null;
        }
        if (asset instanceof ValueNode) {
            return (ValueNode) asset;
        }
        throw new RuntimeException("Asset \"" + id
                + "\" that expected to be a ValueNode is actually: "
                + asset.getClass());
    }

    DefaultCell getCellByAssetId(long id) {
        Asset asset = getAssetManager().get(id);
        if (asset == null) {
            return null;
        }
        if (asset instanceof DefaultCell) {
            return (DefaultCell) asset;
        }
        throw new RuntimeException("Asset \"" + id
                + "\" that expected to be a DefaultCell is actually: "
                + asset.getClass());
    }

    DefaultSheet getReferredSheet(CellRef ref, DefaultSheet currentSheet) {
        if (ref.getSheetName() == null) {
            return currentSheet;
        } else {
            return getSheet(ref.getSheetName());
        }
    }

    DefaultTable getReferredTable(CellRef ref, DefaultTable currentTable) {
        return getReferredTable(ref,
                currentTable == null ? null : currentTable.getSheet(),
                currentTable);
    }

    DefaultTable getReferredTable(CellRef ref, DefaultSheet currentSheet, DefaultTable currentTable) {
        if (ref.getTableName() == null) {
            return currentTable;
        }
        DefaultSheet sheet = getReferredSheet(ref, currentSheet);
        return sheet == null ? null : sheet.getAsset(ref.getTableName());
    }

    DefaultCell getReferredCell(CellRef ref, DefaultTable currentTable) {
        DefaultTable table = getReferredTable(ref, currentTable);
        if (table == null) {
            return null;
        }
        return table.getCell(ref.getRowIndex(), ref.getColumnIndex());
    }

    CalcChain getCalcChain() {
        return GraphHelper.buildCalcChain(this);
    }

//    /**
//     * This method should be removed after the core workflow is robust.
//     */
//    void rebuildDependencies() {
//        dependencyTracer.clearDependencyGraph();
//        evaluableNodes.clear();
//        for (Sheet sheet : this) {
//            DefaultSheet defaultSheet = (DefaultSheet) sheet;
//            for (NamedAsset asset : defaultSheet) {
//                if (asset instanceof DefaultTable) {
//                    collectDependencies((DefaultTable) asset);
//                } else if (asset instanceof DefaultChart) {
//                    collectDependencies((DefaultChart) asset);
//                } else if (asset instanceof DefaultText) {
//                    collectDependencies((DefaultText) asset);
//                } else {
//                    throw new RuntimeException("Unknown asset: " + asset);
//                }
//            }
//        }
//    }

//    private void collectDependencies(DefaultTable table) {
//        if (table.getDependencies() != null) {
//            for (AssetNode dependencyNode : table.getDependencies()) {
//                dependencyTracer.addDependencies(table.getAssetId(), dependencyNode.getAssetId());
//            }
//        }
//        for (Row row : table) {
//            for (Cell cell : row) {
//                collectDependencies((DefaultCell) cell);
//            }
//        }
//        if (table.getAutomaton() != null) {
//            collectDependencies(table, table.getAutomaton());
//        }
//    }

//    private void collectDependencies(DefaultTable table, TableAutomaton automaton) {
//        evaluableNodes.add(table.getAssetId());
//        if (automaton instanceof DefaultQueryAutomaton) {
//            DefaultQueryTemplate template = ((DefaultQueryAutomaton) automaton).getTemplate();
//            for (String name : template.getBuiltinParamNames()) {
//                ValueNode param = template.getBuiltinParam(name);
//                collectDependencies(param);
//            }
//        } else if (automaton instanceof DefaultPivotAutomaton) {
//            collectDependencies(((DefaultPivotAutomaton) automaton).getData());
//        }
//    }
//
//    private void collectDependencies(DefaultChart chart) {
//        if (chart.getDependencies() != null) {
//            for (AssetNode dependencyNode : chart.getDependencies()) {
//                dependencyTracer.addDependencies(chart.getAssetId(), dependencyNode.getAssetId());
//            }
//        }
//        collectDependencies(chart.getTitle());
//        collectDependencies(chart.getCategories());
//        for (int j = 0; j < chart.getSeriesCount(); j++) {
//            DefaultDataSeries series = chart.getSeries(j);
//            collectDependencies(series.getName());
//            collectDependencies(series.getxValues());
//            collectDependencies(series.getyValues());
//        }
//    }
//
//    private void collectDependencies(DefaultText text) {
//        collectDependencies(text.getContent());
//    }

//    private void collectDependencies(ValueNode node) {
//        if (node == null) {
//            return;
//        }
//        evaluableNodes.add(node.getAssetId());
//        Set<? extends AssetNode> dependencies = node.getDependencies();
//        if (dependencies != null) {
//            for (AssetNode dependency : dependencies) {
//                dependencyTracer.addDependencies(node.getAssetId(), dependency.getAssetId());
//            }
//        }
//    }

    public DefaultAssetManager getAssetManager() {
        return (DefaultAssetManager) super.getAssetManager();
    }

    @Override
    public Variant resolve(SimpleReferenceElement referenceElement, FormulaEvaluationContext context) {
        CellRef cellRef = referenceElement.getCellRef();
        if (!cellRef.isValid()) {
            return Value.err(ErrorCodes.ILLEGAL_REF);
        }
        if (cellRef.getCellId() != Asset.UNSPECIFIED_ASSET_ID) {
            Asset asset = getAssetManager().get(cellRef.getCellId());
            if (asset == null || !(asset instanceof Cell)) {
                return Value.err(ErrorCodes.ILLEGAL_REF);
            }
            return ((DefaultCell) asset).getData();
        }

        DefaultCell cell = null;
        if (context.getCurrentTable() != null) {
            cell = getReferredCell(cellRef, (DefaultTable) context.getCurrentTable());
        } else {
            DefaultTable table = getReferredTable(cellRef,
                    (DefaultSheet) context.getCurrentSheet(),
                    (DefaultTable) context.getCurrentTable());
            if (table != null) {
                cell = table.getCell(cellRef.getRowIndex(), cellRef.getColumnIndex());
            }
        }
        if (cell == null) {
            return Value.err(ErrorCodes.ILLEGAL_REF); // cell not found
        }
        return cell.getData();
    }

    @Override
    public Table resolveTable(String sheetName, String tableName, FormulaEvaluationContext context) {
        if (sheetName == null && tableName == null) {
            return context.getCurrentTable();
        }
        Sheet sheet = (sheetName != null) ? getSheet(sheetName) : context.getCurrentSheet();
        if (sheet == null) {
            return null; // or throw new RuntimeException();
        }
        return sheet.getAsset(tableName);
    }

    //// ------------------------------------------------------------------------------------
    //// callbacks for update dependencies and refresh formula results after certain changes.
    //// ------------------------------------------------------------------------------------

    void onCellUpdate(DefaultCell cell) {
        onValueNodeUpdate(cell.getRow().getTable(), cell);
    }

    private void onValueNodeUpdate(DefaultSheet sheet, ValueNode valueNode) {
        onValueNodeUpdate(sheet, null, valueNode);
    }

    private void onValueNodeUpdate(DefaultTable table, ValueNode valueNode) {
        onValueNodeUpdate(table == null ? null : table.getSheet(), table, valueNode);
    }

    /**
     * Do not call this method, use {@link #onValueNodeUpdate(DefaultSheet, ValueNode)}
     * or {@link #onValueNodeUpdate(DefaultTable, ValueNode)} instead.
     * <p>
     * This method resolve formula and set reference anchor (target asset ID),
     * and also updates dependencies. MAYBE split those two step for convenience
     * to do ops like resettle without update dependencies(refer {@link #resettle()}).
     *
     * @param sheet
     * @param table
     * @param valueNode
     */
    private void onValueNodeUpdate(DefaultSheet sheet, DefaultTable table, ValueNode valueNode) {
        if (isSkipWelding()) {
            return; // should manually resolve later
        }
        valueNode.clearDependencies();
//        dependencyTracer.clearRangeDependencies(valueNode.getAssetId());

        // TODO review if it is needed
//        if (table != null) {
//            valueNode.addDependency(table);
//        } else if (valueNode.getParentAsset() instanceof DefaultChart) {
//            valueNode.addDependency(valueNode.getParentAsset());
//        }

        evaluableNodes.remove(valueNode.getAssetId());

        if (valueNode.isFormula()) {
            evaluableNodes.add(valueNode.getAssetId());
            Formula f = valueNode.getFormula();
            for (FormulaElement e : f.getElements()) {
                if (e instanceof SimpleReferenceElement) {
                    CellRef cellRef = ((SimpleReferenceElement) e).getCellRef();
                    hookCellRef(sheet, table, valueNode, cellRef);
                    traceRange(sheet, table, valueNode, cellRef);

                } else if (e instanceof RangeReferenceElement) {
                    RangeRef rangeRef = ((RangeReferenceElement) e).getRangeRef();
                    hookRangeRef(sheet, table, valueNode, rangeRef);
                    traceRange(sheet, table, valueNode, rangeRef);
                }
            }
        }

        refreshIfNeeded();
    }

    private boolean hookCellRef(DefaultSheet fromSheet, DefaultTable fromTable, ValueNode fromNode, CellRef cellRef) {
        if (!cellRef.isValid()) {
            return false;
        }
        DefaultTable table = getReferredTable(cellRef, fromSheet, fromTable);
        if (table == null) {
            throw new IllegalArgumentException();
        }
        DefaultCell depCell = table.getCell(cellRef.getRowIndex(), cellRef.getColumnIndex());
        cellRef.setCellId(depCell.getAssetId()); // fill runtime id for convenience.
        fromNode.addDependency(depCell);
        if (depCell.isEphemeral()) {
            fromNode.addDependency(depCell.getRow().getTable());
        }
        return true;
    }

    private boolean hookRangeRef(DefaultSheet fromSheet, DefaultTable fromTable, ValueNode fromNode, RangeRef rangeRef) {
        if (!rangeRef.isValid()) {
            return false;
        }
        DefaultTable table = getReferredTable(rangeRef.getUpperLeft(), fromSheet, fromTable);
        if (table == null) {
            rangeRef.getUpperLeft().setValid(false);
            return false;
//            throw new IllegalArgumentException();
        }
        // fill runtime id for convenience.
        final int left = rangeRef.getLeft() == -1 ? 0 : rangeRef.getLeft();
        final int top = rangeRef.getTop() == -1 ? 0 : rangeRef.getTop();
        final int right = rangeRef.getRight() != -1 ? rangeRef.getRight() :
                table.getColumnCount() > 0 ? table.getColumnCount() - 1 : 0;
        final int bottom = rangeRef.getBottom() != -1 ? rangeRef.getBottom() :
                table.getRowCount() > 0 ? table.getRowCount() - 1 : 0;
        DefaultCell upperLeft = table.getCell(top, left);
        DefaultCell lowerRight = table.getCell(bottom, right);
        if (upperLeft != null) {
            rangeRef.getUpperLeft().setCellId(upperLeft.getAssetId());
        }
        if (lowerRight != null) {
            rangeRef.getLowerRight().setCellId(lowerRight.getAssetId());
        }
        // scan dependencies
        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                DefaultCell depCell = table.getCell(row, col);
                if (depCell != null) {
                    fromNode.addDependency(depCell);
                    if (depCell.isEphemeral()) {
                        fromNode.addDependency(depCell.getRow().getTable());
                    }
                }
            }
        }
        return true;
    }

    private void traceRange(DefaultSheet fromSheet, DefaultTable fromTable, ValueNode fromNode, CellRef cellRef) {
        if (!cellRef.isValid()) {
            return;
        }
        DefaultTable referredTable = getReferredTable(cellRef, fromSheet, fromTable);
        fromNode.watchRange(referredTable,
                cellRef.getColumnIndex(),
                cellRef.getRowIndex(),
                cellRef.getColumnIndex(),
                cellRef.getRowIndex());
    }

    private void traceRange(DefaultSheet fromSheet, DefaultTable fromTable, ValueNode fromNode, RangeRef rangeRef) {
        DefaultTable targetTable = getReferredTable(rangeRef.getUpperLeft(), fromSheet, fromTable);

        if (targetTable == null) {
            return; // TODO is that ok?
        }

        if (rangeRef.getUpperLeft().isValid() && rangeRef.getLowerRight().isValid()) {
            fromNode.watchRange(targetTable,
                    rangeRef.getLeft(),
                    rangeRef.getTop(),
                    rangeRef.getRight(),
                    rangeRef.getBottom());

        } else if (rangeRef.getUpperLeft().isValid()) {
            fromNode.watchRange(targetTable,
                    rangeRef.getUpperLeft().getRowIndex(),
                    rangeRef.getUpperLeft().getColumnIndex());

        } else if (rangeRef.getLowerRight().isValid()) {
            fromNode.watchRange(targetTable,
                    rangeRef.getLowerRight().getRowIndex(),
                    rangeRef.getLowerRight().getColumnIndex());
        }
    }

    void onRowsInserted(Table table, int rowIndex, int nRows) {
        final int left = 0;
        final int top = rowIndex;
        final int right = table.getColumnCount() - 1;
        final int bottom = table.getRowCount() - 1;

        withoutRefresh(() -> {
            updateRangeReferences((DefaultTable) table, left, top, right, bottom,
                    null, null, null, null);
            autoFiller.autoFillRowsIfPossible(table, rowIndex, nRows);
        });

        refreshIfNeeded();
    }

    void onRowsErased(DefaultTable sheet, int rowIndex, int nRows) {
        refreshIfNeeded();
    }

    void onRowsRemoved(Table table, int rowIndex, int nRows, List<Row> removedRows) {
        final int left = 0;
        final int top = rowIndex;

        withoutRefresh(() ->
                updateRangeReferences((DefaultTable) table, left, top, null, null,
                        null, rowIndex > 0 ? rowIndex - 1 : null,
                        null, rowIndex));

        refreshIfNeeded();
    }

    void onColumnsInserted(Table table, int colIndex, int nCols) {
        final int left = colIndex;
        final int top = 0;
        final int right = table.getColumnCount() - 1;
        final int bottom = table.getRowCount() - 1;

        withoutRefresh(() -> {
            updateRangeReferences((DefaultTable) table, left, top, right, bottom,
                    null, null, null, null);
            autoFiller.autoFillColumnsIfPossible(table, colIndex, nCols);
        });

        refreshIfNeeded();
    }

    void onColumnsErased(Table table, int colIndex, int nCols) {
        refreshIfNeeded();
    }

    void onColumnsRemoved(Table table, int colIndex, int nCols, List<Cell> removedCells) {
        final int left = colIndex;
        final int top = 0;

        withoutRefresh(() -> updateRangeReferences((DefaultTable) table, left, top, null, null,
                colIndex > 0 ? colIndex - 1 : null, null,
                colIndex, null));

        refreshIfNeeded();
    }

    void onTableRemoved(DefaultTable table) {
        withoutRefresh(() -> {
            updateRangeReferences(table, 0, 0, null, null, null, null, null, null);
        });
        refreshIfNeeded();
    }

    /**
     * Find overlapped ranges and dependencies, then update there formulas.
     * If dependency has gone, or both anchor cells of range dependency have gone,
     * set error info to the value node.
     * If one of the anchor cell of range dependency has gone, try to shrink the referred
     * area.
     * <p>
     * Changed area specified by left/top/right/bottom, contains removed area and moved area.
     * <p>
     * If an area has been removed, alignLeft/alignTop/alignRight/alignBottom indicates which
     * row/column should a removed anchor relocated to.
     * <p>
     *
     * @param table       In which table area change happened.
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param alignLeft
     * @param alignTop
     * @param alignRight
     * @param alignBottom
     */
    void updateRangeReferences(DefaultTable table, int left, int top, Integer right, Integer bottom,
                               Integer alignLeft, Integer alignTop,
                               Integer alignRight, Integer alignBottom) {
        if (isSkipWelding()) {
            return; // should manually update later.
        }
        List<DefaultTable.Range> ranges = table.findOverlappedRanges(left, top, right, bottom);
        if (ranges == null || ranges.isEmpty()) {
            return; // nothing to do
        }
        Set<Long> nodes = new HashSet<>();
        for (DefaultTable.Range range : ranges) {
            nodes.addAll(table.getWatchers(range));
        }
        for (Long id : nodes) {
            VariantNode node = getValueNodeByAssetId(id);
            fixFormulaAfterAreaChanged(table, (ValueNode) node, alignLeft, alignTop, alignRight, alignBottom);
        }
    }

    /**
     * @param table       In which table, area change happened.
     * @param node
     * @param alignLeft
     * @param alignTop
     * @param alignRight
     * @param alignBottom
     */
    private void fixFormulaAfterAreaChanged(DefaultTable table, ValueNode node,
                                            Integer alignLeft, Integer alignTop,
                                            Integer alignRight, Integer alignBottom) {
        // update references and construct new formula string, then parse
        // the new formula string and set to the cell.
        // the formula elements keep reference to formula string for
        // convenience of shifting reference by just update reference
        // token, that's why the flow looks redundant and inefficient.
        // TODO review and optimize formula update process
        // probably this can be optimized by simplify token string
        // reference, just keep references refer to it's position in
        // the formula string, and update elements and formula string
        // could be done simultaneously without re-parse.

        boolean modified = false;

        // this loop check and update elements for construct new formula
        for (FormulaElement elem : node.getFormulaElements()) {
            if (!(elem instanceof ReferenceElement)) {
                continue;
            }
            if (elem instanceof SimpleReferenceElement) {
                CellRef cellRef = ((SimpleReferenceElement) elem).getCellRef();
                modified |= fixCellRef(cellRef, table.getAutomaton() != null);

            } else if (elem instanceof RangeReferenceElement) {
                RangeRef rangeRef = ((RangeReferenceElement) elem).getRangeRef();
                CellRef upperLeftRef = rangeRef.getUpperLeft();
                CellRef lowerRightRef = rangeRef.getLowerRight();
                modified |= fixCellRef(upperLeftRef, table.getAutomaton() != null);
                modified |= fixCellRef(lowerRightRef, table.getAutomaton() != null);

                if (!upperLeftRef.isValid() && !lowerRightRef.isValid()) {
                    // invalid range reference

                } else if (!upperLeftRef.isValid()) { // shrink to bottom/right
                    if (alignBottom != null) {
                        moveToAnotherInternalCell(rangeRef.getUpperLeft(),
                                table.getOrCreateCell(alignBottom,
                                        Math.max(rangeRef.getUpperLeft().getColumnIndex(), 0)));
                        modified = true;

                    } else if (alignRight != null) {
                        moveToAnotherInternalCell(rangeRef.getUpperLeft(),
                                table.getOrCreateCell(
                                        Math.max(rangeRef.getUpperLeft().getRowIndex(), 0),
                                        alignRight));
                        modified = true;

                    } else {
                        throw new RuntimeException("Failed to fix formula.");
                    }

                } else if (!lowerRightRef.isValid()) { // shrink to top/left
                    if (alignTop != null) {
                        int columnIndex = rangeRef.getLowerRight().getColumnIndex();
                        if (columnIndex == -1) {
                            columnIndex = table.getColumnCount() - 1;
                        }
                        if (columnIndex == -1) {
                            columnIndex = 0; // Default
                        }
                        moveToAnotherInternalCell(rangeRef.getLowerRight(),
                                table.getOrCreateCell(alignTop, columnIndex));
                        modified = true;

                    } else if (alignLeft != null) {
                        int rowIndex = rangeRef.getLowerRight().getRowIndex();
                        if (rowIndex == -1) {
                            rowIndex = table.getRowCount() - 1;
                        }
                        if (rowIndex == -1) {
                            rowIndex = 0; // Default
                        }
                        moveToAnotherInternalCell(rangeRef.getLowerRight(),
                                table.getOrCreateCell(rowIndex, alignLeft));
                        modified = true;

                    } else {
                        throw new RuntimeException("Failed to fix formula.");
                    }
                }

            } else {
                throw new RuntimeException("What? I don't recognize the reference: " + elem.getClass());
            }
        }

//        if (!modified) {
//            return; // FIXME 如果采用 table!A:D这样的公式，行数变化时公式并无变化，但引用的数据实际需要刷新的。
//        }

        // now lets construct new formula
        String newFormula = FormulaParser.toFormula(node.getFormulaElements(), 0, 0);
        if (node instanceof DefaultCell) {
            DefaultCell cell = (DefaultCell) node;
            cell.getRow().getTable().setCellFormula(cell.getRowIndex(), cell.getColumnIndex(), newFormula); // this will trigger onCellUpdate, some other businesses will be done there.

        } else if (node.getParent() instanceof Chart
                || node.getParent() instanceof DataSeries
                || node.getParent() instanceof ChartBinder) {
            // chart property update is not as easy as a cell, property of chart may be a little
            // difficult to trace as it doesn't has row/column index. a chart property can be
            // a categories formula, or one property of any series.
            // Of cause it's not hard to update chart property itself, however, what changes
            // should we notify the listeners (especially the revise collector)?
            // currently just tell listeners the chart has changed, either model or data.
            DefaultChart chart = (DefaultChart)
                    (((node.getParent() instanceof DataSeries)
                            || node.getParent() instanceof ChartBinder) ?
                            node.getParent().getParent() : node.getParent());
            UpdateChart action = new UpdateChart(chart.getSheet().getName(), chart.getName(), null);
            listenerChain.publicly(action, () -> {
                node.setFormula(new Formula(newFormula));
                onValueNodeUpdate(chart.getSheet(), node);
                return chart;
            });

        } else if (node.getParent() instanceof Text) {
            DefaultText text = (DefaultText) node.getParent();
            text.getSheet().updateText(text.getName(), new TextData(text.getName(), new DynamicValue(newFormula), null));

        } else if (node.getParent() instanceof DefaultQueryAutomaton) {
            node.setDynamicVariant(new DynamicValue(newFormula));
            DefaultQueryAutomaton auto = (DefaultQueryAutomaton) node.getParent();
            auto.getTable().automate(auto.getQueryAutomatonInfo());

        } else if (node.getParent() instanceof DefaultPivotAutomaton) {
            node.setDynamicVariant(new DynamicValue(newFormula));
            DefaultPivotAutomaton auto = (DefaultPivotAutomaton) node.getParent();
            auto.getTable().automate(auto.getPivotAutomatonInfo());

        } else {
            throw new RuntimeException("Unsupported value node: " + node);
        }
    }

    private void moveToAnotherInternalCell(CellRef cellRef, DefaultCell newCell) {
        cellRef.setCellId(newCell.getAssetId());
        if (cellRef.getRowIndex() != -1) {
            cellRef.setRowIndex(newCell.getRowIndex());
        }
        if (cellRef.getColumnIndex() != -1) {
            cellRef.setColumnIndex(newCell.getColumnIndex());
        }
        cellRef.setValid(true);
    }

    /**
     * Update row/column index of the reference by the cell's runtime ID. When a cell has moved,
     * use this method to keep the row/column index up to date.
     *
     * @param cellRef
     * @param stubborn Determine if reference should be kept untouched even it is invalid.
     * @return true if reference has been modified, false otherwise.
     */
    private boolean fixCellRef(CellRef cellRef, boolean stubborn) {
        if (!cellRef.isValid()) {
            return false;
        }
        DefaultCell referredCell = getCellByAssetId(cellRef.getCellId());
        if (referredCell == null) {
            if (!stubborn) {
                cellRef.setValid(false);
            } else {
                cellRef.setCellId(UNSPECIFIED_ASSET_ID);
            }
            return true;
        }

        boolean modified = false;

        DefaultTable table = referredCell.getRow().getTable();
        if (cellRef.getSheetName() != null && !cellRef.getSheetName().equals(table.getSheet().getName())) {
            cellRef.setSheetName(table.getSheet().getName());
            modified = true;
        }
        if (cellRef.getSheetName() != null ||
                (cellRef.getTableName() != null && !cellRef.getTableName().equals(table.getName()))) {
            cellRef.setTableName(table.getName());
            modified = true;
        }
        if (cellRef.getRowIndex() != referredCell.getRowIndex() && cellRef.getRowIndex() != -1) {
            cellRef.setRowIndex(referredCell.getRowIndex());
            modified = true;
        }
        if (cellRef.getColumnIndex() != referredCell.getColumnIndex() && cellRef.getColumnIndex() != -1) {
            cellRef.setColumnIndex(referredCell.getColumnIndex());
            modified = true;
        }

        return modified;
    }

    void onChartCreated(DefaultChart chart) {
        onChartUpdated(chart);
    }

    void onChartUpdated(DefaultChart chart) {
        // handle model update.
        withoutRefresh(() -> {
            DefaultSheet sheet = chart.getSheet();
            onValueNodeUpdate(sheet, chart.getTitle());

            if (chart.getBinder() != null) {
                onValueNodeUpdate(sheet, chart.getBinder().getData());
                chart.rebindIfPossible(); // TODO review this to ensure it's the right place.
            }

            onValueNodeUpdate(sheet, chart.getCategories());
            for (int i = 0; i < chart.getSeriesCount(); i++) {
                DefaultDataSeries series = chart.getSeries(i);
                if (series.getName() != null) {
                    onValueNodeUpdate(sheet, series.getName());
                }
                if (series.getxValues() != null) {
                    onValueNodeUpdate(sheet, series.getxValues());
                }
                if (series.getyValues() != null) {
                    onValueNodeUpdate(sheet, series.getyValues());
                }
            }
        });
        refreshIfNeeded();
    }

    void onTableAutomated(DefaultTable table) {
        withoutRefresh(() -> {
            DefaultSheet sheet = table.getSheet();
            Automaton auto = table.getAutomaton();

            // currently a table can only depends on automaton,
            // when it's automaton changed (including create/update/remove),
            // table dependencies can be cleared and rebuilt.
            // table.clearDependencies();

            evaluableNodes.add(((AssetNode) auto).getAssetId());
            evaluableNodes.add(table.getAssetId());

            if (auto instanceof DefaultQueryAutomaton) {
                DefaultQueryTemplate template = ((DefaultQueryAutomaton) auto).getTemplate();
                for (String name : template.getBuiltinParamNames()) {
                    ValueNode param = template.getBuiltinParam(name);
                    // table.addDependency(param);
                    // cannot pass table parameter here, which mislead the method to deal
                    // the node as a table cell
                    onValueNodeUpdate(sheet, param);
                }

            } else if (auto instanceof DefaultPivotAutomaton) {
                ValueNode data = ((DefaultPivotAutomaton) auto).getData();
                // table.addDependency(data);
                onValueNodeUpdate(sheet, data);
            }

            auto.init();
        });

        refreshIfNeeded();
    }

    void onTextCreated(DefaultText text) {
        onTextUpdated(text);
    }

    void onTextUpdated(DefaultText text) {
        withoutRefresh(() -> {
            DefaultSheet sheet = text.getSheet();
            onValueNodeUpdate(sheet, text.getContent());
        });
        refreshIfNeeded();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Sheet sheet : this) {
            ((DefaultSheet) sheet).toString(sb);
        }
        return sb.toString();
    }

    private ActionListenerChain createListenerChain(ActionListener... extraActionListeners) {
        List<ActionListener> listeners = new ArrayList<>();
        if (extraActionListeners != null) {
            for (ActionListener extraListener : extraActionListeners) {
                listeners.add(extraListener);
            }
        }
        return new ActionListenerChain(listeners);
    }

    void withoutRefresh(Runnable runnable) {
        actionContextManager.withContext(new DefaultActionContext(isSkipWelding(), true, isForceRefresh()), runnable);
    }

    @Override
    public Iterator<Sheet> iterator() {
        return new UnmodifiableIterator(sheets.iterator());
    }

    long nextSequenceNumber() {
        return nextSequenceNumber.getAndIncrement();
    }

    Environment getEnvironment() {
        return environment;
    }
}
