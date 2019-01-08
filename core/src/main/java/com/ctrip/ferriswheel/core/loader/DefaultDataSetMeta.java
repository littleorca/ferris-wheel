package com.ctrip.ferriswheel.core.loader;

import com.ctrip.ferriswheel.common.query.DataSet;

public class DefaultDataSetMeta implements DataSet.SetMeta {
    private final boolean hasRowMeta;
    private final int columnCount;
    private final DataSet.ColumnMeta[] columnMetas;

    public DefaultDataSetMeta(boolean hasRowMeta, DataSet.ColumnMeta[] columnMetas) {
        this.hasRowMeta = hasRowMeta;
        this.columnCount = columnMetas.length;
        this.columnMetas = columnMetas;
    }

    public DefaultDataSetMeta(boolean hasRowMeta, int columnCount) {
        this.hasRowMeta = hasRowMeta;
        this.columnCount = columnCount;
        this.columnMetas = null;
    }

    @Override
    public boolean hasRowMeta() {
        return hasRowMeta;
    }

    @Override
    public boolean hasColumnMeta() {
        return columnMetas != null;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public DataSet.ColumnMeta getColumnMeta(int index) {
        return columnMetas[index];
    }
}
