package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.ref.CellRef;

public class CellReferenceElement extends ReferenceElement {
    private CellRef cellRef;

    public CellReferenceElement(CellRef cellRef) {
        this.cellRef = cellRef;
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        context.pushOperand(context.resolveReference(cellRef));
    }

    @Override
    public String toString() {
        return cellRef.toString();
    }

    public CellRef getCellRef() {
        return cellRef;
    }

    void setCellRef(CellRef cellRef) {
        this.cellRef = cellRef;
    }

}
