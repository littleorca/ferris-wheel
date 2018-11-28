package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.intf.Variant;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class VariantProperties extends LinkedHashMap<String, Variant> {
    public VariantProperties(int initialCapacity) {
        super(initialCapacity);
    }

    public VariantProperties() {
    }

    public VariantProperties(Map<? extends String, ? extends Variant> m) {
        super(m);
    }

    public Integer getInteger(String name) {
        Variant var = get(name);
        return var == null ? null : var.intValue();
    }

    public Integer setInteger(String name, int value) {
        Variant var = put(name, Value.dec(value));
        return var == null ? null : var.intValue();
    }

    public Long getLong(String name) {
        Variant var = get(name);
        return var == null ? null : var.longValue();
    }

    public Long setLong(String name, long value) {
        Variant var = put(name, Value.dec(value));
        return var == null ? null : var.longValue();
    }

    public Float getFloat(String name) {
        Variant var = get(name);
        return var == null ? null : var.floatValue();
    }

    public Float setFloat(String name, float value) {
        Variant var = put(name, Value.dec(value));
        return var == null ? null : var.floatValue();
    }

    public Double getDouble(String name) {
        Variant var = get(name);
        return var == null ? null : var.doubleValue();
    }

    public Double setDouble(String name, double value) {
        Variant var = put(name, Value.dec(value));
        return var == null ? null : var.doubleValue();
    }

    public Boolean getBoolean(String name) {
        Variant var = get(name);
        return var == null ? null : var.booleanValue();
    }

    public Boolean setBoolean(String name, boolean value) {
        Variant var = put(name, Value.bool(value));
        return var == null ? null : var.booleanValue();
    }

    public Date getDate(String name) {
        Variant var = get(name);
        return var == null ? null : var.dateValue();
    }

    public Date setDate(String name, Date value) {
        Variant var = put(name, Value.date(value));
        return var == null ? null : var.dateValue();
    }

    public String getString(String name) {
        Variant var = get(name);
        return var == null ? null : var.strValue();
    }

    public String setString(String name, String value) {
        Variant var = put(name, Value.str(value));
        return var == null ? null : var.strValue();
    }

}
