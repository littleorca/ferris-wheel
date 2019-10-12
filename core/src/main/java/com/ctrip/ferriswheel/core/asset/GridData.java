/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
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
 */

package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.table.Header;
import com.ctrip.ferriswheel.common.table.Row;
import com.ctrip.ferriswheel.common.util.*;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.action.ResetTable;
import com.ctrip.ferriswheel.core.bean.HeaderInfo;
import com.ctrip.ferriswheel.core.util.UnmodifiableIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class GridData extends AssetNode {
    private List<Header> rowHeaders = new ArrayList<>();
    private List<Header> columnHeaders = new ArrayList<>();
    private final SparseAssetArray<DefaultRow> rows;
    private boolean readOnly = false;

    GridData(AssetManager assetManager) {
        super(assetManager);
        this.rows = new SparseAssetArray<>(this);
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        if (getTable().getAutomaton() != null) {
            fillByAutomaton();
        }
        return EvaluationState.DONE;
    }

    void fillByAutomaton() {
        final DefaultTable table = getTable();
        setReadOnly(false);
        try {
            DataSet dataSet = table.getAutomaton().getDataSet();
            table.getSheet().getNotifier().privately(() ->
            {
                if (dataSet != null) {
                    fill(dataSet);
                } else {
                    clear();
                }
                table.onTableUpdate();
            });
        } finally {
            setReadOnly(true);
            table.publicly(new ResetTable(table.getSheet().getName(), table), () -> {
            });
        }
    }

    void clear() {
        if (getRowCount() > 0) {
            getTable().removeRows(0, getRowCount());
        }
        if (this.rowHeaders != null) {
            this.rowHeaders.clear();
        }
        if (this.columnHeaders != null) {
            this.columnHeaders.clear();
        }
    }

    void fill(DataSet dataSet) {
        DefaultTable table = getTable();
        DataSetMetaData setMeta = dataSet.getMetadata();
        int rowCount = 0;

        columnHeaders = new ArrayList<>(setMeta.getColumnCount());
        rowHeaders = new ArrayList<>();

        if (setMeta.hasColumnMeta()) {
            rowHeaders.add(new HeaderInfo(/* TBD */));
            for (int col = 0; col < setMeta.getColumnCount(); col++) {
                columnHeaders.add(new HeaderInfo(/* TBD */));
                DefaultCell cell = table.getOrCreateCell(rowCount, col);
                ColumnMetaData colMeta = setMeta.getColumnMeta(col);
                table.refreshCellValue(rowCount, col, colMeta != null ? Value.str(colMeta.getName()) : Value.BLANK);
            }
            rowCount++;
        } else {
            for (int col = 0; col < setMeta.getColumnCount(); col++) {
                columnHeaders.add(new HeaderInfo(/* TBD */));
            }
        }
        for (DataRecord record : dataSet) {
            rowHeaders.add(new HeaderInfo(/* TBD */));
            for (int col = 0; col < setMeta.getColumnCount(); col++) {
                StylizedVariant stylizedVariant = record.getColumn(col);
                Variant value = stylizedVariant.getValue();
                if (value == null) {
                    value = Value.BLANK;
                }
                String format = stylizedVariant.getFormat();
//                refreshCellValue(row, col, Value.from(value));
                DefaultCell cell = table.getOrCreateCell(rowCount, col);
                cell.setValue(value);
                if (format != null) {
                    cell.setFormat(format);
                }
            }
            rowCount++;
        }

        // trim rows/columns if needed
        for (int i = getRowCount(); i < rows.size(); i++) {
            rows.remove(i);
        }
        Iterator<Map.Entry<Integer, DefaultRow>> rowIter = rows.iterator();
        while (rowIter.hasNext()) {
            DefaultRow row = rowIter.next().getValue();
            int count = row.getCellCount();
            for (int i = getColumnCount(); i < count; i++) {
                row.removeCell(i);
            }
        }
    }

    int getRowCount() {
        return rowHeaders == null ? 0 : rowHeaders.size();
    }

    Header getRowHeader(int rowIndex) {
        return rowHeaders.get(rowIndex);
    }

    List<Header> getRowHeaders() {
        return rowHeaders;
    }

    DefaultRow getRow(int index) {
        return rows.get(index);
    }

    SparseAssetArray<DefaultRow> getRows() {
        return rows;
    }

    int getColumnCount() {
        return columnHeaders == null ? 0 : columnHeaders.size();
    }

    Header getColumnHeader(int columnIndex) {
        return columnHeaders.get(columnIndex);
    }

    List<Header> getColumnHeaders() {
        return columnHeaders;
    }

    boolean isReadOnly() {
        return readOnly;
    }

    void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    Iterator<Map.Entry<Integer, Row>> iterator() {
        return new UnmodifiableIterator(rows.iterator());
    }

    private DefaultTable getTable() {
        return (DefaultTable) getParent();
    }
}