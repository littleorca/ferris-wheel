package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.api.table.TableData;
import com.ctrip.ferriswheel.api.action.Action;

public final class AddTable extends SheetAction implements Action {
    private String tableName;
    private TableData tableData;

    public AddTable() {
    }

    public AddTable(String sheetName, String tableName, TableData tableData) {
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

    public TableData getTableData() {
        return tableData;
    }

    public void setTableData(TableData tableData) {
        this.tableData = tableData;
    }
}
