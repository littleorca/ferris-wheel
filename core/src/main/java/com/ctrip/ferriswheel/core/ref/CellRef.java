package com.ctrip.ferriswheel.core.ref;

import com.ctrip.ferriswheel.core.intf.Asset;
import com.ctrip.ferriswheel.core.intf.Cell;

import java.io.Serializable;

public class CellRef extends PositionRef implements Serializable {
    private String sheetName;
    private String tableName;
    private long cellId = Asset.UNSPECIFIED_ASSET_ID;
    private boolean valid = true;

    public CellRef() {
    }

    public CellRef(Cell cell, boolean isRowAbsolute, boolean isColumnAbsolute) {
        this(cell.getRow().getTable().getSheet().getName(),
                cell.getRow().getTable().getName(),
                cell.getRowIndex(),
                isRowAbsolute,
                cell.getColumnIndex(),
                isColumnAbsolute,
                cell.getAssetId());
    }

    public CellRef(CellRef another) {
        this(another.getSheetName(),
                another.getTableName(),
                another.getRowIndex(),
                another.isRowAbsolute(),
                another.getColumnIndex(),
                another.isColumnAbsolute(),
                another.getCellId());
    }

    public CellRef(String sheetName,
                   String tableName,
                   int rowIndex,
                   boolean isRowAbsolute,
                   int columnIndex,
                   boolean isColumnAbsolute,
                   long cellId) {
        super(rowIndex, isRowAbsolute, columnIndex, isColumnAbsolute);
        this.sheetName = sheetName;
        this.tableName = tableName;
        this.cellId = cellId;
    }

    @Override
    public String toString() {
        return (getTableName() == null ? "" :
                (getSheetName() == null ? getTableName() :
                        getSheetName() + "!" + getTableName()) + "!")
                + "R" + getRowIndex() + "C" + getColumnIndex();
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public long getCellId() {
        return cellId;
    }

    public void setCellId(long cellId) {
        this.cellId = cellId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
