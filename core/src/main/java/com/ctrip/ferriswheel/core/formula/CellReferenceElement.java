package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.ref.CellReference;

public class CellReferenceElement extends ReferenceElement {
    private CellReference cellReference;

    public CellReferenceElement(CellReference cellReference) {
        this.cellReference = cellReference;
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        context.pushOperand(context.resolveReference(this));
    }

    @Override
    public String toString() {
        return cellReference.toString();
    }

    public CellReference getCellReference() {
        return cellReference;
    }

    void setCellReference(CellReference cellReference) {
        this.cellReference = cellReference;
    }

}
