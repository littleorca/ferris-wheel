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

package com.ctrip.ferriswheel.common.util;

import com.ctrip.ferriswheel.common.variant.VariantType;

import java.util.Objects;

public final class ImmutableColumnMetaData implements ColumnMetaData {
    private final String name;
    private final VariantType type;

    public static ImmutableColumnMetaData from(ColumnMetaData columnMetaData) {
        if (columnMetaData == null) {
            return null;
        } else if (columnMetaData instanceof ImmutableColumnMetaData) {
            return (ImmutableColumnMetaData) columnMetaData;
        } else {
            return new ImmutableColumnMetaData(columnMetaData.getName(), columnMetaData.getType());
        }
    }

    public ImmutableColumnMetaData(String name, VariantType type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableColumnMetaData that = (ImmutableColumnMetaData) o;
        return name.equals(that.name) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
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
