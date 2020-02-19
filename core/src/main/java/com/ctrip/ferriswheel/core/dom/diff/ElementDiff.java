package com.ctrip.ferriswheel.core.dom.diff;

import java.util.List;

public class ElementDiff {
    private List<AttributeDiff> attributes;
    private TextDiff text;
    private List<ChildDiff> children;

    public List<AttributeDiff> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDiff> attributes) {
        this.attributes = attributes;
    }

    public TextDiff getText() {
        return text;
    }

    public void setText(TextDiff text) {
        this.text = text;
    }

    public List<ChildDiff> getChildren() {
        return children;
    }

    public void setChildren(List<ChildDiff> children) {
        this.children = children;
    }
}
