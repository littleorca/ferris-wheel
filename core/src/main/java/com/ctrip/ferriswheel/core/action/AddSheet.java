package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;

public final class AddSheet extends BaseAction implements Action {
    private String sheetName;
    private int index;

    public AddSheet() {
    }

    public AddSheet(String sheetName, int index) {
        this.sheetName = sheetName;
        this.index = index;
    }

    @Override
    public String toString() {
        return "AddSheet{" +
                "sheetName='" + sheetName + '\'' +
                ", index=" + index +
                '}';
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
