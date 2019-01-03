package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.api.action.Action;
import com.ctrip.ferriswheel.api.table.Table;

public final class AddTable extends SheetAction implements Action {
    private String tableName;
    private Table tableData;

    public AddTable() {
    }

    public AddTable(String sheetName, String tableName, Table tableData) {
        super(sheetName);
        this.tableName = tableName;
        this.tableData = tableData;
    }

    @Override
    public String toString() {
        return "AddTable{" +
                "sheetName='" + getSheetName() + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableData='" + tableData + '\'' +
                '}';
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Table getTableData() {
        return tableData;
    }

    public void setTableData(Table tableData) {
        this.tableData = tableData;
    }
}
