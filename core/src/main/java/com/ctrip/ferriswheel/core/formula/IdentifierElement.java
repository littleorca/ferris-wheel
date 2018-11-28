package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.quarks.Token;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;

public class IdentifierElement extends FormulaElement {

    public IdentifierElement(Token token, String identifier) {
        super(token, identifier);
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        //TODO
    }
}
