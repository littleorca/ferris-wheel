package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;
import com.ctrip.ferriswheel.core.intf.Variant;

import java.util.Calendar;

/**
 * WEEKDAY(serial_number)
 * Note MS Excel's define is: WEEKDAY(serial_number,[return_type])
 * The optional return_type is annoying and can be replaced by
 * simply add/subtract the result.
 */
public class WeekDay implements Function {
    public static final String WEEKDAY = "WEEKDAY";

    @Override
    public String getName() {
        return WEEKDAY;
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
        int weekday = cal.get(Calendar.DAY_OF_WEEK);
        context.pushOperand(Value.dec(weekday));
    }
}
