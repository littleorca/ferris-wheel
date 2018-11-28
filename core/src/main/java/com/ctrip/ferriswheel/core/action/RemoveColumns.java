package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.intf.Action;

public class RemoveColumns extends TableAction implements Action {
    private int columnIndex;
    private int nColumns;

    public RemoveColumns() {
    }

    public RemoveColumns(String sheetName, String tableName, int columnIndex, int nColumns) {
        super(sheetName, tableName);
        this.columnIndex = columnIndex;
        this.nColumns = nColumns;
    }

    @Override
    public String toString() {
        return "RemoveColumns{" +
                "sheetName='" + getSheetName() + '\'' +
                ", tableName=" + getTableName() +
                ", columnIndex=" + columnIndex +
                ", nColumns=" + nColumns +
                '}';
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
