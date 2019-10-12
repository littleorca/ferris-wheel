package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.automaton.Automaton;
import com.ctrip.ferriswheel.common.automaton.PivotConfiguration;
import com.ctrip.ferriswheel.common.automaton.QueryConfiguration;
import com.ctrip.ferriswheel.common.table.*;
import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Parameter;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.action.*;
import com.ctrip.ferriswheel.core.bean.HeaderInfo;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.formula.Formula;
import com.ctrip.ferriswheel.core.formula.FormulaElement;
import com.ctrip.ferriswheel.core.formula.FormulaParser;
import com.ctrip.ferriswheel.core.formula.NameReferenceElement;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.NameReference;
import com.ctrip.ferriswheel.core.ref.RangeReference;
import com.ctrip.ferriswheel.core.util.References;
import com.ctrip.ferriswheel.core.view.TableLayout;

import java.util.*;
import java.util.concurrent.Callable;

public class DefaultTable extends SheetAssetNode implements Table {
    static final int MAX_ROWS = 65535;
    static final int MAX_COLUMNS = 255;

    private final GridData gridData;
    private final TableLayout layout = new TableLayout();
    private final HotAreaManager hotAreaManager;
    private Automaton automaton;

    private final AutoFiller autoFiller = new AutoFiller();

    DefaultTable(String name, AssetManager assetManager) {
        super(name, assetManager);
        this.gridData = new GridData(assetManager);
        this.hotAreaManager = new HotAreaManager(assetManager);
        bindChild(this.gridData);
        bindChild(this.hotAreaManager);
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
            gridData.getRowHeaders().addAll(rowIndex, newRowHeaders);
            // just move rows to make room for new rows.
            for (int i = getRowCount() - 1; i >= rowIndex; i--) {
                DefaultRow row = gridData.getRows().get(i);
                int toIdx = i + nRows;
                gridData.getRows().move(i, toIdx);
                if (row != null) {
                    row.setRowIndex(toIdx);
                }
            }
            // no need to createWorkbook new rows, it will be done the first time they are really used.
            final int left = 0;
            final int top = rowIndex;
            final int right = getColumnCount() - 1;
            final int bottom = getRowCount() - 1;

            hotAreaManager.onTableAreaChange(left, top, right, bottom,
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
                DefaultRow row = gridData.getRows().get(fromIdx);
                DefaultRow oldRow = gridData.getRows().move(fromIdx, i);
                if (row != null) {
                    row.setRowIndex(i);
                }
                if (oldRow != null) {
                    removedRows.add(oldRow);
                }
            }
            for (; i < rowIndex + nRows; i++) {
                DefaultRow oldRow = gridData.getRows().remove(i);
                if (oldRow != null) {
                    removedRows.add(oldRow);
                }
            }
            gridData.getRowHeaders().subList(rowIndex, rowIndex + nRows).clear();

            final int left = 0;
            final int top = rowIndex;

            hotAreaManager.onTableAreaChange(left, top, null, null,
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
            gridData.getColumnHeaders().addAll(colIndex, newColumnHeaders);
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

            hotAreaManager.onTableAreaChange(left, top, right, bottom,
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
            gridData.getColumnHeaders().subList(colIndex, colIndex + nCols).clear();


            final int left = colIndex;
            final int top = 0;

            hotAreaManager.onTableAreaChange(left, top, null, null,
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
            gridData.removeDependency((AssetNode) this.automaton);
        }

        if (solution instanceof QueryConfiguration) {
            DefaultQueryAutomaton queryAutomaton = new DefaultQueryAutomaton(getAssetManager(),
                    (QueryConfiguration) solution);
            bindChild(queryAutomaton);
            this.automaton = queryAutomaton;
            gridData.addDependency(queryAutomaton);
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
            gridData.addDependency(pivotAutomaton);
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
        gridData.getRows().set(index, row);
        return oldRow;
    }

    private DefaultRow removeRow(int rowIndex) {
        DefaultRow row = gridData.getRows().remove(rowIndex);
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
        if (!isValidRowIndex(rowIndex)) {
            throw new IndexOutOfBoundsException();
        }
    }

    boolean isValidRowIndex(int rowIndex) {
        return (rowIndex >= 0 && rowIndex < getRowCount());
    }

    private void checkRowIndexForInsert(int rowIndex) {
        if (rowIndex < 0 || rowIndex > getRowCount()) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkColumnIndex(int columnIndex) {
        if (!isValidColumnIndex(columnIndex)) {
            throw new IndexOutOfBoundsException();
        }
    }

    boolean isValidColumnIndex(int columnIndex) {
        return (columnIndex >= 0 && columnIndex < getColumnCount());
    }

    private void checkColumnIndexForInsert(int columnIndex) {
        if (columnIndex < 0 || columnIndex > getColumnCount()) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkReadOnly(boolean expected) {
        if (gridData.isReadOnly() != expected) {
            throw new IllegalStateException("Read only flag not matches.");
        }
    }

    @Override
    public int getRowCount() {
        return gridData.getRowCount();
    }

    @Override
    public Header getRowHeader(int rowIndex) {
        return gridData.getRowHeader(rowIndex);
    }

    @Override
    public DefaultRow getRow(int index) {
        return gridData.getRow(index);
    }

    @Override
    public int getColumnCount() {
        return gridData.getColumnCount();
    }

    @Override
    public Header getColumnHeader(int columnIndex) {
        return gridData.getColumnHeader(columnIndex);
    }

    public boolean isReadOnly() {
        return gridData.isReadOnly();
    }

    protected void setReadOnly(boolean readOnly) {
        gridData.setReadOnly(readOnly);
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
        return gridData.iterator();
    }

    @Override
    public TableLayout getLayout() {
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

    void hookCell(CellReference cellReference, long nodeId) {
        hotAreaManager.hookCell(cellReference, nodeId);

    }

    /**
     * Watch range.
     *
     * @param rangeReference Table range reference.
     * @param nodeId         ID of the node which depends on the specified range.
     */
    void hookArea(RangeReference rangeReference, long nodeId) {
        hotAreaManager.hookRange(rangeReference, nodeId);
    }

    /**
     * Find overlapped areas.
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @return
     */
    private List<HotAreaDelegate> findOverlappedAreas(Integer left,
                                                      Integer top,
                                                      Integer right,
                                                      Integer bottom) {
        return hotAreaManager.findOverlappedAreas(left, top, right, bottom);
    }

    void doFillTable(DataSet dataSet) {
        gridData.fill(dataSet);
    }

    void refreshCellValue(int rowIndex, int columnIndex, Value newValue) {
        DefaultCell cell = getCell(rowIndex, columnIndex);
        if (!newValue.equals(cell.getData())) {
            cell.setValue(newValue);
        }
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        return EvaluationState.DONE;
    }

    void onTableUpdate() {
        hotAreaManager.onTableAreaChange(0,
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
        Formula formula = vn.getFormula();
        if (formula == null) {
            return; // raise warning?
        }
        for (FormulaElement e : formula) {
            if (e instanceof NameReferenceElement) {
                NameReference nameRef = ((NameReferenceElement) e).getNameReference();
                if (nameRef.getTargetId() == param.getAssetId()) {
                    nameRef.setSheetName(sheetName);
                    nameRef.setAssetName(assetName);
                    nameRef.setTargetName(param.getName());
                }
            }
        }
        String newFormula = FormulaParser.assemble(formula, 0, 0);
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

    GridData getGridData() {
        return gridData;
    }

    HotAreaManager getHotAreaManager() {
        return hotAreaManager;
    }

}
