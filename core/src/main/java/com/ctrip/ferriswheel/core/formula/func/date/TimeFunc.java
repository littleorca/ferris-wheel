package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;
import com.ctrip.ferriswheel.api.variant.Variant;

import java.util.Calendar;

/**
 * TIME(hour, minute, second)
 */
public class TimeFunc implements Function {
    public static final String TIME = "TIME";

    @Override
    public String getName() {
        return TIME;
    }

    @Override
    public boolean checkArgc(int argc) {
        return argc == 3;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Variant s = context.popOperand();
        Variant m = context.popOperand();
        Variant h = context.popOperand();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.HOUR_OF_DAY, h.intValue());
        cal.set(Calendar.MINUTE, m.intValue());
        cal.set(Calendar.SECOND, s.intValue());
        context.pushOperand(Value.date(cal.getTime()));
    }
}
