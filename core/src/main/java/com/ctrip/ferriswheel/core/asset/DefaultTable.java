package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.automaton.Automaton;
import com.ctrip.ferriswheel.common.automaton.PivotConfiguration;
import com.ctrip.ferriswheel.common.automaton.QueryConfiguration;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.ChartBinder;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.table.*;
import com.ctrip.ferriswheel.common.text.Text;
import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.util.DataSetMetaData;
import com.ctrip.ferriswheel.common.util.StylizedVariant;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Parameter;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.bean.HeaderInfo;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.bean.TextData;
import com.ctrip.ferriswheel.core.formula.*;
import com.ctrip.ferriswheel.core.ref.*;
import com.ctrip.ferriswheel.core.util.References;
import com.ctrip.ferriswheel.core.util.UnmodifiableIterator;
import com.ctrip.ferriswheel.core.view.LayoutImpl;
import com.ctrip.ferriswheel.core.view.TableLayout;

import java.util.*;
import java.util.concurrent.Callable;

public class DefaultTable extends SheetAssetNode implements Table {
    static final int MAX_ROWS = 65535;
    static final int MAX_COLUMNS = 255;

    private List<Header> rowHeaders = new ArrayList<>();
    private List<Header> columnHeaders = new ArrayList<>();
    private final SparseAssetArray<DefaultRow> rows;
    private boolean readOnly = false;
    private final TableLayout layout = new TableLayout();
    private Automaton automaton;
    private Map<Range, Set<Long>> rangeToNodes = new HashMap<>();
    private Map<Long, Set<Range>> nodeToRanges = new HashMap<>();

    private final AutoFiller autoFiller = new AutoFiller();

    DefaultTable(String name, DefaultSheet sheet) {
        super(name, sheet);
        this.rows = new SparseAssetArray<>(this);
    }

    @Override
    public DefaultCell getCell(int rowIndex, int columnIndex) {
        checkRowIndex(rowIndex);
        checkColumnIndex(columnIndex);
        return getOrCreateCell(rowIndex, columnIndex);
    }

    /**
     * Set cell value and notify listener to do further jobs.
     *
     * @param rowIndex
     * @param columnIndex
     * @param value
     * @return
     */
    @Override
    public Variant setCellValue(int rowIndex, int columnIndex, Variant value) {
        return handleAction(new CellAction.SetCellValue(getSheet().getName(), getName(),
                rowIndex, columnIndex, Value.from(value)));
    }

    Variant handleAction(CellAction.SetCellValue setCellValue) {
        final int rowIndex = setCellValue.getRowIndex();
        final int columnIndex = setCellValue.getColumnIndex();
        Variant value = setCellValue.getValue();

        checkForUpdate(rowIndex, columnIndex);

        return publicly(setCellValue, () -> {
            getOrCreateRow(rowIndex).getCellCount();
            DefaultCell cell = getOrCreateCell(rowIndex, columnIndex);
            if (cell.isFormula()) {
                cell.setFormula(null);
            }
            // Old value may be useless and resource consumption
            // Think about remove the return value?
            Variant oldValue = new DynamicValue(cell.getData());
            cell.setValue(value);
            cell.getRow().getCellCount();
            return oldValue;
        });
    }

    /**
     * Set cell formula and notify listener to do further jobs.
     *
     * @param rowIndex
     * @param columnIndex
     * @param formula
     * @return
     */
    @Override
    public String setCellFormula(int rowIndex, int columnIndex, String formula) {
        Formula oldFormula = handleAction(new CellAction.SetCellFormula(getSheet().getName(), getName(),
                rowIndex, columnIndex, formula));
        return oldFormula == null ? null : oldFormula.getString();
    }

    Formula handleAction(CellAction.SetCellFormula setCellFormula) {
        int rowIndex = setCellFormula.getRowIndex();
        int columnIndex = setCellFormula.getColumnIndex();
        String formula = setCellFormula.getFormulaString();
        checkForUpdate(rowIndex, columnIndex);

        return publicly(setCellFormula, () -> {
            getOrCreateRow(rowIndex).getCellCount();
            DefaultCell cell = getOrCreateCell(rowIndex, columnIndex);
            Formula oldFormula = cell.getFormula();
            cell.setFormula(formula == null ? null : new Formula(formula));
            cell.setValue(Value.BLANK); // wipe old value
            return oldFormula;
        });
    }

    @Override
    public void fillUp(int rowIndex, int columnIndex, int nRows) {
        fillUp(rowIndex, columnIndex, columnIndex, nRows);
    }

    @Override
    public void fillUp(int rowIndex, int firstColumn, int lastColumn, int nRows) {
        handleAction(new FillCells.FillUp(getSheet().getName(), getName(),
                rowIndex, firstColumn, lastColumn, nRows));
    }

    @Override
    public void fillRight(int rowIndex, int columnIndex, int nColumns) {
        fillRight(rowIndex, rowIndex, columnIndex, nColumns);
    }

    @Override
    public void fillRight(int firstRow, int lastRow, int columnIndex, int nColumns) {
        handleAction(new FillCells.FillRight(getSheet().getName(), getName(),
                firstRow, lastRow, columnIndex, nColumns));
    }

    @Override
    public void fillDown(int rowIndex, int columnIndex, int nRows) {
        fillDown(rowIndex, columnIndex, columnIndex, nRows);
    }

    @Override
    public void fillDown(int rowIndex, int firstColumn, int lastColumn, int nRows) {
        handleAction(new FillCells.FillDown(getSheet().getName(), getName(),
                rowIndex, firstColumn, lastColumn, nRows));
    }

    @Override
    public void fillLeft(int rowIndex, int columnIndex, int nColumns) {
        fillLeft(rowIndex, rowIndex, columnIndex, nColumns);
    }

    @Override
    public void fillLeft(int firstRow, int lastRow, int columnIndex, int nColumns) {
        handleAction(new FillCells.FillLeft(getSheet().getName(), getName(),
                firstRow, lastRow, columnIndex, nColumns));
    }

    void handleAction(FillCells fillCells) {
        checkForUpdate(fillCells.getLeftBoundary(),
                fillCells.getTopBoundary(),
                fillCells.getRightBoundary(),
                fillCells.getBottomBoundary());
        if (fillCells instanceof FillCells.FillUp) {
            FillCells.FillUp fillUp = (FillCells.FillUp) fillCells;
            publicly(fillCells, () -> {
                for (int i = fillUp.getFirstColumn(); i <= fillUp.getLastColumn(); i++) {
                    autoFiller.fillUp(this, fillUp.getRowIndex(), i, fillUp.getnRows());
                }
            });

        } else if (fillCells instanceof FillCells.FillRight) {
            FillCells.FillRight fillRight = (FillCells.FillRight) fillCells;
            publicly(fillCells, () -> {
                for (int i = fillRight.getFirstRow(); i <= fillRight.getLastRow(); i++) {
                    autoFiller.fillRight(this, i, fillRight.getColumnIndex(), fillRight.getnColumns());
                }
            });

        } else if (fillCells instanceof FillCells.FillDown) {
            FillCells.FillDown fillDown = (FillCells.FillDown) fillCells;
            publicly(fillCells, () -> {
                for (int i = fillDown.getFirstColumn(); i <= fillDown.getLastColumn(); i++) {
                    autoFiller.fillDown(this, fillDown.getRowIndex(), i, fillDown.getnRows());
                }
            });

        } else if (fillCells instanceof FillCells.FillLeft) {
            FillCells.FillLeft fillLeft = (FillCells.FillLeft) fillCells;
            publicly(fillCells, () -> {
                for (int i = fillLeft.getFirstRow(); i <= fillLeft.getLastRow(); i++) {
                    autoFiller.fillLeft(this, i, fillLeft.getColumnIndex(), fillLeft.getnColumns());
                }
            });

        } else {
            throw new RuntimeException("Unknown fill cells action: " + fillCells.getClass());
        }
    }

    @Override
    public void setCellFillUp(int rowIndex, int columnIndex, boolean fillUp) {
        checkForUpdate(rowIndex, columnIndex);
        // TODO transactional and notify action
        getOrCreateCell(rowIndex, columnIndex).setFillUp(fillUp);
    }

    @Override
    public void setCellFillDown(int rowIndex, int columnIndex, boolean fillDown) {
        checkForUpdate(rowIndex, columnIndex);
        // TODO transactional and notify action
        getOrCreateCell(rowIndex, columnIndex).setFillDown(fillDown);
    }

    @Override
    public void setCellFillLeft(int rowIndex, int columnIndex, boolean fillLeft) {
        checkForUpdate(rowIndex, columnIndex);
        // TODO transactional and notify action
        getOrCreateCell(rowIndex, columnIndex).setFillLeft(fillLeft);
    }

    @Override
    public void setCellFillRight(int rowIndex, int columnIndex, boolean fillRight) {
        checkForUpdate(rowIndex, columnIndex);
        // TODO transactional and notify action
        getOrCreateCell(rowIndex, columnIndex).setFillRight(fillRight);
    }

    @Override
    public void setCellsFormat(int rowIndex, int columnIndex, int nRows, int nColumns, String format) {
        handleAction(new SetCellsFormat(getSheet().getName(), getName(), rowIndex, columnIndex, nRows, nColumns, format));
    }

    void handleAction(SetCellsFormat setCellsFormat) {
        final int left = setCellsFormat.getColumnIndex();
        final int top = setCellsFormat.getRowIndex();
        final int right = left + setCellsFormat.getnColumns() - 1;
        final int bottom = top + setCellsFormat.getnRows() - 1;
        final String format = setCellsFormat.getFormat();
        if (right < left || bottom < top) {
            throw new IllegalArgumentException();
        }
        checkForUpdate(left, top, right, bottom);
        publicly(setCellsFormat, () -> {
            for (int rowIndex = top; rowIndex <= bottom; rowIndex++) {
                DefaultRow row = getRow(rowIndex);
                for (int columnIndex = left; columnIndex <= right; columnIndex++) {
                    row.getCell(columnIndex).setFormat(format);
                }
            }
        });
    }

    @Override
    public void eraseCells(int top, int right, int bottom, int left) {
        handleAction(new EraseCells(getSheet().getName(), getName(), top, right, bottom, left));
    }

    void handleAction(EraseCells eraseCells) {
        checkReadOnly(false);
        final int top = eraseCells.getTop();
        final int right = eraseCells.getRight();
        final int bottom = eraseCells.getBottom();
        final int left = eraseCells.getLeft();
        if (top < 0 || left < 0 ||
                left > right || right >= getColumnCount() ||
                top > bottom || bottom >= getRowCount()) {
            throw new IllegalArgumentException();
        }
        publicly(eraseCells, () -> {
            for (int r = top; r <= bottom; r++) {
                DefaultRow row = getRow(r);
                if (row == null) {
                    continue;
                }
                for (int c = left; c <= right; c++) {
                    DefaultCell cell = row.getCell(c);
                    if (cell != null) {
                        cell.erase();
                    }
                }
            }
        });
    }

    @Override
    public void addRows(int rowIndex, int nRows) {
        handleAction(new InsertRows(getSheet().getName(), getName(), rowIndex, nRows));
    }

    void handleAction(InsertRows insertRows) {
        checkReadOnly(false);
        final int rowIndex = insertRows.getRowIndex();
        final int nRows = insertRows.getnRows();
        checkRowIndexForInsert(rowIndex);
        if (nRows <= 0) {
            throw new IllegalArgumentException();
        }
        publicly(insertRows, () -> {
            // insert headers
            List<HeaderInfo> newRowHeaders = new ArrayList<>(nRows);
            for (int i = 0; i < nRows; i++) {
                newRowHeaders.add(new HeaderInfo(/* TBD */));
            }
            rowHeaders.addAll(rowIndex, newRowHeaders);
            // just move rows to make room for new rows.
            for (int i = getRowCount() - 1; i >= rowIndex; i--) {
                DefaultRow row = rows.get(i);
                int toIdx = i + nRows;
                rows.move(i, toIdx);
                if (row != null) {
                    row.setRowIndex(toIdx);
                }
            }
            // no need to createWorkbook new rows, it will be done the first time they are really used.
            final int left = 0;
            final int top = rowIndex;
            final int right = getColumnCount() - 1;
            final int bottom = getRowCount() - 1;

            updateRangeReferences(left, top, right, bottom,
                    null, null, null, null);
            autoFiller.autoFillRowsIfPossible(this, rowIndex, nRows);
        });
    }

    @Override
    public void removeRows(int rowIndex, int nRows) {
        handleAction(new RemoveRows(getSheet().getName(), getName(), rowIndex, nRows));
    }

    List<Row> handleAction(RemoveRows removeRows) {
        checkReadOnly(false);
        final int rowIndex = removeRows.getRowIndex();
        final int nRows = removeRows.getnRows();
        // since empty row/cell is not count by getRowCount, sometimes user trying to
        // remove rows out of getRowCount is reasonable. at present we ignore the range check.
        if (rowIndex < 0 || nRows <= 0 /*|| rowIndex + nRows > getRowCount()*/) {
            throw new IllegalArgumentException();
        }
        return publicly(removeRows, () -> {
            List<Row> removedRows = new ArrayList<>(nRows);
            int i;
            for (i = rowIndex; i < getRowCount() - nRows; i++) {
                int fromIdx = i + nRows;
                DefaultRow row = rows.get(fromIdx);
                DefaultRow oldRow = rows.move(fromIdx, i);
                if (row != null) {
                    row.setRowIndex(i);
                }
                if (oldRow != null) {
                    removedRows.add(oldRow);
                }
            }
            for (; i < rowIndex + nRows; i++) {
                DefaultRow oldRow = rows.remove(i);
                if (oldRow != null) {
                    removedRows.add(oldRow);
                }
            }
            rowHeaders.subList(rowIndex, rowIndex + nRows).clear();

            final int left = 0;
            final int top = rowIndex;

            updateRangeReferences(left, top, null, null,
                    null, rowIndex > 0 ? rowIndex - 1 : null,
                    null, rowIndex);

            return removedRows;
        });
    }

    @Override
    public void addColumns(int colIndex, int nCols) {
        handleAction(new InsertColumns(getSheet().getName(), getName(), colIndex, nCols));
    }

    void handleAction(InsertColumns insertColumns) {
        checkReadOnly(false);
        final int colIndex = insertColumns.getColumnIndex();
        final int nCols = insertColumns.getnColumns();
        checkColumnIndexForInsert(colIndex);
        if (nCols <= 0) {
            throw new IllegalArgumentException();
        }
        publicly(insertColumns, () -> {
            final int oldColumnCount = getColumnCount();
            List<Header> newColumnHeaders = new ArrayList<>(nCols);
            for (int i = 0; i < nCols; i++) {
                newColumnHeaders.add(new HeaderInfo(/* TBD */));
            }
            columnHeaders.addAll(colIndex, newColumnHeaders);
            for (int r = 0; r < getRowCount(); r++) {
                DefaultRow row = getRow(r);
                if (row == null) {
                    continue;
                }
                for (int c = oldColumnCount - 1; c >= colIndex; c--) {
                    row.moveCell(c, c + nCols);
                }
            }

            final int left = colIndex;
            final int top = 0;
            final int right = getColumnCount() - 1;
            final int bottom = getRowCount() - 1;

            updateRangeReferences(left, top, right, bottom,
                    null, null, null, null);
            autoFiller.autoFillColumnsIfPossible(this, colIndex, nCols);
        });
    }

    @Override
    public void removeColumns(int colIndex, int nCols) {
        handleAction(new RemoveColumns(getSheet().getName(), getName(), colIndex, nCols));
    }

    List<Cell> handleAction(RemoveColumns removeColumns) {
        checkReadOnly(false);
        final int colIndex = removeColumns.getColumnIndex();
        final int nCols = removeColumns.getnColumns();
        final int columns = getColumnCount();
        // since getColumnCount may not count BLANK cell in, sometimes it happens that user
        // trying to remove columns out of getColumnCount, at present we ignore the range check.
        if (colIndex < 0 || nCols <= 0 /*|| colIndex + nCols > columns*/) {
            throw new IllegalArgumentException();
        }
        return publicly(removeColumns, () -> {
            List<Cell> removedCells = new ArrayList<>(getRowCount() * nCols);
            for (int r = 0; r < getRowCount(); r++) {
                DefaultRow row = getRow(r);
                if (row == null) {
                    continue;
                }
                int c;
                for (c = colIndex; c < columns - nCols; c++) {
                    DefaultCell oldCell = row.moveCell(c + nCols, c);
                    if (oldCell != null) {
                        removedCells.add(oldCell);
                    }
                }
                for (; c < colIndex + nCols; c++) {
                    DefaultCell oldCell = row.removeCell(c);
                    if (oldCell != null) {
                        removedCells.add(oldCell);
                    }
                }
            }
            columnHeaders.subList(colIndex, colIndex + nCols).clear();


            final int left = colIndex;
            final int top = 0;

            updateRangeReferences(left, top, null, null,
                    colIndex > 0 ? colIndex - 1 : null, null,
                    colIndex, null);

            return removedCells;
        });
    }

    @Override
    public void automate(AutomateConfiguration solution) {
        handleAction(new AutomateTable(getSheet().getName(), getName(), solution));
    }

    void handleAction(AutomateTable automateTable) {
        publicly(automateTable, () -> {
            createAndRegisterAutomaton(automateTable.getSolution());
            Automaton auto = getAutomaton();
            auto.init();
        });
    }

    private Automaton createAndRegisterAutomaton(AutomateConfiguration solution) {
        if (this.automaton != null) {
            unbindChild((AssetNode) this.automaton);
        }

        if (solution instanceof QueryConfiguration) {
            DefaultQueryAutomaton queryAutomaton = new DefaultQueryAutomaton(getAssetManager(),
                    (QueryConfiguration) solution);
            bindChild(queryAutomaton);
            this.automaton = queryAutomaton;
            for (String name : queryAutomaton.getTemplate().getBuiltinParamNames()) {
                Parameter param = queryAutomaton.getTemplate().getBuiltinParam(name);
                queryAutomaton.addDependency((ValueNode) param.getValue());
            }
            return queryAutomaton;

        } else if (solution instanceof PivotConfiguration) {
            DefaultPivotAutomaton pivotAutomaton = new DefaultPivotAutomaton(getAssetManager(),
                    (PivotConfiguration) solution);
            bindChild(pivotAutomaton);
            this.automaton = pivotAutomaton;
            return pivotAutomaton;

        } else {
            throw new RuntimeException("There is probably a bug, unable to create automaton according solution: "
                    + solution.getClass());
        }
    }

    @Override
    public Automaton getAutomaton() {
        return automaton;
    }

    @Override
    public AutomateConfiguration getAutomateConfiguration() {
        if (automaton == null) {
            return null;
        }
        return TableAutomatonInfo.fromAutomaton(automaton);
    }

    private DefaultRow setRow(int index, DefaultRow row) {
        if (row.getCellCount() > getColumnCount()) {
            throw new IllegalArgumentException("Row size exceed column count of the table.");
        }
        DefaultRow oldRow = removeRow(index);
        row.setRowIndex(index);
        rows.set(index, row);
        return oldRow;
    }

    private DefaultRow removeRow(int rowIndex) {
        DefaultRow row = rows.remove(rowIndex);
        return row;
    }

    private DefaultRow getOrCreateRow(int rowIndex) {
        DefaultRow row = getRow(rowIndex);
        if (row == null) {
            row = new DefaultRow(getAssetManager());
            setRow(rowIndex, row);
        }
        return row;
    }

    DefaultCell getOrCreateCell(int rowIndex, int columnIndex) {
        checkRowIndex(rowIndex);
        checkColumnIndex(columnIndex);
        DefaultRow row = getOrCreateRow(rowIndex);
        DefaultCell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = new DefaultCell(getAssetManager());
            row.setCell(columnIndex, cell);
        }
        return cell;
    }

    private void checkForUpdate(int rowIndex, int columnIndex) {
        checkRowIndex(rowIndex);
        checkColumnIndex(columnIndex);
        checkReadOnly(false);
    }

    private void checkForUpdate(int left, int top, int right, int bottom) {
        checkRowIndex(top);
        checkRowIndex(bottom);
        checkColumnIndex(left);
        checkColumnIndex(right);
        checkReadOnly(false);
    }

    private void checkRowIndex(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount()) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkRowIndexForInsert(int rowIndex) {
        if (rowIndex < 0 || rowIndex > getRowCount()) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkColumnIndex(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= getColumnCount()) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkColumnIndexForInsert(int columnIndex) {
        if (columnIndex < 0 || columnIndex > getColumnCount()) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkReadOnly(boolean expected) {
        if (readOnly != expected) {
            throw new IllegalStateException("Read only flag not matches.");
        }
    }

    @Override
    public int getRowCount() {
        return rowHeaders == null ? 0 : rowHeaders.size();
    }

    @Override
    public Header getRowHeader(int rowIndex) {
        return rowHeaders.get(rowIndex);
    }

    @Override
    public DefaultRow getRow(int index) {
        return rows.get(index);
    }

    @Override
    public int getColumnCount() {
        return columnHeaders == null ? 0 : columnHeaders.size();
    }

    @Override
    public Header getColumnHeader(int columnIndex) {
        return columnHeaders.get(columnIndex);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    //    @Override
    public int getMaxRowLimit() {
        return MAX_ROWS;
    }

    //    @Override
    public int getMaxColumnLimit() {
        return MAX_COLUMNS;
    }

    public DefaultSheet getSheet() {
        return (DefaultSheet) getParent();
    }

    DefaultWorkbook getWorkbook() {
        return getSheet() == null ? null : getSheet().getWorkbook();
    }

    @Override
    public Iterator<Map.Entry<Integer, Row>> iterator() {
        return new UnmodifiableIterator(rows.iterator());
    }

    @Override
    public LayoutImpl getLayout() {
        return layout;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public void toString(StringBuilder sb) {
        sb.append("Table: ").append(getName()).append(", rows=").append(getRowCount())
                .append(", cols=").append(getColumnCount()).append("\n");
        sb.append("================================================================================\n");
        sb.append("      |");
        for (int col = 0; col < getColumnCount(); col++) {
            sb.append(String.format(" %-8s \t|", References.toColumnCode(col)));
        }
        sb.delete(sb.length() - 2, sb.length()).append("\n");
        sb.append("================================================================================\n");
        for (int row = 0; row < getRowCount(); row++) {
            DefaultRow defaultRow = getRow(row);
            if (defaultRow == null) {
                continue;
            }
            defaultRow.toString(sb);
            sb.append("--------------------------------------------------------------------------------\n");
        }
    }

    <V> V publicly(Action action, Callable<V> callable) {
        if (getSheet() != null) {
            return getSheet().publicly(action, callable);
        } else {
            try {
                return callable.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    void publicly(Action action, Runnable runnable) {
        if (getSheet() != null) {
            getSheet().publicly(action, runnable);
        } else {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Watch range.
     *
     * @param range  Table range.
     * @param nodeId ID of the node which depends on the specified range.
     */
    void subscribeRange(Range range, long nodeId) {
        Set<Long> nodeIds = rangeToNodes.get(range);
        if (nodeIds == null) {
            nodeIds = new HashSet<>();
            rangeToNodes.put(range, nodeIds);
        }
        nodeIds.add(nodeId);
        Set<Range> ranges = nodeToRanges.get(nodeId);
        if (ranges == null) {
            ranges = new HashSet<>();
            nodeToRanges.put(nodeId, ranges);
        }
        ranges.add(range);
    }

    /**
     * Clear range watchers related to the specified node ID.
     *
     * @param nodeId
     */
    void clearRangeWatcher(long nodeId) {
        Set<Range> ranges = nodeToRanges.remove(nodeId);
        if (ranges == null) {
            return;
        }
        for (Range r : ranges) {
            Set<Long> nodeIds = rangeToNodes.get(r);
            nodeIds.remove(nodeId);
            if (nodeIds.isEmpty()) {
                rangeToNodes.remove(r);
            }
        }
    }

    /**
     * Find overlapped ranges.
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    List<Range> findOverlappedRanges(Integer left,
                                     Integer top,
                                     Integer right,
                                     Integer bottom) {
        // TODO optimize algorithm
        List<Range> ranges = new LinkedList<>();
        for (Range range : rangeToNodes.keySet()) {
            if (range.tableId != getAssetId()) {
                continue;
            }
            if (range.isOverlap(left, top, right, bottom)) {
                ranges.add(range);
            }
        }
        return ranges;
    }

    /**
     * Get node IDs which depend on the specified range.
     *
     * @param range
     * @return
     */
    Set<Long> getWatchers(Range range) {
        Set<Long> set = rangeToNodes.get(range);
        return set == null ? Collections.emptySet() : set;
    }

    Range tableRange(int rowIndex, int colIndex) {
        return tableRange(colIndex, rowIndex, colIndex, rowIndex);
    }

    Range tableRange(int left, int top, int right, int bottom) {
        if (left == -1) {
            left = 0;
        }
        if (top == -1) {
            top = 0;
        }
        if (right == -1) {
            right = Integer.MAX_VALUE;
        }
        if (bottom == -1) {
            bottom = Integer.MAX_VALUE;
        }
        return new Range(getAssetId(), left, top, right, bottom);
    }

    void fillByAutomaton() {
        setReadOnly(false);
        try {
            DataSet dataSet = getAutomaton().getDataSet();
            getSheet().getNotifier().privately(() ->
            {
                if (dataSet != null) {
                    doFillTable(dataSet);
                } else {
                    doClearTable();
                }
                onTableUpdate();
            });
        } finally {
            setReadOnly(true);
            publicly(new ResetTable(getSheet().getName(), this), () -> {
            });
        }
    }

    void doFillTable(DataSet dataSet) {
        DataSetMetaData setMeta = dataSet.getMetaData();
        int rowCount = 0;

        columnHeaders = new ArrayList<>(setMeta.getColumnCount());
        rowHeaders = new ArrayList<>();

        if (setMeta.hasColumnMeta()) {
            rowHeaders.add(new HeaderInfo(/* TBD */));
            for (int col = 0; col < setMeta.getColumnCount(); col++) {
                columnHeaders.add(new HeaderInfo(/* TBD */));
                DefaultCell cell = getOrCreateCell(rowCount, col);
                refreshCellValue(rowCount, col, Value.str(setMeta.getColumnMeta(col).getName()));
            }
            rowCount++;
        } else {
            for (int col = 0; col < setMeta.getColumnCount(); col++) {
                columnHeaders.add(new HeaderInfo(/* TBD */));
            }
        }
        dataSet.rewind();
        while (dataSet.next()) {
            rowHeaders.add(new HeaderInfo(/* TBD */));
            for (int col = 0; col < setMeta.getColumnCount(); col++) {
                StylizedVariant stylizedVariant = dataSet.getColumn(col);
                Variant value = stylizedVariant.getValue();
                if (value == null) {
                    value = Value.BLANK;
                }
                String format = stylizedVariant.getFormat();
//                refreshCellValue(row, col, Value.from(value));
                DefaultCell cell = getOrCreateCell(rowCount, col);
                cell.setValue(value);
                if (format != null) {
                    cell.setFormat(format);
                }
            }
            rowCount++;
        }

        // trim rows/columns if needed
        for (int i = getRowCount(); i < rows.size(); i++) {
            rows.remove(i);
        }
        Iterator<Map.Entry<Integer, DefaultRow>> rowIter = rows.iterator();
        while (rowIter.hasNext()) {
            DefaultRow row = rowIter.next().getValue();
            int count = row.getCellCount();
            for (int i = getColumnCount(); i < count; i++) {
                row.removeCell(i);
            }
        }
    }

    void refreshCellValue(int rowIndex, int columnIndex, Value newValue) {
        DefaultCell cell = getCell(rowIndex, columnIndex);
        if (!newValue.equals(cell.getData())) {
            cell.setValue(newValue);
        }
    }

    void doClearTable() {
        if (getRowCount() > 0) {
            removeRows(0, getRowCount());
        }
        if (this.rowHeaders != null) {
            this.rowHeaders.clear();
        }
        if (this.columnHeaders != null) {
            this.columnHeaders.clear();
        }
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        if (getAutomaton() != null) {
            fillByAutomaton();
        }
        return EvaluationState.DONE;
    }

    void onTableUpdate() {
        updateRangeReferences(0,
                0,
                null,
                null,
                null,
                null,
                null,
                null);
        if (getAutomaton() instanceof DefaultQueryAutomaton) {
            updateNamedReferences((DefaultQueryAutomaton) getAutomaton());
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
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param alignLeft
     * @param alignTop
     * @param alignRight
     * @param alignBottom
     */
    private void updateRangeReferences(int left, int top, Integer right, Integer bottom,
                                       Integer alignLeft, Integer alignTop,
                                       Integer alignRight, Integer alignBottom) {
        if (getWorkbook() != null && getWorkbook().isSkipWelding()) {
            return; // should manually update later.
        }
        List<DefaultTable.Range> ranges = findOverlappedRanges(left, top, right, bottom);
        if (ranges == null || ranges.isEmpty()) {
            return; // nothing to do
        }
        Set<Long> nodes = new HashSet<>();
        for (DefaultTable.Range range : ranges) {
            nodes.addAll(getWatchers(range));
        }
        for (Long id : nodes) {
            VariantNode node = getValueNodeByAssetId(id);
            fixFormulaAfterAreaChanged((ValueNode) node, alignLeft, alignTop, alignRight, alignBottom);
        }
    }

    private ValueNode getValueNodeByAssetId(long id) {
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

    /**
     * @param dependentNode
     * @param alignLeft
     * @param alignTop
     * @param alignRight
     * @param alignBottom
     */
    private void fixFormulaAfterAreaChanged(ValueNode dependentNode,
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
                modified |= fixCellRef(dependentNode, cellReference);

            } else if (elem instanceof RangeReferenceElement) {
                RangeReference rangeReference = ((RangeReferenceElement) elem).getRangeReference();
                modified |= fixRangeRef(dependentNode,
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
            publicly(action, () -> {
                dependentNode.setFormula(new Formula(newFormula));
//                onValueNodeUpdate(dependentNode);
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
            publicly(automateTable, () -> {
                dependentNode.setDynamicVariant(new DynamicValue(newFormula));
//                onValueNodeUpdate(dependentNode);
                automateTable.setSolution(auto instanceof DefaultQueryAutomaton ?
                        ((DefaultQueryAutomaton) auto).getQueryAutomatonInfo() :
                        ((DefaultPivotAutomaton) auto).getPivotAutomatonInfo());
            });

        } else if (dependentNode.getParent() instanceof DefaultFormField
                || dependentNode.getParent() instanceof DefaultFormFieldBinding) {
            dependentNode.setDynamicVariant(new DynamicValue(newFormula));
            DefaultForm form = dependentNode.parent(DefaultForm.class);
            UpdateForm action = new UpdateForm(form.getSheet().getName(), form.getName(), form);
            publicly(action, () -> {
                dependentNode.setFormula(new Formula(newFormula));
                return form;
            });

        } else {
            throw new RuntimeException("Unsupported value node: " + dependentNode);
        }
    }

    private boolean fixRangeRef(ValueNode sourceNode,
                                RangeReference rangeReference,
                                Integer alignLeft,
                                Integer alignTop,
                                Integer alignRight,
                                Integer alignBottom) {
        if (!rangeReference.isPhantom()) {
            return fixNormalRangeRef(sourceNode, rangeReference, alignLeft, alignTop, alignRight, alignBottom);
        } else {
            return fixPhantomRangeRef(sourceNode, rangeReference, alignLeft, alignTop, alignRight, alignBottom);
        }
    }

    private boolean fixNormalRangeRef(ValueNode sourceNode,
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
        modified |= fixCellRef(sourceNode, upperLeftRef);
        modified |= fixCellRef(sourceNode, lowerRightRef);

        // FIXME cut/paste cells not supported yet, but if once supported, upper left cell and lower right cell may resident in different table.

        if (!upperLeftRef.isValid() && !lowerRightRef.isValid()) {
            // invalid range reference

        } else if (!upperLeftRef.isValid()) { // shrink to bottom/right
            if (alignBottom != null) {
                moveToAnotherInternalCell(upperLeftRef,
                        getOrCreateCell(alignBottom,
                                Math.max(upperLeftRef.getPositionRef().getColumnIndex(), 0)));
                modified = true;

            } else if (alignRight != null) {
                moveToAnotherInternalCell(upperLeftRef,
                        getOrCreateCell(
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
                    columnIndex = getColumnCount() - 1;
                }
                if (columnIndex == -1) {
                    columnIndex = 0; // Default
                }
                moveToAnotherInternalCell(lowerRightRef,
                        getOrCreateCell(alignTop, columnIndex));
                modified = true;

            } else if (alignLeft != null) {
                int rowIndex = lowerRightRef.getPositionRef().getRowIndex();
                if (rowIndex == -1) {
                    rowIndex = getRowCount() - 1;
                }
                if (rowIndex == -1) {
                    rowIndex = 0; // Default
                }
                moveToAnotherInternalCell(lowerRightRef,
                        getOrCreateCell(rowIndex, alignLeft));
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

    /**
     * Update row/column index of the reference by the cell's runtime ID. When a cell has moved,
     * use this method to keep the row/column index up to date.
     *
     * @param sourceNode    Source node that the <code>cellReference</code> comes from.
     * @param cellReference
     * @return true if reference has been modified, false otherwise.
     */
    private boolean fixCellRef(ValueNode sourceNode, CellReference cellReference) {
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
                getAssetManager().getReferenceMaintainer().hookCellRef(sourceNode, cellReference);
            } else {
                cellReference.setCellId(UNSPECIFIED_ASSET_ID);
            }

            return true; // FIXME
        }
    }

    private DefaultCell getCellByAssetId(long id) {
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


    private boolean fixPhantomRangeRef(ValueNode sourceNode,
                                       RangeReference rangeReference,
                                       Integer alignLeft,
                                       Integer alignTop,
                                       Integer alignRight,
                                       Integer alignBottom) {
        rangeReference.setAssetName(getName());
        if (!getSheet().getName().equals(sourceNode.parent(DefaultSheet.class).getName())) {
            rangeReference.setSheetName(getSheet().getName());
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
        if (getWorkbook().isSkipWelding() || !(node instanceof ValueNode)) {
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
            publicly(new UpdateForm(
                            form.getSheet().getName(),
                            form.getName(),
                            form),
                    () -> {
                        vn.setFormula(new Formula(newFormula));
                    });
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

    class Range extends SimpleTableRange {
        long tableId;

        Range() {
        }

        Range(long tableId, int left, int top, int right, int bottom) {
            super(left, top, right, bottom);
            this.tableId = tableId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Range range = (Range) o;
            return Objects.equals(tableId, range.tableId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tableId);
        }
    }

}
