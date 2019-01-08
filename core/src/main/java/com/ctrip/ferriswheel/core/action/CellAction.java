package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.variant.Variant;

public abstract class CellAction extends TableAction implements Action {
    private int rowIndex;
    private int columnIndex;

    CellAction() {
    }

    CellAction(String sheetName, String tableName, int rowIndex, int columnIndex) {
        super(sheetName, tableName);
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    /**
     * Set cell value and clear cell formula if the cell has one.
     */
    public static class SetCellValue extends CellAction {
        private Value value;

        public SetCellValue() {
        }

        public SetCellValue(String sheetName, String tableName, int rowIndex, int columnIndex, Variant value) {
            super(sheetName, tableName, rowIndex, columnIndex);
            this.value = Value.from(value);
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }
    }

    /**
     * Set cell formula.
     */
    public static class SetCellFormula extends CellAction {
        private String formulaString;

        public SetCellFormula() {
        }

        public SetCellFormula(String sheetName, String tableName, int rowIndex, int columnIndex, String formulaString) {
            super(sheetName, tableName, rowIndex, columnIndex);
            this.formulaString = formulaString;
        }

        public String getFormulaString() {
            return formulaString;
        }

        public void setFormulaString(String formulaString) {
            this.formulaString = formulaString;
        }
    }

    /**
     * Refresh cell value without touching cell formula.
     */
    public static class RefreshCellValue extends CellAction implements Action {
        private Value value;

        public RefreshCellValue() {
        }

        public RefreshCellValue(String sheetName, String tableName, int rowIndex, int columnIndex, Value value) {
            super(sheetName, tableName, rowIndex, columnIndex);
            this.value = value;
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }
    }

}
