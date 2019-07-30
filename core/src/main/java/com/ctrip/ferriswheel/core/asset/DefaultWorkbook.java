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
import com.ctrip.ferriswheel.common.variant.*;
import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.bean.TextData;
import com.ctrip.ferriswheel.core.formula.*;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluator;
import com.ctrip.ferriswheel.core.formula.eval.ReferenceResolver;
import com.ctrip.ferriswheel.core.ref.*;
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
                            onTableUpdate(table);
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
                    resolveFormulas((AssetNode) asset);
                }
            }
        });
    }

    private void resolveFormulas(AssetNode asset) {
        if (asset instanceof ValueNode) {
            onValueNodeUpdate((ValueNode) asset);
        }
        for (AssetNode child : asset.getChildren()) {
            resolveFormulas(child);
        }
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

// FIXME should throw exception here, but some null asset issues need to be fixed first.
//        } else {
//            throw new IllegalArgumentException();
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
        AssetNode parent = node.getParent();

        if (parent instanceof DefaultChartBinder) {
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
            if (node.getFormulaElements() == null) {
                throw new IllegalArgumentException();
            }
            SheetAssetNode asset = node.parent(SheetAssetNode.class);
            evaluator.setCurrentSheet(asset.getSheet());
            evaluator.setCurrentAsset(asset);
            Variant value = evaluator.evaluate(node.getFormulaElements());
            if (!value.equals(node.getData())) {
                updateValue(node, value);
            }
        }
    }

    private void updateValue(ValueNode node, Variant newValue) {
        AssetNode parent = node.getParent();
        if (node instanceof DefaultCell) {
            updateCellValue((DefaultCell) node, newValue);

        } else if (parent instanceof DefaultDataSeries) {
            updateChartPropertyValue(((DefaultDataSeries) parent).getChart(), node, newValue);

        } else if (parent instanceof DefaultChart) {
            updateChartPropertyValue((DefaultChart) parent, node, newValue);

        } else if (parent instanceof DefaultQueryTemplate) {
            updateQueryParamValue((DefaultQueryTemplate) parent, node, newValue);

        } else if (parent instanceof DefaultText) {
            evaluateText((DefaultText) parent, node, newValue);

        } else if (parent instanceof DefaultChartBinder) {
            ((DefaultChartBinder) parent).getChart().rebindIfPossible();

        } else if (parent instanceof DefaultFormField) {
            // field options
            DefaultForm form = (DefaultForm) parent.getParent();
            UpdateForm updateForm = new UpdateForm(form.getSheet().getName(), form.getName(), form);
            listenerChain.publicly(updateForm, () -> {
                node.setValue(newValue);
            });

        } else if (parent instanceof DefaultFormFieldBinding) {
            DefaultFormField ff = parent.parent(DefaultFormField.class);
            // at present there is no need to set node's value as it only purposed to track reference
            // but we can initiate form field's value at this moment.
            if (ff.getValue().isBlank()) {
                ff.setValue(newValue);
            }

        } else {
            throw new RuntimeException("Unsupported value node: " + node);
        }
    }

    private void updateCellValue(DefaultCell cell, Variant newValue) {
        DefaultTable table = cell.getRow().getTable();
        CellAction.RefreshCellValue action = new CellAction.RefreshCellValue(
                table.getSheet().getName(),
                table.getName(),
                cell.getRowIndex(),
                cell.getColumnIndex(),
                Value.from(newValue));
        listenerChain.publicly(action, () -> cell.setValue(newValue));
    }

    private void updateChartPropertyValue(DefaultChart chart, ValueNode property, Variant newValue) {
        UpdateChart action = new UpdateChart(chart.getSheet().getName(), chart.getName(), null);
        listenerChain.publicly(action, () -> {
            property.setValue(newValue);
            return chart;
        });
    }

    private void updateQueryParamValue(DefaultQueryTemplate queryTemplate, ValueNode param, Variant newValue) {
        param.setValue(newValue); // TODO notify this action?
    }

    private void evaluateText(DefaultText text, ValueNode node, Variant newValue) {
        UpdateText action = new UpdateText(text.getSheet().getName(), text.getName(), null);
        listenerChain.publicly(action, () -> {
            node.setValue(newValue);
            action.setTextData(new TextData(text.getName(),
                    new DynamicValue(node.getFormulaString(), Value.from(node.getData())),
                    text.getLayout()));
        });
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

    DefaultSheet getReferredSheet(AbstractReference ref, DefaultSheet currentSheet) {
        if (ref.getSheetName() == null) {
            return currentSheet;
        } else {
            return getSheet(ref.getSheetName());
        }
    }

    SheetAssetNode getReferredSheetAsset(AbstractReference ref, SheetAssetNode currentAsset) {
        return getReferredSheetAsset(ref,
                currentAsset == null ? null : currentAsset.getSheet(),
                currentAsset);
    }

    SheetAssetNode getReferredSheetAsset(AbstractReference ref, DefaultSheet currentSheet, SheetAssetNode currentAsset) {
        if (ref.getAssetName() == null) {
            return currentAsset;
        }
        DefaultSheet sheet = getReferredSheet(ref, currentSheet);
        return sheet == null ? null : sheet.getAsset(ref.getAssetName());
    }

    public DefaultAssetManager getAssetManager() {
        return (DefaultAssetManager) super.getAssetManager();
    }

    @Override
    public Variant resolve(CellReferenceElement referenceElement, FormulaEvaluationContext context) {
        CellReference cellReference = referenceElement.getCellReference();
        if (!cellReference.isAlive()) {
            return Value.err(ErrorCodes.REF);
        }
        if (cellReference.getCellId() != Asset.UNSPECIFIED_ASSET_ID) {
            Asset asset = getAssetManager().get(cellReference.getCellId());
            if (asset == null || !(asset instanceof Cell)) {
                return Value.err(ErrorCodes.REF);
            }
            return ((DefaultCell) asset).getData();
        }
        return Value.err(ErrorCodes.REF); // cell not found
    }

    // TODO compare to another resolve method, this violates DRY principle!
    // Types of references may change in the future, review this by that time.
    @Override
    public Variant resolve(NameReferenceElement referenceElement, FormulaEvaluationContext context) {
        NameReference nameReference = referenceElement.getNameReference();
        if (!nameReference.isValid()) {
            return Value.err(ErrorCodes.REF);
        }
        if (nameReference.getTargetId() != Asset.UNSPECIFIED_ASSET_ID) {
            Asset asset = getAssetManager().get(nameReference.getTargetId());
            if (asset == null || !(asset instanceof ValueNode)) {
                return Value.err(ErrorCodes.REF);
            }
            return ((ValueNode) asset).getData();
        }
        return Value.err(ErrorCodes.REF);
    }

    @Override
    public Table resolveTable(String sheetName, String tableName, FormulaEvaluationContext context) {
        if (sheetName == null && tableName == null) {
            return (Table) context.getCurrentAsset();
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
        onValueNodeUpdate(cell);
    }

    /**
     * This method resolve formula and set reference anchor (target asset ID),
     * and also updates dependencies. MAYBE split those two step for convenience
     * to do ops like resettle without update dependencies(refer {@link #resettle()}).
     *
     * @param valueNode
     */
    private void onValueNodeUpdate(ValueNode valueNode) {
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
                if (e instanceof CellReferenceElement) {
                    CellReference cellReference = ((CellReferenceElement) e).getCellReference();
                    hookCellRef(valueNode, cellReference);
                    traceRange(valueNode, cellReference);

                } else if (e instanceof NameReferenceElement) {
                    NameReference nameReference = ((NameReferenceElement) e).getNameReference();
                    hookNameRef(valueNode, nameReference);

                } else if (e instanceof RangeReferenceElement) {
                    RangeReference rangeReference = ((RangeReferenceElement) e).getRangeReference();
                    hookRangeRef(valueNode, rangeReference);
                    traceRange(valueNode, rangeReference);
                }
            }
        }

        refreshIfNeeded();
    }

    private void hookCellRef(ValueNode fromNode, CellReference cellReference) {
        if (!cellReference.isAlive()) {
            return;
        }
        DefaultTable table = (DefaultTable) getReferredSheetAsset(cellReference,
                fromNode.parent(DefaultSheet.class),
                fromNode.parent(DefaultTable.class));
        if (table == null) {
            cellReference.setAlive(false);
            return;
        }
        if (table.getAutomaton() != null) {
            cellReference.setPhantom(true); // actually this only need to be done once
        }
        final int rowIndex = cellReference.getPositionRef().getRowIndex();
        final int columnIndex = cellReference.getPositionRef().getColumnIndex();
        if (rowIndex < 0 || rowIndex >= table.getRowCount() ||
                columnIndex < 0 || columnIndex >= table.getColumnCount()) {
            if (cellReference.isPhantom()) {
                cellReference.setCellId(DefaultCell.UNSPECIFIED_ASSET_ID);
                fromNode.addDependency(table);
            } else {
                cellReference.setAlive(false);
            }
            return;
        }
        DefaultCell depCell = table.getCell(rowIndex, columnIndex);
        cellReference.setCellId(depCell.getAssetId()); // fill runtime id for convenience.
        if (cellReference.isPhantom()) {
            fromNode.addDependency(table);
        } else {
            fromNode.addDependency(depCell);
        }
    }

    private void hookNameRef(ValueNode fromNode, NameReference nameReference) {
        if (!nameReference.isValid()) {
            return;
        }
        SheetAssetNode asset = getReferredSheetAsset(nameReference, fromNode.parent(DefaultSheet.class), fromNode.parent(SheetAssetNode.class));
        // Currently only parameter of the query table supports name reference
        if (asset == null || !(asset instanceof DefaultTable)) {
            return;
        }
        DefaultTable table = (DefaultTable) asset;
        if (table.getAutomaton() == null || !(table.getAutomaton() instanceof DefaultQueryAutomaton)) {
            return;
        }
        DefaultQueryTemplate template = ((DefaultQueryAutomaton) table.getAutomaton()).getTemplate();
        Parameter param = template.getBuiltinParam(nameReference.getTargetName());
        if (param == null) {
            return;
        }
        ValueNode targetNode = (ValueNode) param.getValue();
        nameReference.setTargetId(targetNode.getAssetId());
        fromNode.addDependency(targetNode);
    }

    private void hookRangeRef(ValueNode fromNode, RangeReference rangeReference) {
        if (!rangeReference.isAlive()) {
            return;
        }
        DefaultTable table = (DefaultTable) getReferredSheetAsset(rangeReference,
                fromNode.parent(DefaultSheet.class),
                fromNode.parent(DefaultTable.class));
        if (table == null) {
            rangeReference.setAlive(false);
            return;
//            throw new IllegalArgumentException();
        }
        if (table.getAutomaton() != null) {
            rangeReference.setPhantom(true); // actually this only need to be done once
        }
        // fill runtime id for convenience.
        final int left = rangeReference.getLeft() == -1 ? 0 : rangeReference.getLeft();
        final int top = rangeReference.getTop() == -1 ? 0 : rangeReference.getTop();
        final int right = rangeReference.getRight() != -1 ? rangeReference.getRight() :
                table.getColumnCount() > 0 ? table.getColumnCount() - 1 : 0;
        final int bottom = rangeReference.getBottom() != -1 ? rangeReference.getBottom() :
                table.getRowCount() > 0 ? table.getRowCount() - 1 : 0;
        if (top >= 0 && top < table.getRowCount() && left >= 0 && left < table.getColumnCount()) {
            DefaultCell upperLeft = table.getCell(top, left);
            rangeReference.setUpperLeftTargetId(upperLeft.getAssetId());
        }
        if (bottom >= 0 && bottom < table.getRowCount() && right >= 0 && right < table.getColumnCount()) {
            DefaultCell lowerRight = table.getCell(bottom, right);
            rangeReference.setLowerRightTargetId(lowerRight.getAssetId());
        }

        if (rangeReference.isPhantom()) {
            fromNode.addDependency(table);

        } else {
            // scan dependencies
            for (int row = Math.max(top, 0); row <= bottom && row < table.getRowCount(); row++) {
                for (int col = Math.max(left, 0); col <= right && col < table.getColumnCount(); col++) {
                    DefaultCell depCell = table.getCell(row, col);
                    if (depCell != null) {
                        fromNode.addDependency(depCell);
                    }
                }
            }
        }
    }

    private void traceRange(ValueNode fromNode, CellReference cellReference) {
        if (!cellReference.isAlive()) {
            return;
        }
        DefaultTable referredTable = (DefaultTable) getReferredSheetAsset(cellReference,
                fromNode.parent(DefaultSheet.class),
                fromNode.parent(DefaultTable.class));
        PositionRef positionRef = cellReference.getPositionRef();
        fromNode.watchRange(referredTable,
                positionRef.getColumnIndex(),
                positionRef.getRowIndex(),
                positionRef.getColumnIndex(),
                positionRef.getRowIndex());
    }

    private void traceRange(ValueNode fromNode, RangeReference rangeReference) {
        DefaultTable targetTable = (DefaultTable) getReferredSheetAsset(rangeReference,
                fromNode.parent(DefaultSheet.class),
                fromNode.parent(DefaultTable.class));

        if (targetTable == null) {
            return; // TODO is that ok?
        }

        if (rangeReference.isAlive()) {
            fromNode.watchRange(targetTable,
                    rangeReference.getLeft(),
                    rangeReference.getTop(),
                    rangeReference.getRight(),
                    rangeReference.getBottom());

//        } else if (rangeReference.getUpperLeft().isAlive()) {
//            fromNode.watchRange(targetTable,
//                    rangeReference.getUpperLeft().getRowIndex(),
//                    rangeReference.getUpperLeft().getColumnIndex());
//
//        } else if (rangeReference.getLowerRight().isAlive()) {
//            fromNode.watchRange(targetTable,
//                    rangeReference.getLowerRight().getRowIndex(),
//                    rangeReference.getLowerRight().getColumnIndex());
        }
    }

    void onCellsErased(DefaultTable table, int top, int right, int bottom, int left) {
        refreshIfNeeded();
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

    void onTableUpdate(DefaultTable table) {
        updateRangeReferences(table,
                0,
                0,
                null,
                null,
                null,
                null,
                null,
                null);
        if (table.getAutomaton() instanceof DefaultQueryAutomaton) {
            updateNamedReferences((DefaultQueryAutomaton) table.getAutomaton());
        }
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
     * @param changedTable  In which table, area change happened.
     * @param dependentNode
     * @param alignLeft
     * @param alignTop
     * @param alignRight
     * @param alignBottom
     */
    private void fixFormulaAfterAreaChanged(DefaultTable changedTable, ValueNode dependentNode,
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
        for (FormulaElement elem : dependentNode.getFormulaElements()) {
            if (!(elem instanceof ReferenceElement)) {
                continue;
            }
            if (elem instanceof CellReferenceElement) {
                CellReference cellReference = ((CellReferenceElement) elem).getCellReference();
                modified |= fixCellRef(changedTable, dependentNode, cellReference);

            } else if (elem instanceof RangeReferenceElement) {
                RangeReference rangeReference = ((RangeReferenceElement) elem).getRangeReference();
                modified |= fixRangeRef(changedTable,
                        dependentNode,
                        rangeReference,
                        alignLeft,
                        alignTop,
                        alignRight,
                        alignBottom);

            } else {
                throw new RuntimeException("What? I don't recognize the reference: " + elem.getClass());
            }
        }

//        if (!modified) {
//            return; // FIXME 如果采用 table!A:D这样的公式，行数变化时公式并无变化，但引用的数据实际需要刷新的。
//        }

        // now lets construct new formula
        String newFormula = FormulaParser.toFormula(dependentNode.getFormulaElements(), 0, 0);
        if (dependentNode instanceof DefaultCell) {
            DefaultCell cell = (DefaultCell) dependentNode;
            cell.getRow().getTable().setCellFormula(cell.getRowIndex(), cell.getColumnIndex(), newFormula); // this will trigger onCellUpdate, some other businesses will be done there.

        } else if (dependentNode.getParent() instanceof Chart
                || dependentNode.getParent() instanceof DataSeries
                || dependentNode.getParent() instanceof ChartBinder) {
            // chart property update is not as easy as a cell, property of chart may be a little
            // difficult to trace as it doesn't has row/column index. a chart property can be
            // a categories formula, or one property of any series.
            // Of cause it's not hard to update chart property itself, however, what changes
            // should we notify the listeners (especially the revise collector)?
            // currently just tell listeners the chart has changed, either model or data.
            DefaultChart chart = (DefaultChart)
                    (((dependentNode.getParent() instanceof DataSeries)
                            || dependentNode.getParent() instanceof ChartBinder) ?
                            dependentNode.getParent().getParent() : dependentNode.getParent());
            UpdateChart action = new UpdateChart(chart.getSheet().getName(), chart.getName(), null);
            listenerChain.publicly(action, () -> {
                dependentNode.setFormula(new Formula(newFormula));
                onValueNodeUpdate(dependentNode);
                return chart;
            });

        } else if (dependentNode.getParent() instanceof Text) {
            DefaultText text = (DefaultText) dependentNode.getParent();
            text.getSheet().updateText(text.getName(), new TextData(text.getName(), new DynamicValue(newFormula), null));

        } else if (dependentNode.getParent() instanceof DefaultQueryAutomaton
                || dependentNode.getParent() instanceof DefaultPivotAutomaton
                || dependentNode.getParent() instanceof DefaultQueryTemplate) {
            AbstractAutomaton auto = dependentNode.parent(AbstractAutomaton.class);
            AutomateTable automateTable = new AutomateTable(auto.getTable().getSheet().getName(),
                    auto.getTable().getName(), null);
            listenerChain.publicly(automateTable, () -> {
                dependentNode.setDynamicVariant(new DynamicValue(newFormula));
                onValueNodeUpdate(dependentNode);
                automateTable.setSolution(auto instanceof DefaultQueryAutomaton ?
                        ((DefaultQueryAutomaton) auto).getQueryAutomatonInfo() :
                        ((DefaultPivotAutomaton) auto).getPivotAutomatonInfo());
            });

        } else if (dependentNode.getParent() instanceof DefaultFormField
                || dependentNode.getParent() instanceof DefaultFormFieldBinding) {
            dependentNode.setDynamicVariant(new DynamicValue(newFormula));
            DefaultForm form = dependentNode.parent(DefaultForm.class);
            UpdateForm action = new UpdateForm(form.getSheet().getName(), form.getName(), form);
            listenerChain.publicly(action, () -> {
                dependentNode.setFormula(new Formula(newFormula));
                onValueNodeUpdate(dependentNode);
                return form;
            });

        } else {
            throw new RuntimeException("Unsupported value node: " + dependentNode);
        }
    }

    private void moveToAnotherInternalCell(CellReference cellReference, DefaultCell newCell) {
        cellReference.setCellId(newCell.getAssetId());
        PositionRef positionRef = cellReference.getPositionRef();
        if (positionRef.getRowIndex() != -1) {
            positionRef.setRowIndex(newCell.getRowIndex());
        }
        if (positionRef.getColumnIndex() != -1) {
            positionRef.setColumnIndex(newCell.getColumnIndex());
        }
        cellReference.setAlive(true);
    }

    /**
     * Update row/column index of the reference by the cell's runtime ID. When a cell has moved,
     * use this method to keep the row/column index up to date.
     *
     * @param changedTable
     * @param sourceNode    Source node that the <code>cellReference</code> comes from.
     * @param cellReference
     * @return true if reference has been modified, false otherwise.
     */
    private boolean fixCellRef(DefaultTable changedTable, ValueNode sourceNode, CellReference cellReference) {
        if (!cellReference.isAlive()) {
            return false;
        }
        if (!cellReference.isPhantom()) {
            DefaultCell referredCell = getCellByAssetId(cellReference.getCellId());
            if (referredCell == null) {
                cellReference.setAlive(false);
                cellReference.setCellId(UNSPECIFIED_ASSET_ID);
                return true;
            }

            boolean modified = false;
            DefaultTable table = referredCell.getRow().getTable();

            String oldRefSheetName = cellReference.getSheetName() == null ?
                    sourceNode.parent(DefaultSheet.class).getName() : cellReference.getSheetName();
            String oldRefTableName = cellReference.getAssetName() == null ?
                    sourceNode.parent(SheetAssetNode.class).getName() : cellReference.getAssetName();

            if (!oldRefSheetName.equals(table.getSheet().getName())) {
                cellReference.setSheetName(table.getSheet().getName());
                modified = true;
            }

            if (!oldRefTableName.equals(table.getName())) {
                cellReference.setAssetName(table.getName());
                modified = true;
            }

            PositionRef positionRef = cellReference.getPositionRef();
            if (positionRef.getRowIndex() != referredCell.getRowIndex()
                    && positionRef.getRowIndex() != -1) {
                positionRef.setRowIndex(referredCell.getRowIndex());
                modified = true;
            }
            if (positionRef.getColumnIndex() != referredCell.getColumnIndex()
                    && positionRef.getColumnIndex() != -1) {
                positionRef.setColumnIndex(referredCell.getColumnIndex());
                modified = true;
            }

            return modified;

        } else { // phantom reference, just try to re-hook
            if (cellReference.getPositionRef().getRowIndex() != -1 &&
                    cellReference.getPositionRef().getColumnIndex() != -1) {
                hookCellRef(sourceNode, cellReference);
            } else {
                cellReference.setCellId(UNSPECIFIED_ASSET_ID);
            }

            return true; // FIXME
        }
    }

    private boolean fixRangeRef(DefaultTable changedTable,
                                ValueNode sourceNode,
                                RangeReference rangeReference,
                                Integer alignLeft,
                                Integer alignTop,
                                Integer alignRight,
                                Integer alignBottom) {
        if (!rangeReference.isPhantom()) {
            return fixNormalRangeRef(changedTable, sourceNode, rangeReference, alignLeft, alignTop, alignRight, alignBottom);
        } else {
            return fixPhantomRangeRef(changedTable, sourceNode, rangeReference, alignLeft, alignTop, alignRight, alignBottom);
        }
    }

    private boolean fixNormalRangeRef(DefaultTable changedTable,
                                      ValueNode sourceNode,
                                      RangeReference rangeReference,
                                      Integer alignLeft,
                                      Integer alignTop,
                                      Integer alignRight,
                                      Integer alignBottom) {
        boolean modified = false;
        CellReference upperLeftRef = new CellReference(rangeReference.getSheetName(),
                rangeReference.getAssetName(),
                rangeReference.getUpperLeftRef(),
                rangeReference.getUpperLeftTargetId(),
                rangeReference.isAlive(),
                rangeReference.isPhantom());
        CellReference lowerRightRef = new CellReference(rangeReference.getSheetName(),
                rangeReference.getAssetName(),
                rangeReference.getLowerRightRef(),
                rangeReference.getLowerRightTargetId(),
                rangeReference.isAlive(),
                rangeReference.isPhantom());
        modified |= fixCellRef(changedTable, sourceNode, upperLeftRef);
        modified |= fixCellRef(changedTable, sourceNode, lowerRightRef);

        // FIXME cut/past cells not supported yet, but if once supported, upper left cell and lower right cell may resident in different table.

        if (!upperLeftRef.isValid() && !lowerRightRef.isValid()) {
            // invalid range reference

        } else if (!upperLeftRef.isValid()) { // shrink to bottom/right
            if (alignBottom != null) {
                moveToAnotherInternalCell(upperLeftRef,
                        changedTable.getOrCreateCell(alignBottom,
                                Math.max(upperLeftRef.getPositionRef().getColumnIndex(), 0)));
                modified = true;

            } else if (alignRight != null) {
                moveToAnotherInternalCell(upperLeftRef,
                        changedTable.getOrCreateCell(
                                Math.max(upperLeftRef.getPositionRef().getRowIndex(), 0),
                                alignRight));
                modified = true;

            } else {
                throw new RuntimeException("Failed to fix formula.");
            }

        } else if (!lowerRightRef.isValid()) { // shrink to top/left
            if (alignTop != null) {
                int columnIndex = lowerRightRef.getPositionRef().getColumnIndex();
                if (columnIndex == -1) {
                    columnIndex = changedTable.getColumnCount() - 1;
                }
                if (columnIndex == -1) {
                    columnIndex = 0; // Default
                }
                moveToAnotherInternalCell(lowerRightRef,
                        changedTable.getOrCreateCell(alignTop, columnIndex));
                modified = true;

            } else if (alignLeft != null) {
                int rowIndex = lowerRightRef.getPositionRef().getRowIndex();
                if (rowIndex == -1) {
                    rowIndex = changedTable.getRowCount() - 1;
                }
                if (rowIndex == -1) {
                    rowIndex = 0; // Default
                }
                moveToAnotherInternalCell(lowerRightRef,
                        changedTable.getOrCreateCell(rowIndex, alignLeft));
                modified = true;

            } else {
                throw new RuntimeException("Failed to fix formula.");
            }
        }

        if (modified) {
            rangeReference.setSheetName(upperLeftRef.getSheetName());
            rangeReference.setAssetName(upperLeftRef.getAssetName());

            rangeReference.setUpperLeftRef(upperLeftRef.getPositionRef());
            rangeReference.setUpperLeftTargetId(upperLeftRef.getCellId());
            rangeReference.setLowerRightRef(lowerRightRef.getPositionRef());
            rangeReference.setLowerRightTargetId(lowerRightRef.getCellId());
            rangeReference.setAlive(upperLeftRef.isAlive() && lowerRightRef.isAlive());
        }

        return modified;
    }

    private boolean fixPhantomRangeRef(DefaultTable changedTable,
                                       ValueNode sourceNode,
                                       RangeReference rangeReference,
                                       Integer alignLeft,
                                       Integer alignTop,
                                       Integer alignRight,
                                       Integer alignBottom) {
        rangeReference.setAssetName(changedTable.getName());
        if (!changedTable.getSheet().getName().equals(sourceNode.parent(DefaultSheet.class).getName())) {
            rangeReference.setSheetName(changedTable.getSheet().getName());
        } else {
            rangeReference.setSheetName(null);
        }

        return true;
    }

    private void updateNamedReferences(DefaultQueryAutomaton queryAutomaton) {
        Collection<Parameter> params = queryAutomaton.getTemplate().getAllBuiltinParams().values();
        for (Parameter param : params) {
            ValueNode valueNode = (ValueNode) param;
            // copy dependent set as during the fix procedure the set can be rebuilt and
            // cause concurrent modification issue.
            Set<AssetNode> dependents = new HashSet(valueNode.getDependents());
            for (AssetNode node : dependents) {
                fixNameReferenceForForm(node, (ManagedParameter) param);
            }
        }
    }

    private void fixNameReferenceForForm(AssetNode node, ManagedParameter param) {
        if (isSkipWelding() || !(node instanceof ValueNode)) {
            return; // should manually update later.
        }
        String sheetName = param.parent(DefaultSheet.class).getName();
        String assetName = param.parent(DefaultTable.class).getName();
        ValueNode vn = (ValueNode) node;
        FormulaElement[] elems = vn.getFormulaElements();
        if (elems == null) {
            return; // raise warning?
        }
        for (FormulaElement e : elems) {
            if (e instanceof NameReferenceElement) {
                NameReference nameRef = ((NameReferenceElement) e).getNameReference();
                if (nameRef.getTargetId() == param.getAssetId()) {
                    nameRef.setSheetName(sheetName);
                    nameRef.setAssetName(assetName);
                    nameRef.setTargetName(param.getName());
                }
            }
        }
        String newFormula = FormulaParser.toFormula(elems, 0, 0);
        if (!newFormula.equals(vn.getFormulaString())) {
            DefaultForm form = vn.parent(DefaultForm.class);
            listenerChain.publicly(new UpdateForm(
                            form.getSheet().getName(),
                            form.getName(),
                            form),
                    () -> {
                        vn.setFormula(new Formula(newFormula));
                        onValueNodeUpdate(vn);
                    });
        }
    }

    void onChartCreated(DefaultChart chart) {
        onChartUpdated(chart);
    }

    void onChartUpdated(DefaultChart chart) {
        // handle model update.
        withoutRefresh(() -> {
            onValueNodeUpdate(chart.getTitle());

            if (chart.getBinder() != null) {
                onValueNodeUpdate(chart.getBinder().getData());
                chart.rebindIfPossible(); // TODO review this to ensure it's the right place.
            }

            onValueNodeUpdate(chart.getCategories());
            for (int i = 0; i < chart.getSeriesCount(); i++) {
                DefaultDataSeries series = chart.getSeries(i);
                if (series.getName() != null) {
                    onValueNodeUpdate(series.getName());
                }
                if (series.getxValues() != null) {
                    onValueNodeUpdate(series.getxValues());
                }
                if (series.getyValues() != null) {
                    onValueNodeUpdate(series.getyValues());
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
                    Parameter param = template.getBuiltinParam(name);
                    // table.addDependency(param);
                    // cannot pass table parameter here, which mislead the method to deal
                    // the node as a table cell
                    onValueNodeUpdate((ValueNode) param.getValue());
                }

            } else if (auto instanceof DefaultPivotAutomaton) {
                ValueNode data = ((DefaultPivotAutomaton) auto).getData();
                // table.addDependency(data);
                onValueNodeUpdate(data);
            }

            auto.init();
        });

        refreshIfNeeded();
    }

    void onAssetUpdate(AssetNode assetNode) {
        withoutRefresh(() -> postAssetUpdate(assetNode));
        refreshIfNeeded();
    }

    private void postAssetUpdate(AssetNode assetNode) {
        if (assetNode instanceof ValueNode) {
            onValueNodeUpdate((ValueNode) assetNode);
        }
        assetNode.getChildren().forEach(child -> postAssetUpdate(child));
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
