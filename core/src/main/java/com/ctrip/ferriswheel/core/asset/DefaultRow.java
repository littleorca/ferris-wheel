package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.api.table.Cell;
import com.ctrip.ferriswheel.api.table.Row;
import com.ctrip.ferriswheel.core.util.UnmodifiableIterator;

import java.util.Iterator;

public class DefaultRow extends AssetNode implements Row {
    private int rowIndex;
    private final SparseAssetArray<DefaultCell> cells;

    DefaultRow(AssetManager assetManager) {
        super(assetManager);
        this.cells = new SparseAssetArray<>(this);
    }

    @Override
    public DefaultCell getCell(int index) {
        return cells.get(index);
    }

    /**
     * Set cell at the specified position, register it as a child. If a cell
     * at the specified position already exists, it will be dismissed.
     *
     * @param index
     * @param cell
     * @return
     */
    DefaultCell setCell(int index, DefaultCell cell) {
        DefaultCell oldCell = removeCell(index);
        if (cell == null) {
            return oldCell;
        }
        cell.setRow(this);
        cell.setColumnIndex(index);
        cells.set(index, cell);
        return oldCell;
    }

    /**
     * Remove cell at the specified position.
     *
     * @param index
     * @return
     */
    DefaultCell removeCell(int index) {
        return cells.remove(index);
    }

    /**
     * Move cell from former position to new position. If a cell exists in the
     * target position, it will be discarded.
     *
     * @param from
     * @param to
     * @return
     */
    DefaultCell moveCell(int from, int to) {
        DefaultCell cell = cells.get(from);
        DefaultCell oldCell = cells.move(from, to);
        if (cell != null) {
            cell.setColumnIndex(to);
        }
        return oldCell;
    }

    @Override
    public DefaultTable getTable() {
        return (DefaultTable) getParent();
    }

    void setSheet(DefaultTable sheet) {
        setParent(sheet);
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    @Override
    public boolean isBlank() {
        return size() == 0;
    }

    @Override
    public int size() {
        for (int i = cells.size() - 1; i >= 0; i--) {
            Cell cell = cells.get(i);
            if (cell != null && (!cell.isBlank() || cell.isFormula())) {
                return i + 1;
            }
        }
        return 0;
    }

    @Override
    public Iterator<Cell> iterator() {
        return new UnmodifiableIterator(cells.iterator());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }

    public void toString(StringBuilder sb) {
        // first line: value
        sb.append(String.format(" %4d |", getRowIndex() + 1));
        for (int col = 0; col < cells.size(); col++) {
            DefaultCell cell = cells.get(col);
            String value = cell == null ? "" : cell.getValue() == null ? "" : cell.getValue().strValue();
            sb.append(String.format(" %-8s \t|", value));
        }
        sb.delete(sb.length() - 2, sb.length()).append("\n");
        // second line: formula
        sb.append("      |");
        for (int col = 0; col < cells.size(); col++) {
            DefaultCell cell = cells.get(col);
            String formula = cell == null ? "" : cell.getFormula() == null ? "" : cell.getFormula().getString();
            sb.append(String.format(" %-8s \t|", formula));
        }
        sb.delete(sb.length() - 2, sb.length()).append("\n");
    }

    void erase() {
        Iterator<DefaultCell> it = cells.iterator();
        while (it.hasNext()) {
            DefaultCell cell = it.next();
            cell.erase();
        }
    }
}
