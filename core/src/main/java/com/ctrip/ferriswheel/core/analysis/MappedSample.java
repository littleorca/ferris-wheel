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
 *
 */

package com.ctrip.ferriswheel.core.analysis;

import com.ctrip.ferriswheel.common.aggregate.NamedValuesSample;
import com.ctrip.ferriswheel.common.variant.Variant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author liuhaifeng
 */
public class MappedSample implements NamedValuesSample {
    private final Map<String, Variant> values;

    public static Builder newBuilder() {
        return new Builder();
    }

    public MappedSample(Map<String, Variant> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public Variant getValue(String name) {
        return values.get(name);
    }

    @Override
    public Iterator<String> iterator() {
        return values.keySet().iterator();
    }

    public static class Builder {
        private Map<String, Variant> values = new HashMap<>();

        public Builder add(String name, Variant value) {
            if (values == null) {
                throw new IllegalStateException();
            }
            values.put(name, value);
            return this;
        }

        public MappedSample build() {
            if (values == null) {
                throw new IllegalStateException();
            }
            MappedSample sample = new MappedSample(values);
            values = null;
            return sample;
        }
    }

}
