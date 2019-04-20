package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.table.Table;

public final class AddTable extends SheetAction implements Action {
    private Table tableData;

    public AddTable() {
    }

    public AddTable(String sheetName, Table tableData) {
        super(sheetName);
        this.tableData = tableData;
    }

    @Override
    public String toString() {
        return "AddTable{" +
                "sheetName='" + getSheetName() + '\'' +
                ", tableData='" + tableData + '\'' +
                '}';
    }

    public Table getTableData() {
        return tableData;
    }

    public void setTableData(Table tableData) {
        this.tableData = tableData;
    }
}
