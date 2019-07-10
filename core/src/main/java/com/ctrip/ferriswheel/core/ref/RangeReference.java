package com.ctrip.ferriswheel.core.ref;

import com.ctrip.ferriswheel.core.asset.Asset;

import java.io.Serializable;

public class RangeReference extends AbstractReference implements Serializable {
    private PositionRef upperLeftRef;
    private PositionRef lowerRightRef;

    private long upperLeftTargetId = Asset.UNSPECIFIED_ASSET_ID;
    private long lowerRightTargetId = Asset.UNSPECIFIED_ASSET_ID;
    private boolean alive = true;

    public RangeReference() {
        super(null, null);
    }

    public RangeReference(CellReference upperLeft, CellReference lowerRight) {
        super(upperLeft.getSheetName(), upperLeft.getAssetName());
        if (!lowerRight.getSheetName().equals(upperLeft.getSheetName())
                || !lowerRight.getAssetName().equals(upperLeft.getAssetName())) {
            throw new IllegalArgumentException();
        }
        this.upperLeftRef = upperLeft.getPositionRef();
        this.lowerRightRef = lowerRight.getPositionRef();
        this.upperLeftTargetId = upperLeft.getCellId();
        this.lowerRightTargetId = lowerRight.getCellId();
        this.alive = upperLeft.isAlive() && lowerRight.isAlive();
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
                true);
    }

    public RangeReference(String sheetName,
                          String assetName,
                          PositionRef upperLeftRef,
                          PositionRef lowerRightRef,
                          long upperLeftTargetId,
                          long lowerRightTargetId,
                          boolean alive) {
        super(sheetName, assetName);
        this.upperLeftRef = upperLeftRef;
        this.lowerRightRef = lowerRightRef;
        this.upperLeftTargetId = upperLeftTargetId;
        this.lowerRightTargetId = lowerRightTargetId;
        this.alive = alive;
    }

    @Override
    public String toString() {
        return (getAssetName() == null ? "" :
                (getSheetName() == null ? getAssetName() :
                        getSheetName() + "!" + getAssetName()) + "!")
                + "R" + getTop() + "C" + getLeft()
                + ":R" + getBottom() + "C" + getRight();
    }

    public int getTop() {
        return upperLeftRef.getRowIndex();
    }

    public int getLeft() {
        return upperLeftRef.getColumnIndex();
    }

    public int getBottom() {
        return lowerRightRef.getRowIndex();
    }

    public int getRight() {
        return lowerRightRef.getColumnIndex();
    }

    public int width() {
        return getRight() + 1 - getLeft();
    }

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

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isValid() {
        return isAlive() &&
                upperLeftTargetId != Asset.UNSPECIFIED_ASSET_ID &&
                lowerRightTargetId != Asset.UNSPECIFIED_ASSET_ID;
    }
}
