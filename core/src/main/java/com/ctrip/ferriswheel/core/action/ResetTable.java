package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.table.Table;

/**
 * This action is only used to push table data after table has refreshed all data.
 */
public class ResetTable extends SheetAction {
    private Table table;

    public ResetTable() {
    }

    public ResetTable(String sheetName, Table table) {
        super(sheetName);
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
