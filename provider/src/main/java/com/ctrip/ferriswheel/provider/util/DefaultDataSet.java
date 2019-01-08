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
import com.ctrip.ferriswheel.common.variant.Variant;

import java.util.Iterator;
import java.util.List;

public class DefaultDataSet implements DataSet {
    private DataSet.SetMeta setMeta;
    private List<DataSetRecord> records;
    private transient Iterator<DataSetRecord> iterator;
    private transient DataSetRecord current = null;

    public DefaultDataSet() {
        this.setMeta = new DefaultDataSetMeta(false, 0);
    }

    public DefaultDataSet(SetMeta setMeta, List<DataSetRecord> records) {
        this.setMeta = setMeta;
        this.records = records;
    }

    @Override
    public SetMeta getSetMeta() {
        return setMeta;
    }

    public void setSetMeta(SetMeta setMeta) {
        this.setMeta = setMeta;
    }

    @Override
    public boolean next() {
        if (records == null) {
            return false;
        }
        if (iterator == null) {
            iterator = records.iterator();
        }
        if (!iterator.hasNext()) {
            return false;
        }
        current = iterator.next();
        return true;
    }

    @Override
    public RowMeta getRowMeta() {
        return current.getMeta();
    }

    @Override
    public Variant getColumn(int index) {
        return current.getField(index);
    }

    @Override
    public Variant getColumn(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (!setMeta.hasColumnMeta()) {
            throw new IllegalStateException("No column meta data, column name unresolvable.");
        }
        for (int i = 0; i < setMeta.getColumnCount(); i++) {
            if (name.equals(setMeta.getColumnMeta(i).getName())) {
                return getColumn(i);
            }
        }
        return null;
    }
}
