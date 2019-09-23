package com.ctrip.ferriswheel.core.analysis;

import com.ctrip.ferriswheel.common.aggregate.AggregateType;
import com.ctrip.ferriswheel.common.aggregate.Aggregator;
import com.ctrip.ferriswheel.common.aggregate.Sample;
import com.ctrip.ferriswheel.common.aggregate.SingleValueSample;
import com.ctrip.ferriswheel.common.variant.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public abstract class PlainAggregator implements Aggregator {
    protected final MathContext DEFAULT_MC = MathContext.DECIMAL64;
    protected transient ErrorCode errorCode = null;
    protected transient Variant cachedResult = null;

    public static PlainAggregator create(AggregateType type) {
        switch (type) {
            case SUMMARY:
                return new SummaryAggregator();
            case COUNT:
                return new CountAggregator();
            case AVERAGE:
                return new AverageAggregator();
            case MAXIMUM:
                return new MaximumAggregator();
            case MINIMUM:
                return new MinimumAggregator();
            case PRODUCT:
                return new ProductAggregator();
            case DECIMAL_ONLY_COUNT:
                return new DecimalOnlyCountAggregator();
            case STANDARD_DEVIATION:
                return new StandardDeviationAggregator();
            case STANDARD_DEVIATION_POPULATION:
                return new StandardDeviationPopulationAggregator();
            case VARIANCE:
                return new VarianceAggregator();
            case VARIANCE_POPULATION:
                return new VariancePopulationAggregator();
            default:
                throw new UnsupportedOperationException("Unsupported type: " + type);
        }
    }

    public void feed(Variant value) {
        feed(new VariantSample(value));
    }

    @Override
    public void feed(Sample sample) {
        if (!(sample instanceof SingleValueSample)) {
            throw new IllegalArgumentException();
        }
        Variant variant = ((SingleValueSample) sample).getValue();
        cachedResult = null;
        if (errorCode != null) {
            return;
        }
        if (variant.valueType() == VariantType.ERROR) {
            errorCode = variant.errorValue();
            return;
        }
        doFeed(variant);
    }

    protected abstract void doFeed(Variant variant);

    @Override
    public Variant getResult() {
        if (cachedResult == null) {
            if (errorCode != null) {
                cachedResult = Value.err(errorCode);
            } else {
                cachedResult = calcResult();
            }
        }
        return cachedResult;
    }

    protected abstract Variant calcResult();

    protected static abstract class SumAvgSupport extends PlainAggregator implements Aggregator {
        protected BigDecimal summary = new BigDecimal(0);
        protected int count = 0;

        protected void doFeed(Variant variant) {
            if (variant.valueType() != VariantType.DECIMAL) {
                return;
            }
            if (summary == null) {
                summary = variant.decimalValue();
            } else {
                summary = summary.add(variant.decimalValue(), DEFAULT_MC);
            }
            count++;
        }
    }

    public static class SummaryAggregator extends SumAvgSupport implements Aggregator {
        public AggregateType getType() {
            return AggregateType.SUMMARY;
        }

        protected Variant calcResult() {
            return Value.dec(summary);
        }
    }

    public static class CountAggregator extends PlainAggregator implements Aggregator {
        private int count = 0;

        public AggregateType getType() {
            return AggregateType.COUNT;
        }

        protected void doFeed(Variant variant) {
            if (!variant.isBlank()) {
                count++;
            }
        }

        protected Variant calcResult() {
            return Value.dec(count);
        }
    }

    public static class AverageAggregator extends SumAvgSupport implements Aggregator {
        public AggregateType getType() {
            return AggregateType.AVERAGE;
        }

        protected Variant calcResult() {
            return count == 0 ? Value.err(ErrorCodes.DIV)
                    : Value.dec(summary.divide(new BigDecimal(count), DEFAULT_MC));
        }
    }

    public static class MaximumAggregator extends PlainAggregator implements Aggregator {
        private BigDecimal value;

        public AggregateType getType() {
            return AggregateType.MAXIMUM;
        }

        protected void doFeed(Variant variant) {
            if (variant.valueType() != VariantType.DECIMAL) {
                return; // TODO support max string?
            }
            if (value == null) {
                value = variant.decimalValue();
            } else if (value.compareTo(variant.decimalValue()) < 0) {
                value = variant.decimalValue();
            }
        }

        protected Variant calcResult() {
            return value == null ? Value.BLANK : Value.dec(value);
        }
    }

    public static class MinimumAggregator extends PlainAggregator implements Aggregator {
        private BigDecimal value;

        public AggregateType getType() {
            return AggregateType.MINIMUM;
        }

        protected void doFeed(Variant variant) {
            if (variant.valueType() != VariantType.DECIMAL) {
                return; // TODO support min string?
            }
            if (value == null) {
                value = variant.decimalValue();
            } else if (value.compareTo(variant.decimalValue()) > 0) {
                value = variant.decimalValue();
            }
        }

        protected Variant calcResult() {
            return value == null ? Value.BLANK : Value.dec(value);
        }
    }

    public static class ProductAggregator extends PlainAggregator implements Aggregator {
        private BigDecimal value;

        public AggregateType getType() {
            return AggregateType.PRODUCT;
        }

        protected void doFeed(Variant variant) {
            if (variant.valueType() != VariantType.DECIMAL) {
                return;
            }
            if (value == null) {
                value = variant.decimalValue();
            } else {
                value = value.multiply(variant.decimalValue(), DEFAULT_MC);
            }
        }

        protected Variant calcResult() {
            return value == null ? Value.BLANK : Value.dec(value);
        }
    }

    public static class DecimalOnlyCountAggregator extends PlainAggregator implements Aggregator {
        private int count = 0;

        public AggregateType getType() {
            return AggregateType.DECIMAL_ONLY_COUNT;
        }

        protected void doFeed(Variant variant) {
            if (variant.valueType() == VariantType.DECIMAL) {
                count++;
            }
        }

        protected Variant calcResult() {
            return Value.dec(count);
        }
    }

    public static abstract class ComplexAggSupport extends PlainAggregator implements Aggregator {
        protected List<BigDecimal> values = new ArrayList<>();

        protected void doFeed(Variant variant) {
            if (variant.valueType() != VariantType.DECIMAL) {
                return;
            }
            values.add(variant.decimalValue());
        }
    }

    public static class StandardDeviationAggregator extends VarianceAggregator implements Aggregator {
        public AggregateType getType() {
            return AggregateType.STANDARD_DEVIATION;
        }

        protected Variant calcResult() {
            Variant result = super.calcResult();
            if (result == null || result.isBlank() || !result.isValid()) {
                return result;
            }
            return Value.dec(new BigDecimal(Math.sqrt(result.doubleValue()), DEFAULT_MC));
        }
    }

    public static class StandardDeviationPopulationAggregator extends VariancePopulationAggregator implements Aggregator {
        public AggregateType getType() {
            return AggregateType.STANDARD_DEVIATION_POPULATION;
        }

        @Override
        protected Variant calcResult() {
            Variant result = super.calcResult();
            if (result == null || result.isBlank() || !result.isValid()) {
                return result;
            }
            return Value.dec(new BigDecimal(Math.sqrt(result.doubleValue()), DEFAULT_MC));
        }
    }

    public static class VarianceAggregator extends ComplexAggSupport implements Aggregator {
        public AggregateType getType() {
            return AggregateType.VARIANCE;
        }

        protected Variant calcResult() {
            if (values.isEmpty()) {
                return Value.BLANK;
            } else if (values.size() == 1) {
                return Value.err(ErrorCodes.DIV);
            }
            BigDecimal tmp = new BigDecimal(0);
            for (BigDecimal value : values) {
                tmp = tmp.add(value);
            }
            BigDecimal mean = tmp.divide(new BigDecimal(values.size()), DEFAULT_MC);
            tmp = new BigDecimal(0);
            for (BigDecimal value : values) {
                tmp = tmp.add(value.subtract(mean, DEFAULT_MC).pow(2, DEFAULT_MC));
            }
            return Value.dec(tmp.divide(new BigDecimal(values.size() - 1), DEFAULT_MC));
        }
    }

    public static class VariancePopulationAggregator extends ComplexAggSupport implements Aggregator {
        public AggregateType getType() {
            return AggregateType.VARIANCE_POPULATION;
        }

        protected Variant calcResult() {
            if (values.isEmpty()) {
                return Value.BLANK;
            }
            BigDecimal tmp = new BigDecimal(0);
            for (BigDecimal value : values) {
                tmp = tmp.add(value);
            }
            BigDecimal mean = tmp.divide(new BigDecimal(values.size()), DEFAULT_MC);
            tmp = new BigDecimal(0);
            for (BigDecimal value : values) {
                tmp = tmp.add(value.subtract(mean, DEFAULT_MC).pow(2, DEFAULT_MC));
            }
            return Value.dec(tmp.divide(new BigDecimal(values.size()), DEFAULT_MC));
        }
    }

}
