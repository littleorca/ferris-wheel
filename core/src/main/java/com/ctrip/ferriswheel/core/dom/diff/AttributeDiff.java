package com.ctrip.ferriswheel.core.dom.diff;

public class AttributeDiff {
    private boolean positive;
    private String name;
    private String value;

    public AttributeDiff() {
    }

    public AttributeDiff(boolean positive, String name, String value) {
        this.positive = positive;
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return (positive ? "+ " : "- ") + name + "=" + value;
    }

    public boolean isPositive() {
        return positive;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
