package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;

import java.util.Calendar;

public class WeekNum implements Function {
    public static final String WEEKNUM = "WEEKNUM";

    @Override
    public String getName() {
        return WEEKNUM;
    }

    @Override
    public boolean checkArgc(int argc) {
        return argc == 1;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Variant date = context.popOperand();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date.dateValue());
        int n = cal.get(Calendar.WEEK_OF_YEAR);
        context.pushOperand(Value.dec(n));
    }
}
