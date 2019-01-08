package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.table.AutomateConfiguration;

public class AutomateTable extends TableAction implements Action {
    AutomateConfiguration solution;

    public AutomateTable() {
    }

    public AutomateTable(String sheetName, String tableName, AutomateConfiguration solution) {
        super(sheetName, tableName);
        this.solution = solution;
    }

    public AutomateConfiguration getSolution() {
        return solution;
    }

    public void setSolution(AutomateConfiguration solution) {
        this.solution = solution;
    }
}
