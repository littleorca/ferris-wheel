package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.variant.Value;

public class DefaultCell extends ValueNode implements Cell {
    private int columnIndex;
    private String format;
    private boolean fillUp;
    private boolean fillDown;
    private boolean fillLeft;
    private boolean fillRight;

    DefaultCell(DefaultAssetManager assetManager) {
        this(assetManager, Value.BLANK);
    }

    DefaultCell(DefaultAssetManager assetManager, Value value) {
        this(assetManager, value, null);
    }

    DefaultCell(DefaultAssetManager assetManager, Value value, String formulaString) {
        this(assetManager, value, formulaString, null);
    }

    DefaultCell(DefaultAssetManager assetManager, Value value, String formulaString, String format) {
        super(assetManager, value, formulaString);
        this.format = format;
    }

    public DefaultRow getRow() {
        return (DefaultRow) getParent();
    }

    void setRow(DefaultRow row) {
        setParent(row);
    }

    public int getRowIndex() {
        return getRow().getRowIndex();
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    public String getFormat() {
        return format;
    }

    void setFormat(String format) {
        this.format = format;
    }

    @Override
    public boolean isFillUp() {
        return fillUp;
    }

    void setFillUp(boolean fillUp) {
        this.fillUp = fillUp;
    }

    @Override
    public boolean isFillDown() {
        return fillDown;
    }

    void setFillDown(boolean fillDown) {
        this.fillDown = fillDown;
    }

    @Override
    public boolean isFillLeft() {
        return fillLeft;
    }

    void setFillLeft(boolean fillLeft) {
        this.fillLeft = fillLeft;
    }

    @Override
    public boolean isFillRight() {
        return fillRight;
    }

    void setFillRight(boolean fillRight) {
        this.fillRight = fillRight;
    }

}
