package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.util.SparseArray;
import com.ctrip.ferriswheel.core.view.Layout;

import java.io.Serializable;

public class TableData implements Serializable {
    private SparseArray<RowData> rows;
    private TableAutomatonInfo automatonInfo;
    private Layout layout;

    public SparseArray<RowData> getRows() {
        return rows;
    }

    public void setRows(SparseArray<RowData> rows) {
        this.rows = rows;
    }

    public TableAutomatonInfo getAutomatonInfo() {
        return automatonInfo;
    }

    public void setAutomatonInfo(TableAutomatonInfo automatonInfo) {
        this.automatonInfo = automatonInfo;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }
}
