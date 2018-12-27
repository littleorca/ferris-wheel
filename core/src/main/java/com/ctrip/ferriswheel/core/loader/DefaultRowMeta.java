package com.ctrip.ferriswheel.core.loader;

import com.ctrip.ferriswheel.api.query.DataSet;

public class DefaultRowMeta implements DataSet.RowMeta {
    private final String name;

    public DefaultRowMeta(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
