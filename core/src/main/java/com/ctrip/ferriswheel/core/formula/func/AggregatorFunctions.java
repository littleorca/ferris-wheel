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
 *
 */

package com.ctrip.ferriswheel.core.formula.func;

import com.ctrip.ferriswheel.common.aggregate.Aggregator;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.analysis.PlainAggregator;
import com.ctrip.ferriswheel.core.analysis.VariantSample;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;

import java.util.List;

/**
 * @author liuhaifeng
 */
public abstract class AggregatorFunctions implements Function {

    @Override
    public boolean checkArgc(int argc) {
        return argc > 0;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Aggregator aggregator = getAggregator();

        for (int i = 0; i < element.getArgc(); i++) {
            Variant elem = context.popOperand();
            if (elem instanceof Value.ListValue) {
                List<Variant> valueList = elem.listValue();
                for (Variant val : valueList) {
                    aggregator.feed(new VariantSample(val));
                }

            } else {
                aggregator.feed(new VariantSample(elem));
            }
        }

        context.pushOperand(aggregator.getResult());
    }

    @Override
    public boolean isAggregator() {
        return true;
    }

    @Override
    public abstract Aggregator getAggregator();

    public static class Count extends AggregatorFunctions {
        public static final String COUNT = "COUNT";

        @Override
        public String getName() {
            return COUNT;
        }

        @Override
        public Aggregator getAggregator() {
            return new PlainAggregator.CountAggregator();
        }
    }

    public static class Sum extends AggregatorFunctions {
        public static final String SUM = "SUM";

        @Override
        public String getName() {
            return SUM;
        }

        @Override
        public Aggregator getAggregator() {
            return new PlainAggregator.SummaryAggregator();
        }
    }

    public static class Average extends AggregatorFunctions {
        public static final String AVERAGE = "AVERAGE";

        @Override
        public String getName() {
            return AVERAGE;
        }

        @Override
        public Aggregator getAggregator() {
            return new PlainAggregator.AverageAggregator();
        }
    }

    public static class Max extends AggregatorFunctions {
        public static final String MAX = "MAX";

        @Override
        public String getName() {
            return MAX;
        }

        @Override
        public Aggregator getAggregator() {
            return new PlainAggregator.MaximumAggregator();
        }
    }

    public static class Min extends AggregatorFunctions {
        public static final String MIN = "MIN";

        @Override
        public String getName() {
            return MIN;
        }

        @Override
        public Aggregator getAggregator() {
            return new PlainAggregator.MinimumAggregator();
        }
    }

    public static class StDev extends AggregatorFunctions {
        public static final String STDEV = "STDEV";

        @Override
        public String getName() {
            return STDEV;
        }

        @Override
        public Aggregator getAggregator() {
            return new PlainAggregator.StandardDeviationAggregator();
        }
    }

    public static class StDevP extends AggregatorFunctions {
        public static final String STDEVP = "STDEVP";

        @Override
        public String getName() {
            return STDEVP;
        }

        @Override
        public Aggregator getAggregator() {
            return new PlainAggregator.StandardDeviationPopulationAggregator();
        }
    }

    public static class Var extends AggregatorFunctions {
        public static final String VAR = "VAR";

        @Override
        public String getName() {
            return VAR;
        }

        @Override
        public Aggregator getAggregator() {
            return new PlainAggregator.VarianceAggregator();
        }
    }

    public static class VarP extends AggregatorFunctions {
        public static final String VARP = "VARP";

        @Override
        public String getName() {
            return VARP;
        }

        @Override
        public Aggregator getAggregator() {
            return new PlainAggregator.VariancePopulationAggregator();
        }
    }
}
