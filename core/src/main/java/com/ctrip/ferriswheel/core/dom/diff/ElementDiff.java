package com.ctrip.ferriswheel.core.dom.diff;

import java.util.Collections;
import java.util.Map;

public class ElementDiff extends Diff {
    private String tagName;
    private Map<String, String> negativeAttributes;
    private Map<String, String> positiveAttributes;

    public ElementDiff(String tagName, NodeLocation negative, NodeLocation positive) {
        this(tagName, negative, positive, null, null);
    }

    public ElementDiff(String tagName, NodeLocation negative, NodeLocation positive, Map<String, String> negativeAttributes, Map<String, String> positiveAttributes) {
        super(negative, positive);
        this.tagName = tagName;
        this.negativeAttributes = negativeAttributes;
        this.positiveAttributes = positiveAttributes;
    }

    @Override
    public boolean hasContent() {
        return hasAttributeChanges();
    }

    @Override
    protected void doMerge(Diff another) {
        if (!(another instanceof ElementDiff)) {
            throw new IllegalArgumentException("Cannot merge between different classes.");
        }
        ElementDiff anotherElementDiff = (ElementDiff) another;
        if (hasAttributeChanges()) {
            if (anotherElementDiff.hasAttributeChanges()) {
                throw new RuntimeException("Cannot merge attribute changes, this is probably a bug!");
            }
        } else if (anotherElementDiff.hasAttributeChanges()) {
            setNegativeAttributes(anotherElementDiff.negativeAttributes);
            setPositiveAttributes(anotherElementDiff.positiveAttributes);
        }
    }

    public boolean hasAttributeChanges() {
        return (negativeAttributes != null && !negativeAttributes.isEmpty()) ||
                (positiveAttributes != null && !positiveAttributes.isEmpty());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("# ").append(super.toString()).append("\n");
        if (negativeAttributes != null) {
            for (Map.Entry<String, String> negativeEntry : negativeAttributes.entrySet()) {
                sb.append("  - ").append(negativeEntry.getKey())
                        .append("=").append(negativeEntry.getValue())
                        .append("\n");
            }
        }
        if (positiveAttributes != null) {
            for (Map.Entry<String, String> positiveEntry : positiveAttributes.entrySet()) {
                sb.append("  + ").append(positiveEntry.getKey())
                        .append("=").append(positiveEntry.getValue())
                        .append("\n");
            }
        }
        return sb.toString();
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Map<String, String> getNegativeAttributes() {
        return negativeAttributes == null ? Collections.emptyMap() : negativeAttributes;
    }

    public void setNegativeAttributes(Map<String, String> negativeAttributes) {
        this.negativeAttributes = negativeAttributes;
    }

    public Map<String, String> getPositiveAttributes() {
        return positiveAttributes == null ? Collections.emptyMap() : positiveAttributes;
    }

    public void setPositiveAttributes(Map<String, String> positiveAttributes) {
        this.positiveAttributes = positiveAttributes;
    }
}
