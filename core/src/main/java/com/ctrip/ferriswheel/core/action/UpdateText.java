package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.bean.TextData;
import com.ctrip.ferriswheel.core.intf.Action;

public class UpdateText extends TextAction implements Action {
    private TextData textData;

    public UpdateText() {
    }

    public UpdateText(String sheetName, String textName, TextData textData) {
        super(sheetName, textName);
        this.textData = textData;
    }

    public TextData getTextData() {
        return textData;
    }

    public void setTextData(TextData textData) {
        this.textData = textData;
    }
}
