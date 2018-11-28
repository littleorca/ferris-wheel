package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.util.SparseArray;

import java.io.Serializable;

public class RowData implements Serializable {
    private int index;
    private SparseArray<CellData> cells;

    public RowData() {
    }

    public RowData(int index, SparseArray<CellData> cells) {
        this.index = index;
        this.cells = cells;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public SparseArray<CellData> getCells() {
        return cells;
    }

    public void setCells(SparseArray<CellData> cells) {
        this.cells = cells;
    }
}
