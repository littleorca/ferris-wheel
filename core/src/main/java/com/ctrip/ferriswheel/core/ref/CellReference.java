package com.ctrip.ferriswheel.core.ref;

import com.ctrip.ferriswheel.core.asset.DefaultCell;

import java.io.Serializable;

public class CellReference extends HotAreaReference implements Serializable {
    private PositionRef positionRef;

    public CellReference() {
        super(null, null);
    }

    public CellReference(DefaultCell cell, boolean isRowAbsolute, boolean isColumnAbsolute) {
        this(cell.getRow().getTable().getSheet().getName(),
                cell.getRow().getTable().getName(),
                new PositionRef(cell.getRowIndex(), isRowAbsolute, cell.getColumnIndex(), isColumnAbsolute),
                true,
                false);
    }

    public CellReference(String sheetName,
                         String assetName,
                         PositionRef positionRef,
                         boolean alive,
                         boolean phantom) {
        super(sheetName, assetName, alive, phantom);
        this.positionRef = positionRef;
    }

    public PositionRef getPositionRef() {
        return positionRef;
    }

    public void setPositionRef(PositionRef positionRef) {
        this.positionRef = positionRef;
    }

}
