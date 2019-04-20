package com.ctrip.ferriswheel.core.analysis;

import com.ctrip.ferriswheel.common.aggregate.AggregateType;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import junit.framework.TestCase;

public class TestPlainAggregator extends TestCase {
    public void testSummary() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.SUMMARY);
        assertTrue(agg instanceof PlainAggregator.SummaryAggregator);
        assertTrue(AggregateType.SUMMARY.equals(agg.getType()));
        assertEquals(Value.dec(0), agg.getResult());
        agg.feed(Value.dec(10));
        assertEquals(Value.dec(10), agg.getResult());
        agg.feed(Value.dec(15));
        assertEquals(Value.dec(25), agg.getResult());
        agg.feed(Value.str("foobar"));
        assertEquals(Value.dec(25), agg.getResult());
        agg.feed(Value.BLANK);
        assertEquals(Value.dec(25), agg.getResult());
    }

    public void testCount() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.COUNT);
        assertTrue(agg instanceof PlainAggregator.CountAggregator);
        assertTrue(AggregateType.COUNT.equals(agg.getType()));
        assertEquals(Value.dec(0), agg.getResult());
        agg.feed(Value.dec(10));
        assertEquals(Value.dec(1), agg.getResult());
        agg.feed(Value.dec(15));
        assertEquals(Value.dec(2), agg.getResult());
        agg.feed(Value.str("foobar"));
        assertEquals(Value.dec(3), agg.getResult());
        agg.feed(Value.BLANK);
        assertEquals(Value.dec(3), agg.getResult());
    }

    public void testAverage() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.AVERAGE);
        assertTrue(agg instanceof PlainAggregator.AverageAggregator);
        assertTrue(AggregateType.AVERAGE.equals(agg.getType()));
        assertEquals(Value.err(ErrorCodes.DIV), agg.getResult());
        agg.feed(Value.dec(10));
        assertEquals(Value.dec(10), agg.getResult());
        agg.feed(Value.dec(15));
        assertEquals(Value.dec(12.5), agg.getResult());
        agg.feed(Value.str("foobar"));
        assertEquals(Value.dec(12.5), agg.getResult());
        agg.feed(Value.BLANK);
        assertEquals(Value.dec(12.5), agg.getResult());
    }

    public void testMaximum() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.MAXIMUM);
        assertTrue(agg instanceof PlainAggregator.MaximumAggregator);
        assertTrue(AggregateType.MAXIMUM.equals(agg.getType()));
        assertEquals(Value.BLANK, agg.getResult());
        agg.feed(Value.dec(10));
        assertEquals(Value.dec(10), agg.getResult());
        agg.feed(Value.dec(15));
        assertEquals(Value.dec(15), agg.getResult());
        agg.feed(Value.str("foobar"));
        assertEquals(Value.dec(15), agg.getResult());
        agg.feed(Value.BLANK);
        assertEquals(Value.dec(15), agg.getResult());
        agg.feed(Value.dec(12));
        assertEquals(Value.dec(15), agg.getResult());
        agg.feed(Value.dec(17));
        assertEquals(Value.dec(17), agg.getResult());
    }

    public void testMinimum() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.MINIMUM);
        assertTrue(agg instanceof PlainAggregator.MinimumAggregator);
        assertTrue(AggregateType.MINIMUM.equals(agg.getType()));
        assertEquals(Value.BLANK, agg.getResult());
        agg.feed(Value.dec(10));
        assertEquals(Value.dec(10), agg.getResult());
        agg.feed(Value.dec(15));
        assertEquals(Value.dec(10), agg.getResult());
        agg.feed(Value.str("foobar"));
        assertEquals(Value.dec(10), agg.getResult());
        agg.feed(Value.BLANK);
        assertEquals(Value.dec(10), agg.getResult());
        agg.feed(Value.dec(8));
        assertEquals(Value.dec(8), agg.getResult());
        agg.feed(Value.dec(17));
        assertEquals(Value.dec(8), agg.getResult());
    }

    public void testProduct() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.PRODUCT);
        assertTrue(agg instanceof PlainAggregator.ProductAggregator);
        assertTrue(AggregateType.PRODUCT.equals(agg.getType()));
        assertEquals(Value.BLANK, agg.getResult());
        agg.feed(Value.dec(10));
        assertEquals(Value.dec(10), agg.getResult());
        agg.feed(Value.dec(15));
        assertEquals(Value.dec(150), agg.getResult());
        agg.feed(Value.str("foobar"));
        assertEquals(Value.dec(150), agg.getResult());
        agg.feed(Value.BLANK);
        assertEquals(Value.dec(150), agg.getResult());
        agg.feed(Value.dec(8));
        assertEquals(Value.dec(1200), agg.getResult());
    }

    public void testDecimalOnlyCount() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.DECIMAL_ONLY_COUNT);
        assertTrue(agg instanceof PlainAggregator.DecimalOnlyCountAggregator);
        assertTrue(AggregateType.DECIMAL_ONLY_COUNT.equals(agg.getType()));
        assertEquals(Value.dec(0), agg.getResult());
        agg.feed(Value.dec(10));
        assertEquals(Value.dec(1), agg.getResult());
        agg.feed(Value.dec(15));
        assertEquals(Value.dec(2), agg.getResult());
        agg.feed(Value.str("foobar"));
        assertEquals(Value.dec(2), agg.getResult());
        agg.feed(Value.BLANK);
        assertEquals(Value.dec(2), agg.getResult());
    }

    public void testStandardDeviation() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.STANDARD_DEVIATION);
        assertTrue(agg instanceof PlainAggregator.StandardDeviationAggregator);
        assertTrue(AggregateType.STANDARD_DEVIATION.equals(agg.getType()));
        assertEquals(Value.BLANK, agg.getResult());
        agg.feed(Value.dec(2));
        assertEquals(Value.err(ErrorCodes.DIV), agg.getResult());
        agg.feed(Value.dec(3));
        assertEquals(0.707106781, agg.getResult().doubleValue(), 0.000000001);
        agg.feed(Value.dec(5));
        agg.feed(Value.dec(10));
        assertEquals(3.559026084, agg.getResult().doubleValue(), 0.000000001);
        agg.feed(Value.BLANK);
        assertEquals(3.559026084, agg.getResult().doubleValue(), 0.000000001);
    }

    public void testStandardDeviationPopulation() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.STANDARD_DEVIATION_POPULATION);
        assertTrue(agg instanceof PlainAggregator.StandardDeviationPopulationAggregator);
        assertTrue(AggregateType.STANDARD_DEVIATION_POPULATION.equals(agg.getType()));
        assertEquals(Value.BLANK, agg.getResult());
        agg.feed(Value.dec(2));
        assertEquals(0, agg.getResult().doubleValue(), 0.000000001);
        agg.feed(Value.dec(3));
        assertEquals(0.5, agg.getResult().doubleValue(), 0.000000001);
        agg.feed(Value.dec(5));
        agg.feed(Value.dec(10));
        assertEquals(3.082207001484488, agg.getResult().doubleValue(), 0.000000001);
        agg.feed(Value.BLANK);
        assertEquals(3.082207001484488, agg.getResult().doubleValue(), 0.000000001);
    }

    public void testVariance() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.VARIANCE);
        assertTrue(agg instanceof PlainAggregator.VarianceAggregator);
        assertTrue(AggregateType.VARIANCE.equals(agg.getType()));
        assertEquals(Value.BLANK, agg.getResult());
        agg.feed(Value.dec(2));
        assertEquals(Value.err(ErrorCodes.DIV), agg.getResult());
        agg.feed(Value.dec(3));
        assertEquals(0.5, agg.getResult().decimalValue().doubleValue(), 0.000000001);
        agg.feed(Value.dec(5));
        agg.feed(Value.dec(10));
        assertEquals(12.666666667, agg.getResult().decimalValue().doubleValue(), 0.000000001);
        agg.feed(Value.BLANK);
        assertEquals(12.666666667, agg.getResult().decimalValue().doubleValue(), 0.000000001);
    }

    public void testVariancePopulation() {
        PlainAggregator agg = PlainAggregator.create(AggregateType.VARIANCE_POPULATION);
        assertTrue(agg instanceof PlainAggregator.VariancePopulationAggregator);
        assertTrue(AggregateType.VARIANCE_POPULATION.equals(agg.getType()));
        assertEquals(Value.BLANK, agg.getResult());
        agg.feed(Value.dec(2));
        assertEquals(0, agg.getResult().doubleValue(), 0.000000001);
        agg.feed(Value.dec(3));
        assertEquals(0.25, agg.getResult().doubleValue(), 0.000000001);
        agg.feed(Value.dec(5));
        agg.feed(Value.dec(10));
        assertEquals(9.5, agg.getResult().doubleValue(), 0.000000001);
        agg.feed(Value.BLANK);
        assertEquals(9.5, agg.getResult().doubleValue(), 0.000000001);
    }

}
