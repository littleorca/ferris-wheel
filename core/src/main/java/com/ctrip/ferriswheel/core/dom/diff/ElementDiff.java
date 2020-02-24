package com.ctrip.ferriswheel.core.dom.diff;

import java.util.Collections;
import java.util.List;

public class ElementDiff extends Diff {
    private List<AttributeDiff> attributes = Collections.emptyList();

    public ElementDiff() {
    }

    public ElementDiff(NodeLocation negative, NodeLocation positive) {
        super(negative, positive);
    }

    public ElementDiff(NodeLocation negative, NodeLocation positive, List<AttributeDiff> attributes) {
        super(negative, positive);
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        if (attributes == null || attributes.isEmpty())
            return super.toString();

        StringBuilder sb = new StringBuilder(super.toString());
        for (AttributeDiff attrDiff : attributes) {
            sb.append(attrDiff).append("\n");
        }
        return sb.toString();
    }

    public List<AttributeDiff> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeDiff> attributes) {
        this.attributes = attributes;
    }

}
