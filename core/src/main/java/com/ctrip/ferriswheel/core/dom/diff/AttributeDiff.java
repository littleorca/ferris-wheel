package com.ctrip.ferriswheel.core.dom.diff;

public class AttributeDiff {
    private boolean positive;
    private String name;
    private String value;

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
