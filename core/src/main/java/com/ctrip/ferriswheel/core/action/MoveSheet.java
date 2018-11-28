package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.intf.Action;

public class MoveSheet extends BaseAction implements Action {
    private String sheetName;
    private int targetIndex;

    public MoveSheet() {
    }

    public MoveSheet(String sheetName, int targetIndex) {
        this.sheetName = sheetName;
        this.targetIndex = targetIndex;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public void setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
    }
}
