package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.quarks.Token;

public class DelimiterElement extends FormulaElement {

    public DelimiterElement(Token token, String value) {
        super(token, value);
    }

    public String getDelimiter() {
        return getTokenString();
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
    }

}
