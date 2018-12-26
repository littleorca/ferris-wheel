package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.bean.ColumnHeader;
import com.ctrip.ferriswheel.core.bean.RowHeader;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.Formula;
import com.ctrip.ferriswheel.core.intf.*;
import com.ctrip.ferriswheel.core.util.References;
import com.ctrip.ferriswheel.core.util.UnmodifiableIterator;
import com.ctrip.ferriswheel.core.view.Layout;
import com.ctrip.ferriswheel.core.view.Rectangle;
import com.ctrip.ferriswheel.core.view.TableLayout;

import java.util.*;
import java.util.concurrent.Callable;

class DefaultTable extends NamedAssetNode implements Table {
    static final int MAX_ROWS = 65535;
    static final int MAX_COLUMNS = 255;

    private final SparseAssetArray<DefaultRow> rows;
    private int columnCount;
    private boolean readOnly = false;
    private final TableLayout layout = new TableLayout();
    private TableAutomaton automaton;
    private Map<Range, Set<Long>> rangeToNodes = new HashMap<>();
    private Map<Long, Set<Range>> nodeToRanges = new HashMap<>();

    DefaultTable(String name, AssetManager assetManager) {
        super(name, assetManager);
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
            int oldRowSize = getOrCreateRow(rowIndex).size();
            DefaultCell cell = getOrCreateCell(rowIndex, columnIndex);
            if (cell.isFormula()) {
                cell.setFormula(null);
            }
            Variant oldValue = cell.getValue();
            cell.setValue(value);
            int newRowSize = cell.getRow().size();
            if (newRowSize > getColumnCount()) {
                setColumnCount(newRowSize);
            } else if (oldRowSize == getColumnCount() && newRowSize < getColumnCount()) {
                fixColumnCount();
            }
            getWorkbook().onCellUpdate(cell);
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
            int oldRowSize = getOrCreateRow(rowIndex).size();
            DefaultCell cell = getOrCreateCell(rowIndex, columnIndex);
            Formula oldFormula = cell.getFormula();
            cell.setFormula(formula == null ? null : new Formula(formula));
            cell.setValue(Value.BLANK); // wipe old value
            int newRowSize = cell.getRow().size();
            if (newRowSize > getColumnCount()) {
                setColumnCount(newRowSize);
            } else if (oldRowSize == getColumnCount() && newRowSize < getColumnCount()) {
                fixColumnCount();
            }
            getWorkbook().onCellUpdate(cell);
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
        AutoFiller autoFiller = new AutoFiller(getWorkbook());
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
    public void eraseCell(int rowIndex, int colIndex) {
        checkForUpdate(rowIndex, colIndex);
        DefaultCell cell = getCell(rowIndex, colIndex);
        if (!cell.isBlank()) {
            setCellValue(rowIndex, colIndex, Value.BLANK);
        }
    }

    @Override
    public void insertRows(int rowIndex, int nRows) {
        handleAction(new InsertRows(getSheet().getName(), getName(), rowIndex, nRows));
    }

    void handleAction(InsertRows insertRows) {
        checkReadOnly(false);
        final int rowIndex = insertRows.getRowIndex();
        final int nRows = insertRows.getnRows();
        checkRowIndex(rowIndex);
        if (nRows <= 0) {
            throw new IllegalArgumentException();
        }
        publicly(insertRows, () -> {
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
            getWorkbook().onRowsInserted(this, rowIndex, nRows);
        });
    }

    @Override
    public void eraseRows(int rowIndex, int nRows) {
        handleAction(new EraseRows(getSheet().getName(), getName(), rowIndex, nRows));
    }

    void handleAction(EraseRows eraseRows) {
        checkReadOnly(false);
        final int rowIndex = eraseRows.getRowIndex();
        final int nRows = eraseRows.getnRows();
        if (rowIndex < 0 || nRows <= 0 || rowIndex + nRows > getRowCount()) {
            throw new IllegalArgumentException();
        }
        publicly(eraseRows, () -> {
            boolean needFixColumnCount = false;
            for (int i = rowIndex; i < rowIndex + nRows; i++) {
                DefaultRow row = getRow(i);
                if (row != null) {
                    if (!needFixColumnCount && row.size() == getColumnCount()) {
                        needFixColumnCount = true;
                    }
                    row.erase();
                }
            }
            if (needFixColumnCount) {
                fixColumnCount();
            }
            getWorkbook().onRowsErased(this, rowIndex, nRows);
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
            fixColumnCount(); // TODO this can be optimized
            getWorkbook().onRowsRemoved(this, rowIndex, nRows, removedRows);
            return removedRows;
        });
    }

    @Override
    public void insertColumns(int colIndex, int nCols) {
        handleAction(new InsertColumns(getSheet().getName(), getName(), colIndex, nCols));
    }

    void handleAction(InsertColumns insertColumns) {
        checkReadOnly(false);
        final int colIndex = insertColumns.getColumnIndex();
        final int nCols = insertColumns.getnColumns();
        checkColumnIndex(colIndex);
        if (nCols <= 0) {
            throw new IllegalArgumentException();
        }
        publicly(insertColumns, () -> {
            final int columns = getColumnCount();
            for (int r = 0; r < getRowCount(); r++) {
                DefaultRow row = getRow(r);
                if (row == null) {
                    continue;
                }
                for (int c = columns - 1; c >= colIndex; c--) {
                    row.moveCell(c, c + nCols);
                }
            }
            fixColumnCount();
            getWorkbook().onColumnsInserted(this, colIndex, nCols);
        });
    }

    @Override
    public void eraseColumns(int colIndex, int nCols) {
        handleAction(new EraseColumns(getSheet().getName(), getName(), colIndex, nCols));
    }

    void handleAction(EraseColumns eraseColumns) {
        checkReadOnly(false);
        final int colIndex = eraseColumns.getColumnIndex();
        final int nCols = eraseColumns.getnColumns();
        if (colIndex < 0 || nCols <= 0 || colIndex + nCols > getColumnCount()) {
            throw new IllegalArgumentException();
        }
        publicly(eraseColumns, () -> {
            for (int r = 0; r < getRowCount(); r++) {
                DefaultRow row = getRow(r);
                if (row == null) {
                    continue;
                }
                for (int c = colIndex; c < colIndex + nCols; c++) {
                    DefaultCell cell = row.getCell(c);
                    if (cell != null) {
                        cell.erase();
                    }
                }
            }
            if (colIndex + nCols == getColumnCount()) {
                fixColumnCount();
            }
            getWorkbook().onColumnsErased(this, colIndex, nCols);
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
            fixColumnCount();
            getWorkbook().onColumnsRemoved(this, colIndex, nCols, removedCells);
            return removedCells;
        });
    }

    @Override
    public void automate(TableAutomatonInfo solution) {
        handleAction(new AutomateTable(getSheet().getName(), getName(), solution));
    }

    void handleAction(AutomateTable automateTable) {
        publicly(automateTable, () -> {
            createAndRegisterAutomaton(automateTable.getSolution());
            for (DefaultRow row : rows) {
                row.setEphemeral(true);
                for (Cell cell : row) {
                    ((DefaultCell) cell).setEphemeral(true);
                    cell.getDependents().forEach(d -> ((AssetNode) d).addDependency(this));
                }
            }
            getWorkbook().onTableAutomated(this);
        });
    }

    private TableAutomaton createAndRegisterAutomaton(TableAutomatonInfo solution) {
        if (this.automaton != null) {
            unbindChild((AssetNode) this.automaton);
        }

        if (solution instanceof TableAutomatonInfo.QueryAutomatonInfo) {
            DefaultQueryAutomaton queryAutomaton = new DefaultQueryAutomaton(getAssetManager(),
                    (TableAutomatonInfo.QueryAutomatonInfo) solution);
            bindChild(queryAutomaton);
            this.automaton = queryAutomaton;
            return queryAutomaton;

        } else if (solution instanceof TableAutomatonInfo.PivotAutomatonInfo) {
            DefaultPivotAutomaton pivotAutomaton = new DefaultPivotAutomaton(getAssetManager(),
                    (TableAutomatonInfo.PivotAutomatonInfo) solution);
            bindChild(pivotAutomaton);
            this.automaton = pivotAutomaton;
            return pivotAutomaton;

        } else {
            throw new RuntimeException("There is probably a bug, unable to create automaton according solution: "
                    + solution.getClass());
        }
    }

    @Override
    public TableAutomaton getAutomaton() {
        return automaton;
    }

    private DefaultRow setRow(int index, DefaultRow row) {
        DefaultRow oldRow = removeRow(index);
        row.setRowIndex(index);
        rows.set(index, row);
        if (row.size() > getColumnCount()) {
            setColumnCount(rows.size());
        }
        return oldRow;
    }

    private DefaultRow removeRow(int rowIndex) {
        DefaultRow row = rows.remove(rowIndex);
        if (row != null) {
            if (row.size() == getColumnCount()) {
                fixColumnCount();
            }
        }
        return row;
    }

    private DefaultRow getOrCreateRow(int rowIndex) {
        DefaultRow row = getRow(rowIndex);
        if (row == null) {
            row = new DefaultRow(getAssetManager());
            setRow(rowIndex, row);
            if (getAutomaton() != null) {
                row.setEphemeral(true);
            }
        }
        return row;
    }

    DefaultCell getOrCreateCell(int rowIndex, int columnIndex) {
        DefaultRow row = getOrCreateRow(rowIndex);
        DefaultCell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = new DefaultCell((DefaultAssetManager) getAssetManager());
            row.setCell(columnIndex, cell);
            // cell.addDependency(this); // TODO review if it is needed
            if (getAutomaton() != null) {
                row.setEphemeral(true);
            }
        }
        return cell;
    }

    protected void fixColumnCount() {
        int maxColumnCount = 0;
        Iterator<DefaultRow> it = rows.iterator();
        while (it.hasNext()) {
            DefaultRow row = it.next();
            if (maxColumnCount < row.size()) {
                maxColumnCount = row.size();
            }
        }
        setColumnCount(maxColumnCount);
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
        if (rowIndex < 0 || rowIndex >= MAX_ROWS) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkColumnIndex(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= MAX_COLUMNS) {
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
        for (int i = rows.size() - 1; i >= 0; i--) {
            Row row = rows.get(i);
            if (row != null && !row.isBlank()) {
                return i + 1;
            }
        }
        return 0;
    }

    @Override
    public DefaultRow getRow(int index) {
        return rows.get(index);
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    protected void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public int getMaxRowLimit() {
        return MAX_ROWS;
    }

    @Override
    public int getMaxColumnLimit() {
        return MAX_COLUMNS;
    }

    @Override
    public DefaultSheet getSheet() {
        return (DefaultSheet) getParent();
    }

    DefaultWorkbook getWorkbook() {
        return getSheet().getWorkbook();
    }

    @Override
    public RowHeader getRowHeader(int rowIndex) {
        return null;//TODO
    }

    @Override
    public ColumnHeader getColumnHeader(int columnIndex) {
        return null;//TODO
    }

    @Override
    public Iterator<Row> iterator() {
        return new UnmodifiableIterator(rows.iterator());
    }

    @Override
    public Layout getLayout() {
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
        // 先用笨方法实现功能
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

    class Range extends Rectangle {
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

        public boolean isOverlap(Integer left, Integer top, Integer right, Integer bottom) {
            return isRowOverlap(top, bottom) && isColumnOverlap(left, right);
        }

        private boolean isRowOverlap(Integer top, Integer bottom) {
            return (top == null || top <= this.getBottom()) && (bottom == null || bottom >= this.getTop());
        }

        private boolean isColumnOverlap(Integer left, Integer right) {
            return (left == null || left <= this.getRight()) && (right == null || right >= this.getLeft());
        }
    }

}
