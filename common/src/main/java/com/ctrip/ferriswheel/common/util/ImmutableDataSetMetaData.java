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

import java.util.Arrays;

public final class ImmutableDataSetMetaData implements DataSetMetaData {
    private final ImmutableColumnMetaData[] columnMetas;

    public static ImmutableDataSetMetaData from(DataSetMetaData metaData) {
        if (metaData == null) {
            return null;
        }
        if (metaData instanceof ImmutableDataSetMetaData) {
            return (ImmutableDataSetMetaData) metaData;
        }
        ImmutableColumnMetaData[] immutableColumnMetas = new ImmutableColumnMetaData[metaData.getColumnCount()];
        for (int i = 0; i < immutableColumnMetas.length; i++) {
            immutableColumnMetas[i] = ImmutableColumnMetaData.from(metaData.getColumnMeta(i));
        }
        return new ImmutableDataSetMetaData(immutableColumnMetas);
    }

    public ImmutableDataSetMetaData(ColumnMetaData[] columnMetas) {
        ImmutableColumnMetaData[] immutableColumnMetas = new ImmutableColumnMetaData[columnMetas.length];
        for (int i = 0; i < immutableColumnMetas.length; i++) {
            immutableColumnMetas[i] = ImmutableColumnMetaData.from(columnMetas[i]);
        }
        this.columnMetas = Arrays.copyOf(immutableColumnMetas, immutableColumnMetas.length);
    }

    public ImmutableDataSetMetaData(int columnCount) {
        this.columnMetas = new ImmutableColumnMetaData[columnCount];
    }

    private ImmutableDataSetMetaData(ImmutableColumnMetaData[] columnMetas) {
        this.columnMetas = columnMetas;
    }

    @Override
    public boolean hasColumnMeta() {
        for (ImmutableColumnMetaData columnMeta : columnMetas) {
            if (columnMeta != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getColumnCount() {
        return columnMetas == null ? 0 : columnMetas.length;
    }

    @Override
    public ColumnMetaData getColumnMeta(int index) {
        if (columnMetas == null) {
            throw new IndexOutOfBoundsException();
        }
        return columnMetas[index];
    }
}
