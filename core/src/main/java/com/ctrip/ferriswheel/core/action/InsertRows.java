package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.intf.Action;

public class InsertRows extends TableAction implements Action {
    private int rowIndex;
    private int nRows;

    public InsertRows() {
    }

    public InsertRows(String sheetName, String tableName, int rowIndex, int nRows) {
        super(sheetName, tableName);
        this.rowIndex = rowIndex;
        this.nRows = nRows;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getnRows() {
        return nRows;
    }

    public void setnRows(int nRows) {
        this.nRows = nRows;
    }
}