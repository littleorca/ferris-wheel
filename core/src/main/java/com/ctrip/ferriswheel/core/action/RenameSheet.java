package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.intf.Action;

public class RenameSheet extends BaseAction implements Action {
    private String oldSheetName;
    private String newSheetName;

    public RenameSheet() {
    }

    public RenameSheet(String oldSheetName, String newSheetName) {
        this.oldSheetName = oldSheetName;
        this.newSheetName = newSheetName;
    }

    @Override
    public String toString() {
        return "RenameSheet{" +
                "oldSheetName='" + oldSheetName + '\'' +
                ", newSheetName='" + newSheetName + '\'' +
                '}';
    }

    public String getOldSheetName() {
        return oldSheetName;
    }

    public void setOldSheetName(String oldSheetName) {
        this.oldSheetName = oldSheetName;
    }

    public String getNewSheetName() {
        return newSheetName;
    }

    public void setNewSheetName(String newSheetName) {
        this.newSheetName = newSheetName;
    }
}
