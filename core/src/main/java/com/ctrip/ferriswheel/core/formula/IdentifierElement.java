package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.quarks.Token;

public class IdentifierElement extends FormulaElement {

    public IdentifierElement(Token token, String identifier) {
        super(token, identifier);
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        throw new RuntimeException("Should not be called.");
    }
}
