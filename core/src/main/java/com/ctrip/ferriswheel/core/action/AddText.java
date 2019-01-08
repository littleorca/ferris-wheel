package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.text.Text;
import com.ctrip.ferriswheel.core.bean.TextData;

public final class AddText extends SheetAction implements Action {
    private String textName;
    private Text textData;

    public AddText() {
    }

    public AddText(String sheetName, String textName, Text textData) {
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

    public Text getTextData() {
        return textData;
    }

    public void setTextData(TextData textData) {
        this.textData = textData;
    }
}
