package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.api.table.DryCellData;

import java.io.Serializable;

public class CellData implements DryCellData, Serializable {
    private int index;
    private DynamicValue value;

    public CellData() {
    }

    public CellData(int index, DynamicValue value) {
        this.index = index;
        this.value = value;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public DynamicValue getValue() {
        return value;
    }

    public void setValue(DynamicValue value) {
        this.value = value;
    }
}
