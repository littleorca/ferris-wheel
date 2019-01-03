package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.api.table.Cell;
import com.ctrip.ferriswheel.api.variant.DynamicVariant;

import java.io.Serializable;

public class CellData implements Cell, Serializable {
    private DynamicVariant data;
    private boolean fillLeft;
    private boolean fillUp;
    private boolean fillRight;
    private boolean fillDown;

    public CellData() {
    }

    public CellData(DynamicVariant data) {
        this.data = data;
    }

    public void erase() {
        setData(new DynamicVariantImpl(Value.BLANK));
    }

    @Override
    public DynamicVariant getData() {
        return data;
    }

    public void setData(DynamicVariant data) {
        this.data = data;
    }

    @Override
    public boolean isFillLeft() {
        return fillLeft;
    }

    public void setFillLeft(boolean fillLeft) {
        this.fillLeft = fillLeft;
    }

    @Override
    public boolean isFillUp() {
        return fillUp;
    }

    public void setFillUp(boolean fillUp) {
        this.fillUp = fillUp;
    }

    @Override
    public boolean isFillRight() {
        return fillRight;
    }

    public void setFillRight(boolean fillRight) {
        this.fillRight = fillRight;
    }

    @Override
    public boolean isFillDown() {
        return fillDown;
    }

    public void setFillDown(boolean fillDown) {
        this.fillDown = fillDown;
    }
}
