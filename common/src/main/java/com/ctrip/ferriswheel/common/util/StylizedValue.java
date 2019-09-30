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

package com.ctrip.ferriswheel.common.util;

import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;

public final class StylizedValue implements StylizedVariant {
    private final Value value;
    private final String format;

    public static StylizedValue from(StylizedVariant another) {
        if (another == null) {
            return null;
        }
        if (another instanceof StylizedValue) {
            return (StylizedValue) another;
        }
        return new StylizedValue(another.getValue(), another.getFormat());
    }

    public StylizedValue(Variant value) {
        this(value, null);
    }

    public StylizedValue(Variant value, String format) {
        this.value = Value.from(value);
        this.format = format;
    }

    @Override
    public Variant getValue() {
        return value;
    }

    @Override
    public String getFormat() {
        return format;
    }
}
