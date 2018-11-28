package com.ctrip.ferriswheel.core.loader;

import com.ctrip.ferriswheel.core.intf.DataSet;
import com.ctrip.ferriswheel.core.intf.Variant;

import java.util.Iterator;
import java.util.List;

public class DefaultDataSet implements DataSet {
    private SetMeta setMeta;
    private List<DataSetRecord> records;
    private transient Iterator<DataSetRecord> iterator;
    private transient DataSetRecord current = null;

    public DefaultDataSet() {
        this.setMeta = new DefaultDataSetMeta(false, 0);
    }

    public DefaultDataSet(SetMeta setMeta, List<DataSetRecord> records) {
        this.setMeta = setMeta;
        this.records = records;
    }

    @Override
    public SetMeta getSetMeta() {
        return setMeta;
    }

    public void setSetMeta(SetMeta setMeta) {
        this.setMeta = setMeta;
    }

    @Override
    public boolean next() {
        if (records == null) {
            return false;
        }
        if (iterator == null) {
            iterator = records.iterator();
        }
        if (!iterator.hasNext()) {
            return false;
        }
        current = iterator.next();
        return true;
    }

    @Override
    public RowMeta getRowMeta() {
        return current.getMeta();
    }

    @Override
    public Variant getColumn(int index) {
        return current.getField(index);
    }

    @Override
    public Variant getColumn(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (!setMeta.hasColumnMeta()) {
            throw new IllegalStateException("No column meta data, column name unresolvable.");
        }
        for (int i = 0; i < setMeta.getColumnCount(); i++) {
            if (name.equals(setMeta.getColumnMeta(i).getName())) {
                return getColumn(i);
            }
        }
        return null;
    }
}
