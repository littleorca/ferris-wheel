package com.ctrip.ferriswheel.core.ref;

import com.ctrip.ferriswheel.common.table.Table;

import java.io.Serializable;

public class RangeReference extends HotAreaReference implements TableRange, Serializable {
    private Anchor leftAnchor;
    private Anchor topAnchor;
    private Anchor rightAnchor;
    private Anchor bottomAnchor;

    public RangeReference() {
        super(null, null);
    }

    public RangeReference(CellReference upperLeft, CellReference lowerRight) {
        super(upperLeft.getSheetName(), upperLeft.getAssetName(), upperLeft.isAlive() && lowerRight.isAlive(), false);
        if (!lowerRight.getSheetName().equals(upperLeft.getSheetName())
                || !lowerRight.getAssetName().equals(upperLeft.getAssetName())) {
            throw new IllegalArgumentException();
        }
        this.leftAnchor = new Anchor(upperLeft.getPositionRef().getColumnAnchor());
        this.topAnchor = new Anchor(upperLeft.getPositionRef().getRowAnchor());
        this.rightAnchor = new Anchor(lowerRight.getPositionRef().getColumnAnchor());
        this.bottomAnchor = new Anchor(lowerRight.getPositionRef().getRowAnchor());
    }

    public RangeReference(String sheetName,
                          String assetName,
                          Integer left,
                          Integer top,
                          Integer right,
                          Integer bottom) {
        super(sheetName, assetName);
        if (left != null) {
            this.leftAnchor = new Anchor(left);
        }
        if (top != null) {
            this.topAnchor = new Anchor(top);
        }
        if (right != null) {
            this.rightAnchor = new Anchor(right);
        }
        if (bottom != null) {
            this.bottomAnchor = new Anchor(bottom);
        }
    }

    public RangeReference(String sheetName,
                          String assetName,
                          Anchor leftAnchor,
                          Anchor topAnchor,
                          Anchor rightAnchor,
                          Anchor bottomAnchor) {
        super(sheetName, assetName);
        this.leftAnchor = leftAnchor == null ? null : new Anchor(leftAnchor);
        this.topAnchor = topAnchor == null ? null : new Anchor(topAnchor);
        this.rightAnchor = rightAnchor == null ? null : new Anchor(rightAnchor);
        this.bottomAnchor = bottomAnchor == null ? null : new Anchor(bottomAnchor);
    }


    public RangeReference relativeShift(int nShiftRows, int nShiftCols) {
        Anchor shiftedLeftAnchor = leftAnchor == null ? null : leftAnchor.relativeShift(nShiftCols);
        Anchor shiftedTopAnchor = topAnchor == null ? null : topAnchor.relativeShift(nShiftRows);
        Anchor shiftedRightAnchor = rightAnchor == null ? null : rightAnchor.relativeShift(nShiftCols);
        Anchor shiftedBottomAnchor = bottomAnchor == null ? null : bottomAnchor.relativeShift(nShiftRows);

        return new RangeReference(getSheetName(), getAssetName(),
                shiftedLeftAnchor, shiftedTopAnchor, shiftedRightAnchor, shiftedBottomAnchor);
    }

    @Override
    public String toString() {
        return (getAssetName() == null ? "" :
                (getSheetName() == null ? getAssetName() :
                        getSheetName() + "!" + getAssetName()) + "!")
                + "R" + getTop() + "C" + getLeft()
                + ":R" + getBottom() + "C" + getRight();
    }


    public TableRange getOverlappedRectangle(Table table) {
        final int rowCount = table.getRowCount();
        final int columnCount = table.getColumnCount();

        final int expectedTop = getTop() == null ? 0 : getTop();
        final int expectedBottom = getBottom() == null ? rowCount - 1 : getBottom();
        final int expectedLeft = getLeft() == null ? 0 : getLeft();
        final int expectedRight = getRight() == null ? columnCount - 1 : getRight();

        if (expectedTop >= rowCount || expectedLeft >= columnCount) {
            return null;
        }

        final int top = expectedTop;
        final int left = expectedLeft;
        final int bottom = Math.min(expectedBottom, rowCount - 1);
        final int right = Math.min(expectedRight, columnCount - 1);

        if (bottom < top || right < left) {
            return null;
        }

        return new SimpleTableRange(left, top, right, bottom);
    }

    @Override
    public Integer getLeft() {
        return leftAnchor == null ? null : leftAnchor.getIndex();
    }

    @Override
    public Integer getTop() {
        return topAnchor == null ? null : topAnchor.getIndex();
    }

    @Override
    public Integer getRight() {
        return rightAnchor == null ? null : rightAnchor.getIndex();
    }

    @Override
    public Integer getBottom() {
        return bottomAnchor == null ? null : bottomAnchor.getIndex();
    }

    public Anchor getLeftAnchor() {
        return leftAnchor;
    }

    public void setLeftAnchor(Anchor leftAnchor) {
        this.leftAnchor = leftAnchor;
    }

    public Anchor getTopAnchor() {
        return topAnchor;
    }

    public void setTopAnchor(Anchor topAnchor) {
        this.topAnchor = topAnchor;
    }

    public Anchor getRightAnchor() {
        return rightAnchor;
    }

    public void setRightAnchor(Anchor rightAnchor) {
        this.rightAnchor = rightAnchor;
    }

    public Anchor getBottomAnchor() {
        return bottomAnchor;
    }

    public void setBottomAnchor(Anchor bottomAnchor) {
        this.bottomAnchor = bottomAnchor;
    }

}
