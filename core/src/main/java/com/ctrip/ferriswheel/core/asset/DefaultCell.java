package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.intf.Cell;
import com.ctrip.ferriswheel.core.view.CellStyle;

class DefaultCell extends ValueNode implements Cell {
    private int columnIndex;
    private CellStyle style;
    private boolean fillUp;
    private boolean fillDown;
    private boolean fillLeft;
    private boolean fillRight;

    DefaultCell(DefaultAssetManager assetManager) {
        this(assetManager, Value.BLANK);
    }

    DefaultCell(DefaultAssetManager assetManager, Value value) {
        this(assetManager, value, null, null);
    }

    DefaultCell(DefaultAssetManager assetManager, Value value, String formulaString, CellStyle style) {
        super(assetManager, value, formulaString);
        this.style = style;
    }

    @Override
    public DefaultRow getRow() {
        return (DefaultRow) getParent();
    }

    void setRow(DefaultRow row) {
        setParent(row);
    }

    @Override
    public int getRowIndex() {
        return getRow().getRowIndex();
    }

    @Override
    public int getColumnIndex() {
        return columnIndex;
    }

    void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    public CellStyle getStyle() {
        return style;
    }

    void setStyle(CellStyle style) {
        this.style = style;
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
