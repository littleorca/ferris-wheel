package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;

import java.util.Calendar;
import java.util.Locale;

/**
 * TODAY()
 */
public class Today implements Function {
    public static final String TODAY = "TODAY";

    @Override
    public String getName() {
        return TODAY;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);
        cal.clear();
        cal.set(y, m, d);
        context.pushOperand(Value.date(cal.getTime()));
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
