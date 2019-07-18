package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Row;
import com.ctrip.ferriswheel.core.util.TreeSparseArray;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class RowData implements Row, Serializable {
    private TreeSparseArray<Cell> cells;

    public RowData() {
    }

    public RowData(TreeSparseArray<Cell> cells) {
        this.cells = cells;
    }

    public TreeSparseArray<Cell> getCells() {
        return cells;
    }

    public void setCells(TreeSparseArray<Cell> cells) {
        this.cells = cells;
    }

    @Override
    public Cell getCell(int index) {
        if (cells == null) {
            return null;
        }
        return cells.get(index);
    }

    public void setCell(int columnIndex, Cell cell) {
        if (cells == null) {
            cells = new TreeSparseArray<>();
        }
        cells.set(columnIndex, cell);
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
    public int getCellCount() {
        return cells == null ? 0 : cells.size();
    }

    @Override
    public Iterator<Map.Entry<Integer, Cell>> iterator() {
        if (cells == null) {
            return Collections.emptyIterator();
        }
        return cells.iterator();
    }


    /**
     * Move cell from former position to new position. If a cell exists in the
     * target position, it will be discarded.
     *
     * @param from
     * @param to
     * @return
     */
    public Cell moveCell(int from, int to) {
        return cells.move(from, to);
    }

    public Cell removeCell(int index) {
        return cells.remove(index);
    }
}
