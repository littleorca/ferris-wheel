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

import com.ctrip.ferriswheel.common.variant.Variant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class ListDataSet implements DataSet {
    private DataSetMetaData setMeta;
    private List<Record> records;
    private transient Iterator<Record> iterator;
    private transient Record current = null;

    public ListDataSet() {
        this.setMeta = new DataSetMetaDataImpl(0);
    }

    public ListDataSet(DataSetMetaData setMeta, List<Record> records) {
        this.setMeta = setMeta;
        this.records = records;
    }

    @Override
    public DataSetMetaData getMetaData() {
        return setMeta;
    }

    public void setSetMeta(DataSetMetaData setMeta) {
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
    public boolean isReusable() {
        return true;
    }

    @Override
    public void rewind() throws UnsupportedOperationException {
        iterator = null;
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

    public static class Record implements Serializable {
        private TreeMap<Integer, Variant> fields = new TreeMap<>();

        public Variant getField(int index) {
            return fields.get(index);
        }

        public void setField(int index, Variant value) {
            fields.put(index, value);
        }

        public TreeMap<Integer, Variant> getFields() {
            return fields;
        }

        public void setFields(TreeMap<Integer, Variant> fields) {
            this.fields = fields;
        }
    }

    public static class Builder {
        private int columnCount = 0;
        private List<ColumnMetaData> columnMetas = new ArrayList<>();
        private List<Record> records = new ArrayList<>();

        public static DataSet emptyDataSet() {
            return new ListDataSet();
        }

        public DataSet build() {
            DataSetMetaData setMeta;
            if (columnMetas.isEmpty()) {
                setMeta = new DataSetMetaDataImpl(columnCount);
            } else {
                setMeta = new DataSetMetaDataImpl(columnMetas.toArray(new ColumnMetaData[columnMetas.size()]));
            }
            return new ListDataSet(setMeta, records);
        }

        /**
         * CAUTION: when column meta is added, column count will be ignored,
         * and this method should not be invoked.
         *
         * @param columnCount
         * @return
         * @see #addColumnMeta(ColumnMetaData)
         */
        public Builder setColumnCount(int columnCount) {
            if (!columnMetas.isEmpty()) {
                throw new IllegalStateException("Cannot set column count after added any column meta.");
            }
            this.columnCount = columnCount;
            return this;
        }

        /**
         * CAUTION: when column meta is added, column count will be ignored.
         *
         * @param columnMeta
         * @return
         */
        public Builder addColumnMeta(ColumnMetaData columnMeta) {
            this.columnMetas.add(columnMeta);
            return this;
        }

        public DataSetRecordBuilder newRecord() {
            return new DataSetRecordBuilder(this);
        }

        public class DataSetRecordBuilder {
            private final Builder dataSetBuilder;
            private Record record = new Record();

            DataSetRecordBuilder(Builder dataSetBuilder) {
                this.dataSetBuilder = dataSetBuilder;
            }

            public DataSetRecordBuilder set(int index, Variant value) {
                if (record == null) {
                    throw new IllegalStateException("Record has been committed, allows no further ops.");
                }
                record.setField(index, value);
                return this;
            }

            public DataSetRecordBuilder set(Variant... values) {
                if (record == null) {
                    throw new IllegalStateException("Record has been committed, allows no further ops.");
                }
                if (record.getFields() != null && !record.getFields().isEmpty()) {
                    throw new IllegalStateException("Some fields has already been set, this operation conflicts with former operations");
                }
                for (int i = 0; i < values.length; i++) {
                    record.setField(i, values[i]);
                }
                return this;
            }

            public Builder commit() {
                if (record == null) {
                    throw new IllegalStateException("Record has been committed already.");
                }
                this.dataSetBuilder.records.add(record);
                record = null;
                return this.dataSetBuilder;
            }
        }
    }
}
