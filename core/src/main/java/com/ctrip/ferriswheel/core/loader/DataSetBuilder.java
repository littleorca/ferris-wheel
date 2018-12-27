package com.ctrip.ferriswheel.core.loader;

import com.ctrip.ferriswheel.api.query.DataSet;
import com.ctrip.ferriswheel.api.variant.Variant;

import java.util.ArrayList;
import java.util.List;

public class DataSetBuilder {
    private int columnCount = 0;
    private List<DataSet.ColumnMeta> columnMetas = new ArrayList<>();
    private boolean hasRowMeta = false;
    private List<DataSetRecord> records = new ArrayList<>();

    public static DataSet emptyDataSet() {
        return new DefaultDataSet();
    }

    public DataSet build() {
        DataSet.SetMeta setMeta;
        if (columnMetas.isEmpty()) {
            setMeta = new DefaultDataSetMeta(hasRowMeta, columnCount);
        } else {
            setMeta = new DefaultDataSetMeta(hasRowMeta,
                    columnMetas.toArray(new DataSet.ColumnMeta[columnMetas.size()]));
        }
        return new DefaultDataSet(setMeta, records);
    }

    /**
     * CAUTION: when column meta is added, column count will be ignored,
     * and this method should not be invoked.
     *
     * @param columnCount
     * @return
     * @see #addColumnMeta(DataSet.ColumnMeta)
     */
    public DataSetBuilder setColumnCount(int columnCount) {
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
    public DataSetBuilder addColumnMeta(DataSet.ColumnMeta columnMeta) {
        this.columnMetas.add(columnMeta);
        return this;
    }

    public DataSetBuilder setHasRowMeta(boolean hasRowMeta) {
        this.hasRowMeta = hasRowMeta;
        return this;
    }

    public DataSetRecordBuilder newRecord() {
        return new DataSetRecordBuilder(this);
    }

    public class DataSetRecordBuilder {
        private final DataSetBuilder dataSetBuilder;
        private DataSetRecord record = new DataSetRecord();

        DataSetRecordBuilder(DataSetBuilder dataSetBuilder) {
            this.dataSetBuilder = dataSetBuilder;
        }

        public DataSetRecordBuilder setMeta(DataSet.RowMeta rowMeta) {
            if (record == null) {
                throw new IllegalStateException("Record has been committed, allows no further ops.");
            }
            record.setMeta(rowMeta);
            return this;
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

        public DataSetBuilder commit() {
            if (record == null) {
                throw new IllegalStateException("Record has been committed already.");
            }
            this.dataSetBuilder.records.add(record);
            record = null;
            return this.dataSetBuilder;
        }
    }
}
