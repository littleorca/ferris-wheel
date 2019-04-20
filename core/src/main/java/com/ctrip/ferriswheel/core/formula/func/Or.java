package com.ctrip.ferriswheel.core.formula.func;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;

import java.util.ArrayList;
import java.util.List;


public class Or implements Function {
    public static final String OR = "OR";

    @Override
    public String getName() {
        return OR;
    }

    @Override
    public boolean checkArgc(int argc) {
        return argc > 0;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Boolean result = true;
        List<Variant> operands = new ArrayList<>(element.getArgc());
        for (int i = 0; i < element.getArgc(); i++) {
            operands.add(context.popOperand());
        }
        for (Variant condition : operands) {
            if (Boolean.FALSE.equals(result)) {
                try {
                    result |= condition.booleanValue();
                } catch (RuntimeException e) {
                    result = null;
                }
            }
        }
        context.pushOperand(result == null ?
                Value.err(ErrorCodes.VALUE) : Value.bool(result));
    }

}
