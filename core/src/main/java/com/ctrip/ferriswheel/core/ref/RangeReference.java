package com.ctrip.ferriswheel.core.ref;

import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.core.asset.Asset;

import java.io.Serializable;

public class RangeReference extends AbstractReference implements TableRange, Serializable {
    private PositionRef upperLeftRef;
    private PositionRef lowerRightRef;

    private long upperLeftTargetId = Asset.UNSPECIFIED_ASSET_ID;
    private long lowerRightTargetId = Asset.UNSPECIFIED_ASSET_ID;

    public RangeReference() {
        super(null, null);
    }

    public RangeReference(CellReference upperLeft, CellReference lowerRight) {
        super(upperLeft.getSheetName(), upperLeft.getAssetName(), upperLeft.isAlive() && lowerRight.isAlive(), false);
        if (!lowerRight.getSheetName().equals(upperLeft.getSheetName())
                || !lowerRight.getAssetName().equals(upperLeft.getAssetName())) {
            throw new IllegalArgumentException();
        }
        this.upperLeftRef = upperLeft.getPositionRef();
        this.lowerRightRef = lowerRight.getPositionRef();
        this.upperLeftTargetId = upperLeft.getCellId();
        this.lowerRightTargetId = lowerRight.getCellId();
    }

    public RangeReference(String sheetName, String assetName, PositionRef upperLeftPos, PositionRef lowerRightPos) {
        super(sheetName, assetName);
        this.upperLeftRef = upperLeftPos;
        this.lowerRightRef = lowerRightPos;
    }

    public RangeReference(String sheetName,
                          String assetName,
                          int left,
                          int top,
                          int right,
                          int bottom,
                          long upperLeftCellId,
                          long lowerRightCellId) {
        this(sheetName,
                assetName,
                left, true,
                top, true,
                right, true,
                bottom, true,
                upperLeftCellId,
                lowerRightCellId);
    }

    public RangeReference(String sheetName,
                          String tableName,
                          int left, boolean isLeftAbsolute,
                          int top, boolean isTopAbsolute,
                          int right, boolean isRightAbsolute,
                          int bottom, boolean isBottomAbsolute,
                          long upperLeftCellId,
                          long lowerRightCellId) {
        this(sheetName,
                tableName,
                new PositionRef(top, isTopAbsolute, left, isLeftAbsolute),
                new PositionRef(bottom, isBottomAbsolute, right, isRightAbsolute),
                upperLeftCellId,
                lowerRightCellId,
                true,
                false);
    }

    public RangeReference(String sheetName,
                          String assetName,
                          PositionRef upperLeftRef,
                          PositionRef lowerRightRef,
                          long upperLeftTargetId,
                          long lowerRightTargetId,
                          boolean alive,
                          boolean phantom) {
        super(sheetName, assetName, alive, phantom);
        this.upperLeftRef = upperLeftRef;
        this.lowerRightRef = lowerRightRef;
        this.upperLeftTargetId = upperLeftTargetId;
        this.lowerRightTargetId = lowerRightTargetId;
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

        final int expectedTop = getTop() == -1 ? 0 : getTop();
        final int expectedBottom = getBottom() == -1 ? rowCount - 1 : getBottom();
        final int expectedLeft = getLeft() == -1 ? 0 : getLeft();
        final int expectedRight = getRight() == -1 ? columnCount - 1 : getRight();

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
    public int getTop() {
        return upperLeftRef.getRowIndex();
    }

    @Override
    public int getLeft() {
        return upperLeftRef.getColumnIndex();
    }

    @Override
    public int getBottom() {
        return lowerRightRef.getRowIndex();
    }

    @Override
    public int getRight() {
        return lowerRightRef.getColumnIndex();
    }

    @Override
    public int width() {
        return getRight() + 1 - getLeft();
    }

    @Override
    public int height() {
        return getBottom() + 1 - getTop();
    }

    public PositionRef getUpperLeftRef() {
        return upperLeftRef;
    }

    public void setUpperLeftRef(PositionRef upperLeftRef) {
        this.upperLeftRef = upperLeftRef;
    }

    public PositionRef getLowerRightRef() {
        return lowerRightRef;
    }

    public void setLowerRightRef(PositionRef lowerRightRef) {
        this.lowerRightRef = lowerRightRef;
    }

    public long getUpperLeftTargetId() {
        return upperLeftTargetId;
    }

    public void setUpperLeftTargetId(long upperLeftTargetId) {
        this.upperLeftTargetId = upperLeftTargetId;
    }

    public long getLowerRightTargetId() {
        return lowerRightTargetId;
    }

    public void setLowerRightTargetId(long lowerRightTargetId) {
        this.lowerRightTargetId = lowerRightTargetId;
    }

    public boolean isValid() {
        return isAlive() &&
                upperLeftTargetId != Asset.UNSPECIFIED_ASSET_ID &&
                lowerRightTargetId != Asset.UNSPECIFIED_ASSET_ID;
    }

}
