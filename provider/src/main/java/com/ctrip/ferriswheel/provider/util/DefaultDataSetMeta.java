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

package com.ctrip.ferriswheel.provider.util;

import com.ctrip.ferriswheel.common.query.DataSet;

public class DefaultDataSetMeta implements DataSet.SetMeta {
    private final boolean hasRowMeta;
    private final int columnCount;
    private final DataSet.ColumnMeta[] columnMetas;

    public DefaultDataSetMeta(boolean hasRowMeta, DataSet.ColumnMeta[] columnMetas) {
        this.hasRowMeta = hasRowMeta;
        this.columnCount = columnMetas.length;
        this.columnMetas = columnMetas;
    }

    public DefaultDataSetMeta(boolean hasRowMeta, int columnCount) {
        this.hasRowMeta = hasRowMeta;
        this.columnCount = columnCount;
        this.columnMetas = null;
    }

    @Override
    public boolean hasRowMeta() {
        return hasRowMeta;
    }

    @Override
    public boolean hasColumnMeta() {
        return columnMetas != null;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public DataSet.ColumnMeta getColumnMeta(int index) {
        return columnMetas[index];
    }
}
