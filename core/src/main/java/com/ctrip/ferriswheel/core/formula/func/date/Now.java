package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;

import java.util.Date;

public class Now implements Function {
    public static final String NOW = "NOW";

    public Now() {
    }

    @Override
    public String getName() {
        return NOW;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        context.pushOperand(Value.date(new Date()));
    }

    @Override
    public boolean isVolatile() {
        return true;
    }

    @Override
    public boolean checkArgc(int argc) {
        return (argc == 0);
    }
}
