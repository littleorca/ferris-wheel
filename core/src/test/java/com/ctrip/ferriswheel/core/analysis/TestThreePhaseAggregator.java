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

package com.ctrip.ferriswheel.core.analysis;

import com.ctrip.ferriswheel.common.aggregate.NamedValuesSample;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import junit.framework.TestCase;

import java.util.Iterator;

/**
 * @author liuhaifeng
 */
public class TestThreePhaseAggregator extends TestCase {
    public void testIllegalFormula() {
        checkIllegalFormula("A");
        checkIllegalFormula("A+1");
//        checkIllegalFormula("3+1");
        checkIllegalFormula("A+B");
        checkIllegalFormula("IF(TRUE,2,3)");
        checkIllegalFormula("3+IF(TRUE,2,3)");
        checkIllegalFormula("A+COUNT(B)");
        checkIllegalFormula("COUNT(COUNT(B))");
    }

    protected void checkIllegalFormula(String formula) {
        try {
            new ThreePhaseAggregator(formula);
            fail();
        } catch (IllegalArgumentException e) {
            // expected.
        }
    }

    public void testNormalCase() {
        ThreePhaseAggregator agg = new ThreePhaseAggregator("SUM(A+1)/SUM(B)+10");
        agg.feed(new FakeSample(Value.dec(10), Value.dec(3), null));
        agg.feed(new FakeSample(Value.dec(12), Value.dec(7), null));
        Variant result = agg.getResult();
        assertEquals(12.4, result.doubleValue(), 0.00000001);

        agg = new ThreePhaseAggregator("SUM(A*2)+10/AVERAGE(B)+10");
        agg.feed(new FakeSample(Value.dec(10), Value.dec(3), null));
        agg.feed(new FakeSample(Value.dec(12), Value.dec(7), null));
        result = agg.getResult();
        assertEquals(56, result.intValue());
    }

    class FakeSample implements NamedValuesSample {
        private final Variant a;
        private final Variant b;
        private final Variant c;

        public FakeSample(Variant a, Variant b, Variant c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public int size() {
            return 3;
        }

        @Override
        public Variant getValue(String field) {
            if ("a".equalsIgnoreCase(field)) {
                return a;
            } else if ("b".equalsIgnoreCase(field)) {
                return b;
            } else if ("c".equalsIgnoreCase(field)) {
                return c;
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public Iterator<String> iterator() {
            return null;
        }
    }
}
