/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
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
 */

package com.ctrip.ferriswheel.common.query;

import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;

import java.util.*;

public final class ImmutableDataQuery implements DataQuery {
    private final String scheme;
    private final Map<String, Value> params;

    public static ImmutableDataQuery from(DataQuery another) {
        if (another == null) {
            return null;
        }
        if (another instanceof ImmutableDataQuery) {
            return (ImmutableDataQuery) another;
        }
        return new ImmutableDataQuery(another.getScheme(), another.getAllParams());
    }

    public ImmutableDataQuery(String scheme, Map<String, Variant> params) {
        this.scheme = scheme;

        if (params == null) {
            this.params = Collections.emptyMap();

        } else {
            this.params = new LinkedHashMap<>(params.size());
            params.forEach((k, v) -> this.params.put(k, Value.from(v)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableDataQuery that = (ImmutableDataQuery) o;
        return scheme.equals(that.scheme) &&
                params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, params);
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public Value getParam(String name) {
        return params.get(name);
    }

    @Override
    public Set<String> getParamNames() {
        return Collections.unmodifiableSet(params.keySet());
    }

    @Override
    public Map<String, Variant> getAllParams() {
        return new LinkedHashMap<>(params);
    }
}
