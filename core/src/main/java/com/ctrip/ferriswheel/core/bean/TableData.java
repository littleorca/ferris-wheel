/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.common.automaton.Automaton;
import com.ctrip.ferriswheel.common.table.*;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.view.Layout;
import com.ctrip.ferriswheel.core.util.TreeSparseArray;
import com.ctrip.ferriswheel.core.util.UnmodifiableIterator;

import java.io.Serializable;
import java.util.*;

public class TableData implements Table, Serializable {
    private String name;
    private List<Header> rowHeaders;
    private List<Header> columnHeaders;
    private TreeSparseArray<Row> rows;
    private AutomateConfiguration automateConfiguration;
    private Layout layout;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeSparseArray<Row> getRows() {
        return rows;
    }

    public void setRows(TreeSparseArray<Row> rows) {
        this.rows = rows;
    }

    @Override
    public int getRowCount() {
        return rowHeaders == null ? 0 : rowHeaders.size();
    }

    @Override
    public Header getRowHeader(int rowIndex) {
        return rowHeaders.get(rowIndex);
    }

    public void setRowHeaders(List<Header> rowHeaders) {
        this.rowHeaders = rowHeaders;
    }

    @Override
    public Row getRow(int rowIndex) {
        if (rows == null) {
            return null;
        }
        return rows.get(rowIndex);
    }

    @Override
    public int getColumnCount() {
        return columnHeaders == null ? 0 : columnHeaders.size();
    }

    @Override
    public Header getColumnHeader(int columnIndex) {
        return columnHeaders.get(columnIndex);
    }

    public void setColumnHeaders(List<Header> columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    @Override
    public Cell getCell(int rowIndex, int columnIndex) {
        Row row = getRow(rowIndex);
        return row == null ? null : row.getCell(columnIndex);
    }

    @Override
    public Variant setCellValue(int rowIndex, int columnIndex, Variant value) {
        Cell cell = getOrCreateCell(rowIndex, columnIndex);
        DynamicValue oldValue = new DynamicValue(cell.getData());
        // TODO review this cast
        if (value instanceof DynamicVariant) {
            ((CellData) cell).setData((DynamicVariant) value);
        } else {
            ((CellData) cell).setData(new DynamicValue(Value.from(value)));
        }
        return oldValue;
    }

    @Override
    public String setCellFormula(int rowIndex, int columnIndex, String formula) {
        Cell cell = getOrCreateCell(rowIndex, columnIndex);
        String oldFormulaString = cell.getData().getFormulaString();
        ((CellData) cell).setData(new DynamicValue(formula));
        return oldFormulaString;
    }

    @Override
    public void fillUp(int rowIndex, int columnIndex, int nRows) {
        fillUp(rowIndex, columnIndex, columnIndex, nRows);
    }

    @Override
    public void fillUp(int rowIndex, int firstColumn, int lastColumn, int nRows) {
        // TODO incomplete
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillRight(int rowIndex, int columnIndex, int nColumns) {
        fillRight(rowIndex, rowIndex, columnIndex, nColumns);
    }

    @Override
    public void fillRight(int firstRow, int lastRow, int columnIndex, int nColumns) {
        // TODO incomplete
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillDown(int rowIndex, int columnIndex, int nRows) {
        fillDown(rowIndex, columnIndex, columnIndex, nRows);
    }

    @Override
    public void fillDown(int rowIndex, int firstColumn, int lastColumn, int nRows) {
        // TODO incomplete
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillLeft(int rowIndex, int columnIndex, int nColumns) {
        fillLeft(rowIndex, rowIndex, columnIndex, nColumns);
    }

    @Override
    public void fillLeft(int firstRow, int lastRow, int columnIndex, int nColumns) {
        // TODO incomplete
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellFillUp(int rowIndex, int columnIndex, boolean fillUp) {
        // TODO incomplete
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellFillDown(int rowIndex, int columnIndex, boolean fillDown) {
        // TODO incomplete
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellFillLeft(int rowIndex, int columnIndex, boolean fillLeft) {
        // TODO incomplete
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellFillRight(int rowIndex, int columnIndex, boolean fillRight) {
        // TODO incomplete
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCellsFormat(int rowIndex, int columnIndex, int nRows, int nColumns, String format) {
        final int left = Math.max(columnIndex, 0);
        final int top = Math.max(rowIndex, 0);
        final int right = Math.min(columnIndex + nColumns - 1, getColumnCount() - 1);
        final int bottom = Math.min(rowIndex + nRows - 1, getRowCount() - 1);
//        if (left < 0 || top < 0 || right <= left || bottom <= top) {
//            throw new IllegalArgumentException();
//        }
        for (int r = top; r <= bottom; r++) {
            for (int c = left; c <= right; c++) {
                CellData cell = (CellData) getOrCreateCell(r, c);
                cell.setFormat(format);
            }
        }
    }

    @Override
    public void eraseCells(int top, int right, int bottom, int left) {
        for (int r = top; r <= bottom; r++) {
            Row row = getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = left; c <= right; c++) {
                Cell cell = row.getCell(c);
                if (cell != null) {
                    // TODO review this cast
                    ((CellData) cell).erase();
                }
            }
        }
    }

    @Override
    public void addRows(int rowIndex, int nRows) {
        final int prevRowCount = getRowCount();
        if (rowIndex < 0 || rowIndex > prevRowCount || nRows < 1) {
            throw new IllegalArgumentException();
        }
        createRowHeaders(rowIndex, nRows);
        // just move rows to make room for new rows.
        for (int i = prevRowCount - 1; i >= rowIndex; i--) {
            Row row = rows.remove(i);
            int toIdx = i + nRows;
            if (row == null) {
                rows.remove(toIdx);
            } else {
                rows.set(toIdx, row);
            }
        }
    }

    private void createRowHeaders(int rowIndex, int nRows) {
        if (rowHeaders == null) {
            rowHeaders = new ArrayList<>();
        }
        for (int i = 0; i < nRows; i++) {
            rowHeaders.add(rowIndex, new HeaderInfo());
        }
    }

    @Override
    public void removeRows(int rowIndex, int nRows) {
        if (rowIndex < 0 || nRows < 1 || rowIndex + nRows > getRowCount()) {
            throw new IllegalArgumentException();
        }
        int i;
        for (i = rowIndex; i < getRowCount() - nRows; i++) {
            int fromIdx = i + nRows;
            Row row = rows.remove(fromIdx);
            if (row == null) {
                rows.remove(i);
            } else {
                rows.set(i, row);
            }
        }
        for (; i < rowIndex + nRows; i++) {
            rows.remove(i);
        }
        removeRowHeaders(rowIndex, nRows);
    }

    private void removeRowHeaders(int rowIndex, int nRows) {
        for (int i = 0; i < nRows; i++) {
            rowHeaders.remove(rowIndex);
        }
    }

    @Override
    public void addColumns(int colIndex, int nCols) {
        final int prevColumnCount = getColumnCount();
        if (colIndex < 0 || colIndex > prevColumnCount || nCols < 1) {
            throw new IllegalArgumentException();
        }
        createColumnHeaders(colIndex, nCols);
        for (int r = 0; r < getRowCount(); r++) {
            Row row = getRow(r);
            if (row == null) {
                continue;
            }
            for (int c = prevColumnCount - 1; c >= colIndex; c--) {
                // TODO review this cast
                ((RowData) row).moveCell(c, c + nCols);
            }
        }
    }

    private void createColumnHeaders(int colIndex, int nCols) {
        if (columnHeaders == null) {
            columnHeaders = new ArrayList<>();
        }
        for (int i = 0; i < nCols; i++) {
            columnHeaders.add(colIndex, new HeaderInfo());
        }
    }

    @Override
    public void removeColumns(int colIndex, int nCols) {
        final int prevColumnCount = getColumnCount();
        if (colIndex < 0 || nCols < 1 || colIndex + nCols > prevColumnCount) {
            throw new IllegalArgumentException();
        }
        for (int r = 0; r < getRowCount(); r++) {
            Row row = getRow(r);
            if (row == null) {
                continue;
            }
            int c;
            for (c = colIndex; c < prevColumnCount - nCols; c++) {
                ((RowData) row).moveCell(c + nCols, c);
            }
            for (; c < colIndex + nCols; c++) {
                ((RowData) row).removeCell(c);
            }
        }
        removeColumnHeaders(colIndex, nCols);
    }

    private void removeColumnHeaders(int colIndex, int nCols) {
        for (int i = 0; i < nCols; i++) {
            columnHeaders.remove(colIndex);
        }
    }

    protected Row getOrCreateRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount()) {
            throw new IndexOutOfBoundsException();
        }
        Row row = getRow(rowIndex);
        if (row == null) {
            row = new RowData();
            if (rows == null) {
                rows = new TreeSparseArray<>();
            }
            rows.set(rowIndex, row);
        }
        return row;
    }

    protected Cell getOrCreateCell(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount() ||
                columnIndex < 0 || columnIndex >= getColumnCount()) {
            throw new IndexOutOfBoundsException();
        }
        Row row = getOrCreateRow(rowIndex);
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = new CellData();
            // TODO review this
            ((RowData) row).setCell(columnIndex, cell);
        }
        return cell;
    }

    @Override
    public void automate(AutomateConfiguration solution) {
        setAutomateConfiguration(solution);
    }

    @Override
    public Automaton getAutomaton() {
        return null; // TODO review this
    }

    public AutomateConfiguration getAutomateConfiguration() {
        return automateConfiguration;
    }

    public void setAutomateConfiguration(AutomateConfiguration automatonInfo) {
        this.automateConfiguration = automatonInfo;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public Iterator<Map.Entry<Integer, Row>> iterator() {
        if (rows == null) {
            return Collections.emptyIterator();
        }
        return new UnmodifiableIterator<>(rows.iterator());
    }

}
