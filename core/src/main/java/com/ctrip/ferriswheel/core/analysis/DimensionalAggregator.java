package com.ctrip.ferriswheel.core.analysis;

import com.ctrip.ferriswheel.common.table.AggregateType;
import com.ctrip.ferriswheel.common.table.Aggregator;
import com.ctrip.ferriswheel.common.variant.Variant;

import java.io.Serializable;
import java.util.*;

public class DimensionalAggregator {
    private final AggregateType[] types;
    private final Map<String, Set<Variant>> dimensions;
    private final Map<AggKey, Aggregator[]> buckets;

    public DimensionalAggregator(AggregateType[] types) {
        this.types = types;
        this.dimensions = new LinkedHashMap<>();
        this.buckets = new LinkedHashMap<>();
    }

    protected void feed(Record record) {
        Aggregator[] aggregators = buckets.get(record.key);
        if (aggregators == null) {
            aggregators = new Aggregator[types.length];
            buckets.put(record.key, aggregators);
        }
        for (int i = 0; i < types.length; i++) {
            Aggregator aggregator = aggregators[i];
            if (aggregator == null) {
                aggregator = Aggregators.create(types[i]);
                aggregators[i] = aggregator;
            }
            aggregator.feed(record.values[i]);
        }
        logDimensions(record.key);
    }

    private void logDimensions(AggKey key) {
        for (Map.Entry<String, Variant> entry : key.dimensions.entrySet()) {
            Set<Variant> dimValues = this.dimensions.get(entry.getKey());
            if (dimValues == null) {
                dimValues = new LinkedHashSet<>();
                this.dimensions.put(entry.getKey(), dimValues);
            }
            dimValues.add(entry.getValue());
        }
    }

    public Map<String, Set<Variant>> getAllDimensions() {
        return dimensions;
    }

    public Variant[] getValues(Map<String, Variant> dimensions) {
        AggKey key = new AggKey();
        key.dimensions = dimensions;
        Aggregator[] aggs = buckets.get(key);
        if (aggs == null) {
            return null;
        }
        Variant[] values = new Variant[aggs.length];
        for (int i = 0; i < aggs.length; i++) {
            values[i] = aggs[i].getResult();
        }
        return values;
    }

    class AggKey implements Serializable {
        private Map<String, Variant> dimensions = new HashMap<>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AggKey aggKey = (AggKey) o;
            return Objects.equals(dimensions, aggKey.dimensions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimensions);
        }
    }

    public Record newRecord() {
        return new Record(this);
    }

    public class Record {
        private final DimensionalAggregator aggregator;
        private AggKey key = new AggKey();
        private Variant[] values;
        private boolean committed = false;

        private Record(DimensionalAggregator aggregator) {
            this.aggregator = aggregator;
            this.values = new Variant[aggregator.types.length];
        }

        public Record dim(String dimName, Variant dimValue) {
            if (committed) {
                throw new IllegalStateException("This record has been committed already.");
            }
            key.dimensions.put(dimName, dimValue);
            return this;
        }

        public Record val(int index, Variant value) {
            if (committed) {
                throw new IllegalStateException("This record has been committed already.");
            }
            this.values[index] = value;
            return this;
        }

        public void commit() {
            this.aggregator.feed(this);
            this.committed = true;
        }
    }
}
