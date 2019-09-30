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

package com.ctrip.ferriswheel.common.query;

import com.ctrip.ferriswheel.common.variant.Variant;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author liuhaifeng
 */
public interface ParameterHolder {
    Variant getParam(String name);

    Set<String> getParamNames();

    Map<String, Variant> getAllParams();

    default Integer getInteger(String name) {
        Variant variant = getParam(name);
        return variant == null ? null : variant.intValue();
    }

    default Long getLong(String name) {
        Variant variant = getParam(name);
        return variant == null ? null : variant.longValue();
    }

    default Float getFloat(String name) {
        Variant variant = getParam(name);
        return variant == null ? null : variant.floatValue();
    }

    default Double getDouble(String name) {
        Variant variant = getParam(name);
        return variant == null ? null : variant.doubleValue();
    }

    default BigDecimal getDecimal(String name) {
        Variant variant = getParam(name);
        return variant == null ? null : variant.decimalValue();
    }

    default Boolean getBoolean(String name) {
        Variant variant = getParam(name);
        return variant == null ? null : variant.booleanValue();
    }

    default Date getDate(String name) {
        Variant variant = getParam(name);
        return variant == null ? null : variant.dateValue();
    }

    default String getString(String name) {
        Variant variant = getParam(name);
        return variant == null ? null : variant.strValue();
    }

    default List<Variant> getList(String name) {
        Variant variant = getParam(name);
        return variant == null ? null : variant.listValue();
    }

}
