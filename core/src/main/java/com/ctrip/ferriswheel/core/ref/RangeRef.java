package com.ctrip.ferriswheel.core.ref;

import java.io.Serializable;

public class RangeRef implements Serializable {
    private CellRef upperLeft;
    private CellRef lowerRight;

    public RangeRef() {
    }

    public RangeRef(CellRef upperLeft, CellRef lowerRight) {
        this.upperLeft = upperLeft;
        this.lowerRight = lowerRight;
        if (lowerRight.getSheetName() == null) {
            lowerRight.setSheetName(upperLeft.getSheetName());
        } else if (!lowerRight.getSheetName().equals(upperLeft.getSheetName())) {
            throw new IllegalArgumentException();
        }
        if (lowerRight.getTableName() == null) {
            lowerRight.setTableName(upperLeft.getTableName());
        } else if (!lowerRight.getTableName().equals(upperLeft.getTableName())) {
            throw new IllegalArgumentException();
        }
    }

    public RangeRef(String sheetName,
                    String tableName,
                    int left,
                    int top,
                    int right,
                    int bottom,
                    long upperLeftCellId,
                    long lowerRightCellId) {
        this.upperLeft = new CellRef(sheetName, tableName, top, true, left, true, upperLeftCellId);
        this.lowerRight = new CellRef(sheetName, tableName, bottom, true, right, true, lowerRightCellId);
    }

    public RangeRef(String sheetName,
                    String tableName,
                    int left, boolean isLeftAbsolute,
                    int top, boolean isTopAbsolute,
                    int right, boolean isRightAbsolute,
                    int bottom, boolean isBottomAbsolute,
                    long upperLeftCellId,
                    long lowerRightCellId) {
        this.upperLeft = new CellRef(sheetName, tableName, top, isTopAbsolute, left, isLeftAbsolute, upperLeftCellId);
        this.lowerRight = new CellRef(sheetName, tableName, bottom, isBottomAbsolute, right, isRightAbsolute, lowerRightCellId);
    }

    @Override
    public String toString() {
        return (tableName() == null ? "" :
                (sheetName() == null ? tableName() :
                        sheetName() + "!" + tableName()) + "!")
                + "R" + getTop() + "C" + getLeft()
                + ":R" + getBottom() + "C" + getRight();
    }

    public boolean isValid() {
        return upperLeft.isValid() && lowerRight.isValid();
    }

    public String sheetName() {
        return upperLeft.getSheetName();
    }

    public String tableName() {
        return upperLeft.getTableName();
    }

    public int width() {
        return getRight() + 1 - getLeft();
    }

    public int height() {
        return getBottom() + 1 - getTop();
    }

    public int getLeft() {
        return upperLeft.getColumnIndex();
    }

    public boolean isLeftAbsolute() {
        return upperLeft.isColumnAbsolute();
    }

    public int getTop() {
        return upperLeft.getRowIndex();
    }

    public boolean isTopAbsolute() {
        return upperLeft.isRowAbsolute();
    }

    public int getRight() {
        return lowerRight.getColumnIndex();
    }

    public boolean isRightAbsolute() {
        return lowerRight.isColumnAbsolute();
    }

    public int getBottom() {
        return lowerRight.getRowIndex();
    }

    public boolean isBottomAbsolute() {
        return lowerRight.isRowAbsolute();
    }

    public long getUpperLeftCellId() {
        return upperLeft.getCellId();
    }

    public long getLowerRightCellId() {
        return lowerRight.getCellId();
    }

    public boolean contains(int row, int column) {
        return row >= getTop() && row <= getBottom() && column >= getLeft() && column <= getRight();
    }

    public CellRef getUpperLeft() {
        return upperLeft;
    }

    public void setUpperLeft(CellRef upperLeft) {
        this.upperLeft = upperLeft;
    }

    public CellRef getLowerRight() {
        return lowerRight;
    }

    public void setLowerRight(CellRef lowerRight) {
        this.lowerRight = lowerRight;
    }
}
