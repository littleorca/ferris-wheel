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

package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.util.VariantProperties;

import java.math.BigDecimal;
import java.util.*;

public class DefaultDataQuery extends VariantProperties implements DataQuery {
    private String scheme;

    public DefaultDataQuery() {
    }

    public DefaultDataQuery(DataQuery queryInfo) {
        this(queryInfo.getScheme(), queryInfo.getAllParams());
    }

    public DefaultDataQuery(String scheme, Map<String, Variant> m) {
        super(m);
        this.scheme = scheme;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public Variant getParam(String name) {
        return get(name);
    }

    @Override
    public Set<String> getParamNames() {
        return keySet();
    }

    public Variant setParameter(String name, Variant value) {
        return put(name, value);
    }

    @Override
    public Map<String, Variant> getAllParams() {
        return Collections.unmodifiableMap(this);
    }

    @Override
    public Integer getInteger(String name) {
        Variant var = getParam(name);
        return var == null ? null : var.intValue();
    }

    @Override
    public Long getLong(String name) {
        Variant var = getParam(name);
        return var == null ? null : var.longValue();
    }

    @Override
    public Float getFloat(String name) {
        Variant var = getParam(name);
        return var == null ? null : var.floatValue();
    }

    @Override
    public Double getDouble(String name) {
        Variant var = getParam(name);
        return var == null ? null : var.doubleValue();
    }

    @Override
    public BigDecimal getDecimal(String name) {
        Variant var = getParam(name);
        return var == null ? null : var.decimalValue();
    }

    @Override
    public Boolean getBoolean(String name) {
        Variant var = getParam(name);
        return var == null ? null : var.booleanValue();
    }

    @Override
    public Date getDate(String name) {
        Variant var = getParam(name);
        return var == null ? null : var.dateValue();
    }

    @Override
    public String getString(String name) {
        Variant var = getParam(name);
        return var == null ? null : var.strValue();
    }

    @Override
    public List<Variant> getList(String name) {
        Variant var = getParam(name);
        return var == null ? null : var.listValue();
    }

}
