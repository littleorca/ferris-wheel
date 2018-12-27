package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.api.action.Action;
import com.ctrip.ferriswheel.api.text.Text;

public class UpdateText extends TextAction implements Action {
    private Text textData;

    public UpdateText() {
    }

    public UpdateText(String sheetName, String textName, Text textData) {
        super(sheetName, textName);
        this.textData = textData;
    }

    public Text getTextData() {
        return textData;
    }

    public void setTextData(Text textData) {
        this.textData = textData;
    }
}
