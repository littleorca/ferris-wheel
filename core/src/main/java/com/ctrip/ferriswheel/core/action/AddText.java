package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.bean.TextData;
import com.ctrip.ferriswheel.core.intf.Action;

public final class AddText extends SheetAction implements Action {
    private String textName;
    private TextData textData;

    public AddText() {
    }

    public AddText(String sheetName, String textName, TextData textData) {
        super(sheetName);
        this.textName = textName;
        this.textData = textData;
    }

    public String getTextName() {
        return textName;
    }

    public void setTextName(String textName) {
        this.textName = textName;
    }

    public TextData getTextData() {
        return textData;
    }

    public void setTextData(TextData textData) {
        this.textData = textData;
    }
}
