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

package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.common.variant.Variant;

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
