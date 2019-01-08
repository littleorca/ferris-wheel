package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;
import com.ctrip.ferriswheel.common.variant.Variant;

import java.util.Calendar;

/**
 * DATE(y, m d)
 */
public class DateFunc implements Function {
    public static final String DATE = "DATE";

    @Override
    public String getName() {
        return DATE;
    }

    @Override
    public boolean checkArgc(int argc) {
        return argc == 3;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Variant d = context.popOperand();
        Variant m = context.popOperand();
        Variant y = context.popOperand();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(y.intValue(), m.intValue() - 1, d.intValue());
        context.pushOperand(Value.date(cal.getTime()));
    }
}
