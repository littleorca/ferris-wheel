package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;

import java.util.Calendar;

/**
 * DATEDIF(start_date,end_date,unit)
 */
public class DateDif implements Function {
    public static final String DATEDIF = "DATEDIF";

    /**
     * The number of complete years in the period.
     */
    public static final String UNIT_Y = "Y";

    /**
     * The number of complete months in the period.
     */
    public static final String UNIT_M = "M";

    /**
     * The number of days in the period.
     */
    public static final String UNIT_D = "D";

    /**
     * The difference between the days in start_date and end_date.
     * The months and years of the dates are ignored.
     * Important: We don't recommend using the "MD" argument, as
     * there are known limitations with it.
     */
    public static final String UNIT_MD = "MD";

    /**
     * The difference between the months in start_date and end_date.
     * The days and years of the dates are ignored.
     */
    public static final String UNIT_YM = "YM";

    /**
     * The difference between the days of start_date and end_date.
     * The years of the dates are ignored.
     */
    public static final String UNIT_YD = "YD";

    @Override
    public String getName() {
        return DATEDIF;
    }

    @Override
    public boolean checkArgc(int argc) {
        return argc == 3;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Variant unit = context.popOperand();
        Variant end = context.popOperand();
        Variant start = context.popOperand();

        Calendar cal = Calendar.getInstance();
        // TODO incomplete
    }
}
