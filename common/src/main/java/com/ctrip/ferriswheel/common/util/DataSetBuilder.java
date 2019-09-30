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

import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;

import java.util.ArrayList;
import java.util.List;

public final class DataSetBuilder {
    private ImmutableDataSetMetaData metaData;
    private List<ImmutableArrayRecord> records = new ArrayList<>();

    public static DataSet emptyDataSet() {
        return new ImmutableArrayDataSet(
                new ImmutableDataSetMetaData(0),
                new ImmutableArrayRecord[0]);
    }

    public static DataSetBuilder withColumnCount(int columnCount) {
        return new DataSetBuilder(columnCount);
    }

    public static DataSetBuilder withMetaData(DataSetMetaData metaData) {
        return new DataSetBuilder(metaData);
    }

    public static MetaDataBuilder metaDataBuilder() {
        return new MetaDataBuilder();
    }

    DataSetBuilder(int columnCount) {
        if (columnCount < 0) {
            throw new IllegalArgumentException();
        }
        this.metaData = new ImmutableDataSetMetaData(columnCount);
    }

    DataSetBuilder(DataSetMetaData metaData) {
        this.metaData = ImmutableDataSetMetaData.from(metaData);
    }

    public RecordBuilder newRecord() {
        ensureNotBuilt();
        return new RecordBuilder(this);
    }

    public DataSetBuilder addRecord(ImmutableArrayRecord record) {
        ensureNotBuilt();
        this.records.add(record);
        return this;
    }

    private void ensureNotBuilt() throws IllegalStateException {
        if (metaData == null) {
            throw new IllegalStateException("Data set has been built already.");
        }
    }

    public ImmutableArrayDataSet build() {
        ensureNotBuilt();
        try {
            return new ImmutableArrayDataSet(
                    metaData,
                    records.toArray(new ImmutableArrayRecord[records.size()]));

        } finally {
            this.metaData = null;
            this.records = null;
        }
    }

    public static final class MetaDataBuilder {
        private List<ColumnMetaData> columnMetas = new ArrayList<>();

        MetaDataBuilder() {
        }

        public MetaDataBuilder addColumns(int count) {
            for (int i = 0; i < count; i++) {
                addColumn(null);
            }
            return this;
        }

        public MetaDataBuilder addColumn(String name, VariantType type) {
            return addColumn(new ImmutableColumnMetaData(name, type));
        }

        public MetaDataBuilder addColumn(ColumnMetaData columnMetaData) {
            if (columnMetas == null) {
                throw new IllegalStateException();
            }
            columnMetas.add(columnMetaData);
            return this;
        }

        public DataSetBuilder seal() {
            ColumnMetaData[] columnMetaArray = columnMetas.toArray(new ColumnMetaData[columnMetas.size()]);
            columnMetas = null; // this builder cannot be reused.
            return new DataSetBuilder(new ImmutableDataSetMetaData(columnMetaArray));
        }
    }

    public static final class RecordBuilder {
        private final DataSetBuilder dataSetBuilder;
        private StylizedValue[] fields;

        RecordBuilder(DataSetBuilder dataSetBuilder) {
            this.dataSetBuilder = dataSetBuilder;
            this.fields = new StylizedValue[dataSetBuilder.metaData.getColumnCount()];
        }

        public RecordBuilder set(int index, Variant value) {
            return set(index, new StylizedValue(value));
        }

        public RecordBuilder set(int index, StylizedVariant value) {
            ensureNotBuilt();
            fields[index] = StylizedValue.from(value);
            return this;
        }

        public RecordBuilder set(StylizedVariant... values) {
            ensureNotBuilt();
            for (int i = 0; i < values.length; i++) {
                fields[i] = StylizedValue.from(values[i]);
            }
            return this;
        }

        private void ensureNotBuilt() throws IllegalStateException {
            if (fields == null) {
                throw new IllegalStateException("Record has been built already.");
            }
        }

        public DataSetBuilder commit() {
            ensureNotBuilt();
            ImmutableArrayRecord record = new ImmutableArrayRecord(fields);
            this.fields = null;
            return dataSetBuilder.addRecord(record);
        }
    }

}
