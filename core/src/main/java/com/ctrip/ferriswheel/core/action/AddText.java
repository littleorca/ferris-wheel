package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.text.Text;

public final class AddText extends SheetAction implements Action {
    private Text textData;

    public AddText() {
    }

    public AddText(String sheetName, Text textData) {
        super(sheetName);
        this.textData = textData;
    }

    public Text getTextData() {
        return textData;
    }

    public void setTextData(Text textData) {
        this.textData = textData;
    }
}
