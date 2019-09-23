package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.action.CellAction;

public class DefaultCell extends ValueNode implements Cell {
    private int columnIndex;
    private String format;
    private boolean fillUp;
    private boolean fillDown;
    private boolean fillLeft;
    private boolean fillRight;

    DefaultCell(AssetManager assetManager) {
        this(assetManager, Value.BLANK);
    }

    DefaultCell(AssetManager assetManager, Value value) {
        this(assetManager, value, null);
    }

    DefaultCell(AssetManager assetManager, Value value, String formulaString) {
        this(assetManager, value, formulaString, null);
    }

    DefaultCell(AssetManager assetManager, Value value, String formulaString, String format) {
        super(assetManager, value, formulaString);
        this.format = format;
    }


    @Override
    protected void doUpdateValue(Variant newValue) {
        DefaultTable table = getRow().getTable();
        CellAction.RefreshCellValue action = new CellAction.RefreshCellValue(
                table.getSheet().getName(),
                table.getName(),
                getRowIndex(),
                getColumnIndex(),
                Value.from(newValue));
        table.publicly(action, () -> setValue(newValue));
    }

    @Override
    public boolean isPhantom() {
        return getRow() != null && getRow().isPhantom();
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
