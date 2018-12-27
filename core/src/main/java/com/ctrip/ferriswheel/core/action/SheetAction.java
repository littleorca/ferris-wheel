package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.api.action.Action;

public abstract class SheetAction extends BaseAction implements Action {
    private String sheetName;

    SheetAction() {
    }

    SheetAction(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
}
