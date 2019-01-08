package com.ctrip.ferriswheel.core.formula.func;

import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.common.variant.impl.ErrorCodes;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.common.variant.Variant;

public class If implements Function {
    public static final String IF = "IF";

    @Override
    public String getName() {
        return IF;
    }

    @Override
    public boolean checkArgc(int argc) {
        return argc == 2 || argc == 3;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Variant falseCase = Value.BLANK;
        if (element.getArgc() == 3) {
            falseCase = context.popOperand();
        }
        Variant trueCase = context.popOperand();
        Variant test = context.popOperand();

        if (test instanceof Value.BooleanValue || test instanceof Value.DecimalValue) {
            if (Boolean.TRUE.equals(test.booleanValue())) {
                context.pushOperand(trueCase);
            } else {
                context.pushOperand(falseCase);
            }

        } else {
            context.pushOperand(Value.err(ErrorCodes.UNKNOWN)); // TODO error code
        }
    }

}
