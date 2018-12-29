package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.api.table.Cell;
import com.ctrip.ferriswheel.api.table.Row;
import com.ctrip.ferriswheel.api.table.Table;
import com.ctrip.ferriswheel.core.util.TreeSparseArray;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

public class RowData implements Row, Serializable {
    private Table table;
    private int rowIndex;
    private TreeSparseArray<Cell> cells;

    public RowData() {
    }

    public RowData(int rowIndex, TreeSparseArray<Cell> cells) {
        this.rowIndex = rowIndex;
        this.cells = cells;
    }

    @Override
    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public TreeSparseArray<Cell> getCells() {
        return cells;
    }

    public void setCells(TreeSparseArray<Cell> cells) {
        this.cells = cells;
    }

    @Override
    public Cell getCell(int index) {
        return cells.get(index);
    }

    @Override
    public boolean isBlank() {
        if (cells == null || cells.isEmpty()) {
            return true;
        }
        for (Cell cell : cells.values()) {
            if (!cell.getData().isBlank()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int size() {
        return cells == null ? 0 : cells.size();
    }

    @Override
    public Iterator<Cell> iterator() {
        if (cells == null) {
            return Collections.emptyIterator();
        }
        return Collections.unmodifiableCollection(cells.values()).iterator();
    }
}
