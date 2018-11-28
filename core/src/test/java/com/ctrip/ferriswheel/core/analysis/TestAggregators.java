package com.ctrip.ferriswheel.core.analysis;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.ErrorCode;
import com.ctrip.ferriswheel.core.intf.AggregateType;
import com.ctrip.ferriswheel.core.intf.Aggregator;
import junit.framework.TestCase;

public class TestAggregators extends TestCase {
    public void testSummary() {
        Aggregator agg = Aggregators.create(AggregateType.SUMMARY);
        assertTrue(agg instanceof Aggregators.SummaryAggregator);
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
        Aggregator agg = Aggregators.create(AggregateType.COUNT);
        assertTrue(agg instanceof Aggregators.CountAggregator);
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
        Aggregator agg = Aggregators.create(AggregateType.AVERAGE);
        assertTrue(agg instanceof Aggregators.AverageAggregator);
        assertTrue(AggregateType.AVERAGE.equals(agg.getType()));
        assertEquals(Value.err(ErrorCode.DIV_0), agg.getResult());
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
        Aggregator agg = Aggregators.create(AggregateType.MAXIMUM);
        assertTrue(agg instanceof Aggregators.MaximumAggregator);
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
        Aggregator agg = Aggregators.create(AggregateType.MINIMUM);
        assertTrue(agg instanceof Aggregators.MinimumAggregator);
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
        Aggregator agg = Aggregators.create(AggregateType.PRODUCT);
        assertTrue(agg instanceof Aggregators.ProductAggregator);
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
        Aggregator agg = Aggregators.create(AggregateType.DECIMAL_ONLY_COUNT);
        assertTrue(agg instanceof Aggregators.DecimalOnlyCountAggregator);
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
        Aggregator agg = Aggregators.create(AggregateType.STANDARD_DEVIATION);
        assertTrue(agg instanceof Aggregators.StandardDeviationAggregator);
        assertTrue(AggregateType.STANDARD_DEVIATION.equals(agg.getType()));
        assertEquals(Value.BLANK, agg.getResult());
        agg.feed(Value.dec(2));
        assertEquals(Value.err(ErrorCode.DIV_0), agg.getResult());
        agg.feed(Value.dec(3));
        assertEquals(0.707106781, agg.getResult().decimalValue().doubleValue(), 0.000000001);
        agg.feed(Value.dec(5));
        agg.feed(Value.dec(10));
        assertEquals(3.559026084, agg.getResult().decimalValue().doubleValue(), 0.000000001);
        agg.feed(Value.BLANK);
        assertEquals(3.559026084, agg.getResult().decimalValue().doubleValue(), 0.000000001);
    }

    public void testGlobalStandardDeviation() {
        // TODO
    }

    public void testVariance() {
        Aggregator agg = Aggregators.create(AggregateType.VARIANCE);
        assertTrue(agg instanceof Aggregators.VarianceAggregator);
        assertTrue(AggregateType.VARIANCE.equals(agg.getType()));
        assertEquals(Value.BLANK, agg.getResult());
        agg.feed(Value.dec(2));
        assertEquals(Value.err(ErrorCode.DIV_0), agg.getResult());
        agg.feed(Value.dec(3));
        assertEquals(0.5, agg.getResult().decimalValue().doubleValue(), 0.000000001);
        agg.feed(Value.dec(5));
        agg.feed(Value.dec(10));
        assertEquals(12.666666667, agg.getResult().decimalValue().doubleValue(), 0.000000001);
        agg.feed(Value.BLANK);
        assertEquals(12.666666667, agg.getResult().decimalValue().doubleValue(), 0.000000001);
    }

    public void testGlobalVariance() {
        // TODO
    }

}
