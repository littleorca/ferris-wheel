package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.intf.Action;

public class AutomateTable extends TableAction implements Action {
    TableAutomatonInfo solution;

    public AutomateTable() {
    }

    public AutomateTable(String sheetName, String tableName, TableAutomatonInfo solution) {
        super(sheetName, tableName);
        this.solution = solution;
    }

    public TableAutomatonInfo getSolution() {
        return solution;
    }

    public void setSolution(TableAutomatonInfo solution) {
        this.solution = solution;
    }
}
