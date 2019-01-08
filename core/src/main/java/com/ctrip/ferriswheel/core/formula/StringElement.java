package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.quarks.Token;

public class StringElement extends FormulaElement {
    public StringElement(Token token, String value) {
        super(token, value);
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        context.pushOperand(new Value.StrValue(getTokenString()));
    }
}
