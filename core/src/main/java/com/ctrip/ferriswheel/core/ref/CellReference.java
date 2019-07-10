package com.ctrip.ferriswheel.core.ref;

import com.ctrip.ferriswheel.core.asset.Asset;
import com.ctrip.ferriswheel.core.asset.DefaultCell;

import java.io.Serializable;

public class CellReference extends AbstractReference implements Serializable {
    private PositionRef positionRef;
    private long cellId = Asset.UNSPECIFIED_ASSET_ID;
    private boolean alive = true;

    public CellReference() {
        super(null, null);
    }

    public CellReference(DefaultCell cell, boolean isRowAbsolute, boolean isColumnAbsolute) {
        this(cell.getRow().getTable().getSheet().getName(),
                cell.getRow().getTable().getName(),
                new PositionRef(cell.getRowIndex(), isRowAbsolute, cell.getColumnIndex(), isColumnAbsolute),
                cell.getAssetId(),
                true);
    }

    public CellReference(String sheetName,
                         String assetName,
                         PositionRef positionRef,
                         long cellId,
                         boolean alive) {
        super(sheetName, assetName);
        this.positionRef = positionRef;
        this.cellId = cellId;
        this.alive = alive;
    }

    public PositionRef getPositionRef() {
        return positionRef;
    }

    public void setPositionRef(PositionRef positionRef) {
        this.positionRef = positionRef;
    }

    public long getCellId() {
        return cellId;
    }

    public void setCellId(long cellId) {
        this.cellId = cellId;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isValid() {
        return alive && cellId != Asset.UNSPECIFIED_ASSET_ID;
    }
}
