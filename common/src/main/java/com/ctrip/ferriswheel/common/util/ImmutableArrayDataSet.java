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

package com.ctrip.ferriswheel.common.util;

import java.util.Iterator;

public final class ImmutableArrayDataSet implements DataSet {
    private final ImmutableDataSetMetaData metaData;
    private final ImmutableArrayRecord[] records;

    public static ImmutableArrayDataSet from(DataSet dataSet) {
        if (dataSet == null) {
            return null;

        } else if (dataSet instanceof ImmutableArrayDataSet) {
            return (ImmutableArrayDataSet) dataSet;

        } else {
            DataSetBuilder dataSetBuilder = DataSetBuilder.withMetaData(dataSet.getMetadata());

            for (DataRecord record : dataSet) {
                if (record == null) {
                    dataSetBuilder.addRecord(null); // forbid null record?
                } else if (record instanceof ImmutableArrayRecord) {
                    dataSetBuilder.addRecord((ImmutableArrayRecord) record);
                } else {
                    DataSetBuilder.RecordBuilder recordBuilder = dataSetBuilder.newRecord();
                    for (int i = 0; i < dataSet.getMetadata().getColumnCount(); i++) {
                        recordBuilder.set(i, record.getColumn(i));
                    }
                    recordBuilder.commit();
                }
            }

            return dataSetBuilder.build();
        }
    }

    ImmutableArrayDataSet(ImmutableDataSetMetaData metaData, ImmutableArrayRecord[] records) {
        this.metaData = metaData;
        this.records = records;
    }

    @Override
    public DataSetMetaData getMetadata() {
        return metaData;
    }

    @Override
    public Iterator<DataRecord> iterator() {
        return new ReadonlyIterator();
    }

    final class ReadonlyIterator implements Iterator<DataRecord> {
        private volatile int cursor = 0;

        @Override
        public boolean hasNext() {
            return ImmutableArrayDataSet.this.records != null && cursor < ImmutableArrayDataSet.this.records.length;
        }

        @Override
        public DataRecord next() {
            if (!hasNext()) {
                return null;
            }
            return ImmutableArrayDataSet.this.records[cursor++];
        }
    }
}
