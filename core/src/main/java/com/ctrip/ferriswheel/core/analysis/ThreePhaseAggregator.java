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

import com.ctrip.ferriswheel.common.aggregate.AggregateType;
import com.ctrip.ferriswheel.common.aggregate.Aggregator;
import com.ctrip.ferriswheel.common.aggregate.NamedValuesSample;
import com.ctrip.ferriswheel.common.aggregate.Sample;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.asset.Asset;
import com.ctrip.ferriswheel.core.formula.*;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluator;
import com.ctrip.ferriswheel.core.formula.eval.ReferenceResolver;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.NameReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liuhaifeng
 */
public class ThreePhaseAggregator implements Aggregator {
    private List<FormulaElement[]> preAggs;
    private List<Aggregator> aggs;
    private FormulaElement[] postFormula;

    public ThreePhaseAggregator(String formulaString) {
        this(FormulaParser.parse(formulaString));
    }

    ThreePhaseAggregator(FormulaElement[] formulaElements) {
        this.preAggs = new ArrayList<>();
        this.aggs = new ArrayList<>();

        List<FormulaElement> tmpPostFormula = new ArrayList<>();

        int offset = 0;
        for (int i = 0; i < formulaElements.length; i++) {
            FormulaElement elem = formulaElements[i];
            if (elem instanceof FuncElement && ((FuncElement) elem).getFunction().isAggregator()) {
                int arcStartPos = i - (elem.getSlices() - 1);
                if (arcStartPos < offset) {
                    throw new IllegalArgumentException("Cannot aggregate multiple rounds.");
                }
                for (; offset < arcStartPos; offset++) {
                    tmpPostFormula.add(formulaElements[offset]);
                }
                if (arcStartPos < i) {
                    preAggs.add(Arrays.copyOfRange(formulaElements, arcStartPos, i));
                } else {
                    preAggs.add(new FormulaElement[0]);
                }
                aggs.add(((FuncElement) elem).getFunction().getAggregator());
                tmpPostFormula.add(new AggHolder(aggs.size() - 1));
                offset = i + 1;
            }
        }

        for (; offset < formulaElements.length; offset++) {
            tmpPostFormula.add(formulaElements[offset]);
        }

        this.postFormula = tmpPostFormula.toArray(new FormulaElement[tmpPostFormula.size()]);

        // check preAggs
        for (FormulaElement[] slice : preAggs) {

        }

        // check postFormula
        if (postFormula == null || postFormula.length < 1) {
            throw new IllegalArgumentException("Illegal formula (1).");
        }
        for (FormulaElement elem : postFormula) {
            if (elem instanceof ReferenceElement) {
                throw new IllegalArgumentException("Illegal formula (2).");
            }
        }
    }

    @Override
    public AggregateType getType() {
        return AggregateType.CUSTOM;
    }

    @Override
    public void feed(Sample sample) {
        if (!(sample instanceof NamedValuesSample)) {
            throw new IllegalArgumentException();
        }
        FormulaEvaluator evaluator = new FormulaEvaluator(new FieldReferenceResolver((NamedValuesSample) sample));

        for (int i = 0; i < preAggs.size(); i++) {
            FormulaElement[] slice = preAggs.get(i);
            List<Variant> argv = evaluator.evaluatePartial(slice);
            Aggregator agg = this.aggs.get(i);
            for (Variant arg : argv) {
                agg.feed(new VariantSample(arg));
            }
        }
    }

    @Override
    public Variant getResult() {
        FormulaEvaluator evaluator = new FormulaEvaluator(new DummyResolver());
        return evaluator.evaluate(postFormula);
    }

    List<FormulaElement[]> getPreAggs() {
        return preAggs;
    }

    void setPreAggs(List<FormulaElement[]> preAggs) {
        this.preAggs = preAggs;
    }

    List<Aggregator> getAggs() {
        return aggs;
    }

    void setAggs(List<Aggregator> aggs) {
        this.aggs = aggs;
    }

    FormulaElement[] getPostFormula() {
        return postFormula;
    }

    void setPostFormula(FormulaElement[] postFormula) {
        this.postFormula = postFormula;
    }

    class AggHolder extends FormulaElement {
        private int index;

        public AggHolder(int index) {
            this.index = index;
        }

        @Override
        public void evaluate(FormulaEvaluationContext context) {
            Variant result = ThreePhaseAggregator.this.aggs.get(index).getResult();
            context.pushOperand(result);
        }
    }

    class FieldReferenceResolver implements ReferenceResolver {
        private final NamedValuesSample sample;

        public FieldReferenceResolver(NamedValuesSample sample) {
            this.sample = sample;
        }

        @Override
        public Variant resolve(CellReferenceElement referenceElement, FormulaEvaluationContext context) {
            return sample.getValue(referenceElement.getTokenString());
        }

        @Override
        public Variant resolve(RangeReferenceElement referenceElement, FormulaEvaluationContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Variant resolve(NameReferenceElement referenceElement, FormulaEvaluationContext context) {
            return sample.getValue(referenceElement.getTokenString());
        }

        @Override
        public Asset getReferredAsset(CellReference cellReference) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Asset getReferredAsset(NameReference nameReference) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Asset getAssetById(long assetId) {
            throw new UnsupportedOperationException();
        }
    }

    class DummyResolver implements ReferenceResolver {
        @Override
        public Variant resolve(CellReferenceElement referenceElement, FormulaEvaluationContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Variant resolve(RangeReferenceElement referenceElement, FormulaEvaluationContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Variant resolve(NameReferenceElement referenceElement, FormulaEvaluationContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Asset getReferredAsset(CellReference cellReference) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Asset getReferredAsset(NameReference nameReference) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Asset getAssetById(long assetId) {
            throw new UnsupportedOperationException();
        }
    }
}
