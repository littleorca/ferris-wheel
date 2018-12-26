package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.action.ResetTable;
import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.intf.AssetManager;
import com.ctrip.ferriswheel.core.intf.DataSet;
import com.ctrip.ferriswheel.core.intf.TableAutomaton;
import com.ctrip.ferriswheel.core.intf.Variant;

public abstract class AbstractTableAutomaton extends AssetNode implements TableAutomaton {

    AbstractTableAutomaton(AssetManager assetManager) {
        super(assetManager);
    }

    @Override
    public void init() {
        // query(Collections.emptyMap());
    }

    @Override
    public void destroy() {
        // dummy
    }

    protected void fillTable(DefaultTable table, DataSet dataSet) {
        table.setReadOnly(false);
        try {
            doFillTable(table, dataSet);
        } finally {
            table.setReadOnly(true);
            table.publicly(new ResetTable(table.getSheet().getName(), table), () -> {
            });
        }
    }

    protected void doFillTable(DefaultTable table, DataSet dataSet) {
        DataSet.SetMeta setMeta = dataSet.getSetMeta();
        int row = 0;
        int colOff = setMeta.hasRowMeta() ? 1 : 0;

        //table.eraseRows(0, table.getRowCount());

        if (setMeta.hasColumnMeta()) {
            for (int col = 0; col < setMeta.getColumnCount(); col++) {
                refreshCellValue(table, row, colOff + col, Value.str(setMeta.getColumnMeta(col).getName()));
            }
            row++;
        }
        while (dataSet.next()) {
            if (setMeta.hasRowMeta()) {
                refreshCellValue(table, row, 0, new Value.StrValue(dataSet.getRowMeta().getName()));
            }
            for (int col = 0; col < setMeta.getColumnCount(); col++) {
                Variant value = dataSet.getColumn(col);
                if (value == null) {
                    value = Value.BLANK;
                }
                refreshCellValue(table, row, colOff + col, Value.from(value));
            }
            row++;
        }

        // trim rows/columns if needed
        if (table.getRowCount() > row) {
            table.removeRows(row, table.getRowCount() - row);
        }
        if (table.getColumnCount() > colOff + setMeta.getColumnCount()) {
            table.removeColumns(colOff + setMeta.getColumnCount(),
                    table.getColumnCount() - (colOff + setMeta.getColumnCount()));
        }

        table.fixColumnCount();
    }

    protected void refreshCellValue(DefaultTable table, int rowIndex, int columnIndex, Value newValue) {
        DefaultCell cell = table.getCell(rowIndex, columnIndex);
        if (!newValue.equals(cell.getValue())) {
            cell.setValue(newValue);
        }
    }

    public DefaultTable getTable() {
        return (DefaultTable) getParent();
    }

    protected void clearTable() {
        DefaultTable table = getTable();
        if (table == null) {
            return;
        }
        table.setReadOnly(false);
        try {
            doClearTable(table);
        } finally {
            table.setReadOnly(true);
            table.publicly(new ResetTable(table.getSheet().getName(), table), () -> {
            });
        }
    }

    protected void doClearTable(DefaultTable table) {
        if (table != null) {
            if (table.getRowCount() > 0) {
                table.removeRows(0, table.getRowCount());
            }
        }
    }
}
