package com.ctrip.ferriswheel.core.ref;

import java.io.Serializable;

public class PositionRef implements Serializable {
    private int rowIndex;
    private boolean isRowAbsolute = false;
    private int columnIndex;
    private boolean isColumnAbsolute = false;

    public PositionRef() {
    }

    public PositionRef(PositionRef another) {
        this(another.getRowIndex(), another.isRowAbsolute(), another.getColumnIndex(), another.isColumnAbsolute());
    }

    public PositionRef(int rowIndex, boolean isRowAbsolute, int columnIndex, boolean isColumnAbsolute) {
        this.rowIndex = rowIndex;
        this.isRowAbsolute = isRowAbsolute;
        this.columnIndex = columnIndex;
        this.isColumnAbsolute = isColumnAbsolute;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public boolean isRowAbsolute() {
        return isRowAbsolute;
    }

    public void setRowAbsolute(boolean rowAbsolute) {
        isRowAbsolute = rowAbsolute;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public boolean isColumnAbsolute() {
        return isColumnAbsolute;
    }

    public void setColumnAbsolute(boolean columnAbsolute) {
        isColumnAbsolute = columnAbsolute;
    }
}
