package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.view.Layout;

import java.io.Serializable;

public class TextData implements Serializable {
    private DynamicValue content;
    private Layout layout;

    public TextData() {
    }

    public TextData(DynamicValue content, Layout layout) {
        this.content = content;
        this.layout = layout;
    }

    public DynamicValue getContent() {
        return content;
    }

    public void setContent(DynamicValue content) {
        this.content = content;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }
}
