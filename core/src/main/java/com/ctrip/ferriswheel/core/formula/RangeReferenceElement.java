package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.ref.RangeReference;

public class RangeReferenceElement extends ReferenceElement {
    private RangeReference rangeReference;

    public RangeReferenceElement(RangeReference rangeReference) {
        this.rangeReference = rangeReference;
    }


    @Override
    public void evaluate(FormulaEvaluationContext context) {
        context.pushOperand(context.resolveReference(this));
    }

    @Override
    public String toString() {
        return rangeReference.toString();
    }

    public RangeReference getRangeReference() {
        return rangeReference;
    }

    void setRangeReference(RangeReference rangeReference) {
        this.rangeReference = rangeReference;
    }

}
