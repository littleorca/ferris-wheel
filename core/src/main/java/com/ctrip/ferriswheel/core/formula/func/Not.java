package com.ctrip.ferriswheel.core.formula.func;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.ErrorCodes;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.api.variant.Variant;


public class Not implements Function {
    public static final String NOT = "NOT";

    @Override
    public String getName() {
        return NOT;
    }

    @Override
    public boolean checkArgc(int argc) {
        return argc == 1;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Variant condition = context.popOperand();
        Boolean booleanValue;
        try {
            booleanValue = !condition.booleanValue();
        } catch (RuntimeException e) {
            booleanValue = null;
        }
        context.pushOperand(booleanValue == null ?
                Value.err(ErrorCodes.ILLEGAL_VALUE) : Value.bool(booleanValue));
    }

}
