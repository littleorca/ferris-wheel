package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.api.action.Action;
import com.ctrip.ferriswheel.api.table.AutomateSolution;

public class AutomateTable extends TableAction implements Action {
    AutomateSolution solution;

    public AutomateTable() {
    }

    public AutomateTable(String sheetName, String tableName, AutomateSolution solution) {
        super(sheetName, tableName);
        this.solution = solution;
    }

    public AutomateSolution getSolution() {
        return solution;
    }

    public void setSolution(AutomateSolution solution) {
        this.solution = solution;
    }
}
