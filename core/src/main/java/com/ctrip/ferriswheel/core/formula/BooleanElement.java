package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.quarks.Token;

public class BooleanElement extends FormulaElement {
    private boolean booleanValue;

    public BooleanElement(Token token, boolean booleanValue) {
        super(token, null);
        this.booleanValue = booleanValue;
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        context.pushOperand(Value.bool(booleanValue));
    }

}
