package com.ctrip.ferriswheel.core.analysis;

import com.ctrip.ferriswheel.common.aggregate.AggregateType;
import com.ctrip.ferriswheel.common.aggregate.Aggregator;
import com.ctrip.ferriswheel.common.aggregate.NamedValuesSample;
import com.ctrip.ferriswheel.common.variant.Variant;

import java.io.Serializable;
import java.util.*;

public class DimensionalAggregateMaster {
    private final String[] dimensionNames;
    private final AggregateMeta[] aggregateMetas;
    private final Map<String, Set<Variant>> dimensions;
    private final Map<AggKey, Aggregator[]> buckets;

    public DimensionalAggregateMaster(String[] dimensionNames, AggregateMeta[] aggregateMetas) {
        this.dimensionNames = dimensionNames;
        this.aggregateMetas = aggregateMetas;
        this.dimensions = new LinkedHashMap<>();
        this.buckets = new LinkedHashMap<>();
    }

    public void feed(NamedValuesSample sample) {
        AggKey key = new AggKey();
        for (int i = 0; i < dimensionNames.length; i++) {
            String name = dimensionNames[i];
            key.dimensions.put(name, sample.getValue(name));
        }

        Aggregator[] aggregators = buckets.get(key);
        if (aggregators == null) {
            aggregators = new Aggregator[aggregateMetas.length];
            buckets.put(key, aggregators);
        }

        for (int i = 0; i < aggregateMetas.length; i++) {
            Aggregator aggregator = aggregators[i];
            if (aggregator == null) {
                aggregator = createAggregator(aggregateMetas[i]);
                aggregators[i] = aggregator;
            }
            if (AggregateType.CUSTOM.equals(aggregator.getType())) {
                aggregator.feed(sample);
            } else {
                Variant value = sample.getValue(aggregateMetas[i].getField());
                aggregator.feed(new VariantSample(value));
            }
        }
        logDimensions(key);
    }

    private Aggregator createAggregator(AggregateMeta aggregateMeta) {
        if (AggregateType.CUSTOM.equals(aggregateMeta.getType())) {
            return new ThreePhaseAggregator(aggregateMeta.getField());
        } else {
            return PlainAggregator.create(aggregateMeta.getType());
        }
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

    public Set<Variant> getDimensions(String name) {
        return dimensions.get(name);
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
}
