package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.api.table.DryCellData;
import com.ctrip.ferriswheel.api.table.DryRowData;
import com.ctrip.ferriswheel.core.util.TreeSparseArray;

import java.io.Serializable;

public class RowData implements DryRowData, Serializable {
    private int index;
    private TreeSparseArray<DryCellData> cells;

    public RowData() {
    }

    public RowData(int index, TreeSparseArray<DryCellData> cells) {
        this.index = index;
        this.cells = cells;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public TreeSparseArray<DryCellData> getCells() {
        return cells;
    }

    public void setCells(TreeSparseArray<DryCellData> cells) {
        this.cells = cells;
    }
}
