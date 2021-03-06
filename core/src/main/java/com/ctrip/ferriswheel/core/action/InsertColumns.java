package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;

public class InsertColumns extends TableAction implements Action {
    private int columnIndex;
    private int nColumns;

    public InsertColumns() {
    }

    public InsertColumns(String sheetName, String tableName, int columnIndex, int nColumns) {
        super(sheetName, tableName);
        this.columnIndex = columnIndex;
        this.nColumns = nColumns;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getnColumns() {
        return nColumns;
    }

    public void setnColumns(int nColumns) {
        this.nColumns = nColumns;
    }
}
