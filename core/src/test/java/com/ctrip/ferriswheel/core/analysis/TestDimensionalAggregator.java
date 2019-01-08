package com.ctrip.ferriswheel.core.analysis;

import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.common.table.AggregateType;
import com.ctrip.ferriswheel.common.variant.Variant;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class TestDimensionalAggregator extends TestCase {
    public void testDimensionalAggregate() {
        AggregateType[] types = new AggregateType[]{
                AggregateType.SUMMARY,
                AggregateType.COUNT,
                AggregateType.VARIANCE
        };

        DimensionalAggregator aggregator = new DimensionalAggregator(types);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    aggregator.newRecord()
                            .dim("da", Value.str("da" + i))
                            .dim("db", Value.str("db" + j))
                            .dim("dc", Value.str("dc" + k))
                            .val(0, Value.dec(i * j * k))
                            .val(1, Value.dec(i * j * k))
                            .val(2, Value.dec(i * j * k))
                            .commit();

                    aggregator.newRecord()
                            .dim("da", Value.str("da" + i))
                            .dim("db", Value.str("db" + j))
                            .dim("dc", Value.str("dc" + k))
                            .val(0, Value.dec(i + j + k))
                            .val(1, Value.dec(i + j + k))
                            .val(2, Value.dec(i + j + k))
                            .commit();
                }
            }
        }

        assertEquals(3, aggregator.getAllDimensions().size());
        assertEquals(2, aggregator.getAllDimensions().get("da").size());
        assertEquals(2, aggregator.getAllDimensions().get("db").size());
        assertEquals(2, aggregator.getAllDimensions().get("dc").size());

        Map<String, Variant> dimensions = new HashMap<>();
        dimensions.put("da", Value.str("da0"));
        dimensions.put("db", Value.str("db0"));
        dimensions.put("dc", Value.str("dc0"));
        Variant[] values = aggregator.getValues(dimensions);
        assertEquals(3, values.length);
        assertEquals(0, values[0].intValue());
        assertEquals(2, values[1].intValue());
        assertEquals(0, values[2].doubleValue(), 0.00000001);

        dimensions.put("da", Value.str("da1"));
        dimensions.put("db", Value.str("db1"));
        dimensions.put("dc", Value.str("dc1"));
        values = aggregator.getValues(dimensions);
        assertEquals(3, values.length);
        assertEquals(4, values[0].intValue());
        assertEquals(2, values[1].intValue());
        assertEquals(2, values[2].doubleValue(), 0.00000001);

        dimensions.put("da", Value.str("da0"));
        dimensions.put("db", Value.str("db1"));
        dimensions.put("dc", Value.str("dc0"));
        values = aggregator.getValues(dimensions);
        assertEquals(3, values.length);
        assertEquals(1, values[0].intValue());
        assertEquals(2, values[1].intValue());
        assertEquals(0.5, values[2].doubleValue(), 0.00000001);

        // Do we need to check all of them?
    }
}
