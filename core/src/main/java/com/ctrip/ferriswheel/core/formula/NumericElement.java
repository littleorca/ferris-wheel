package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.quarks.Token;

import java.math.BigDecimal;

public class NumericElement extends FormulaElement {
    private BigDecimal value;

    public NumericElement(Token token, BigDecimal value) {
        super(token, null);
        this.value = value;
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        context.pushOperand(new Value.DecimalValue(value));
    }
}
