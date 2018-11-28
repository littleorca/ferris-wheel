package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.intf.Action;

public abstract class TextAction extends SheetAction implements Action {
    private String textName;

    TextAction() {
    }

    TextAction(String sheetName, String textName) {
        super(sheetName);
        this.textName = textName;
    }

    public String getTextName() {
        return textName;
    }

    public void setTextName(String textName) {
        this.textName = textName;
    }
}
