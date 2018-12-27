package com.ctrip.ferriswheel.core.loader;

import com.ctrip.ferriswheel.api.query.DataSet;
import com.ctrip.ferriswheel.api.variant.Variant;

import java.io.Serializable;
import java.util.TreeMap;

public class DataSetRecord implements Serializable {
    private DataSet.RowMeta meta;
    private TreeMap<Integer, Variant> fields = new TreeMap<>();

    public Variant getField(int index) {
        return fields.get(index);
    }

    public void setField(int index, Variant value) {
        fields.put(index, value);
    }

    public DataSet.RowMeta getMeta() {
        return meta;
    }

    public void setMeta(DataSet.RowMeta meta) {
        this.meta = meta;
    }

    public TreeMap<Integer, Variant> getFields() {
        return fields;
    }

    public void setFields(TreeMap<Integer, Variant> fields) {
        this.fields = fields;
    }
}
