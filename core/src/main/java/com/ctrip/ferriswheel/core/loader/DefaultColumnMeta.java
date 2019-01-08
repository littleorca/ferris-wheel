package com.ctrip.ferriswheel.core.loader;

import com.ctrip.ferriswheel.common.query.DataSet;
import com.ctrip.ferriswheel.common.variant.VariantType;

public class DefaultColumnMeta implements DataSet.ColumnMeta {
    private final String name;
    private final VariantType type;

    public DefaultColumnMeta(String name, VariantType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public VariantType getType() {
        return type;
    }
}
