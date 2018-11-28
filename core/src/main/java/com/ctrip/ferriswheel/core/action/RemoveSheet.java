package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.intf.Action;

public class RemoveSheet extends BaseAction implements Action {
    private String sheetName;

    public RemoveSheet() {
    }

    public RemoveSheet(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }
}
