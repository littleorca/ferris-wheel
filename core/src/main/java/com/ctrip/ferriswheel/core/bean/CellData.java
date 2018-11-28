package com.ctrip.ferriswheel.core.bean;

import java.io.Serializable;

public class CellData implements Serializable {
    private int index;
    private DynamicValue value;

    public CellData() {
    }

    public CellData(int index, DynamicValue value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public DynamicValue getValue() {
        return value;
    }

    public void setValue(DynamicValue value) {
        this.value = value;
    }
}
