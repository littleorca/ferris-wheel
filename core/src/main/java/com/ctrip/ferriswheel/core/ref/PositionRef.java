package com.ctrip.ferriswheel.core.ref;

import java.io.Serializable;

public class PositionRef implements Serializable {
    private Anchor rowAnchor;
    private Anchor columnAnchor;

    public PositionRef() {
    }

    public PositionRef(PositionRef another) {
        this(another.getRowAnchor(), another.getColumnAnchor());
    }

    public PositionRef(int rowIndex, int columnIndex) {
        this(new Anchor(rowIndex), new Anchor(columnIndex));
    }

    public PositionRef(int rowIndex, boolean isRowAbsolute, int columnIndex, boolean isColumnAbsolute) {
        this(new Anchor(rowIndex, isRowAbsolute), new Anchor(columnIndex, isColumnAbsolute));
    }

    public PositionRef(Anchor rowAnchor, Anchor columnAnchor) {
        this.rowAnchor = rowAnchor;
        this.columnAnchor = columnAnchor;
    }

    public PositionRef relativeShift(int nShiftRows, int nShiftColumns) {
        return new PositionRef(
                rowAnchor.relativeShift(nShiftRows),
                columnAnchor.relativeShift(nShiftColumns)
        );
    }

    public int getRowIndex() {
        return rowAnchor.getIndex();
    }

    public void setRowIndex(int rowIndex) {
        rowAnchor.setIndex(rowIndex);
    }

    public boolean isRowAbsolute() {
        return rowAnchor.isAbsolute();
    }

    public void setRowAbsolute(boolean rowAbsolute) {
        rowAnchor.setAbsolute(rowAbsolute);
    }

    public int getColumnIndex() {
        return columnAnchor.getIndex();
    }

    public void setColumnIndex(int columnIndex) {
        columnAnchor.setIndex(columnIndex);
    }

    public boolean isColumnAbsolute() {
        return columnAnchor.isAbsolute();
    }

    public void setColumnAbsolute(boolean columnAbsolute) {
        columnAnchor.setAbsolute(columnAbsolute);
    }

    public Anchor getRowAnchor() {
        return rowAnchor;
    }

    public void setRowAnchor(Anchor rowAnchor) {
        this.rowAnchor = rowAnchor;
    }

    public Anchor getColumnAnchor() {
        return columnAnchor;
    }

    public void setColumnAnchor(Anchor columnAnchor) {
        this.columnAnchor = columnAnchor;
    }
}
