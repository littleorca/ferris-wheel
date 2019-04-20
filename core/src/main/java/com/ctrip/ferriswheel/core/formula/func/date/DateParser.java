package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class DateParser implements Function {

    @Override
    public boolean checkArgc(int argc) {
        return argc == 1;
    }

    /**
     * DATEVALUE(date_text) date text to date
     */
    public static class DateValue extends DateParser {
        public static final String DATEVALUE = "DATEVALUE";

        public static final String DATE_PATTERN_1 = "yyyy-MM-dd";
        public static final String DATE_PATTERN_2 = "yyyy/MM/dd";

        @Override
        public String getName() {
            return DATEVALUE;
        }

        @Override
        public void evaluate(FuncElement element, FormulaEvaluationContext context) {
            Variant text = context.popOperand();
            Date result = null;
            try {
                result = new SimpleDateFormat(DATE_PATTERN_1).parse(text.strValue());
            } catch (ParseException e) {
                try {
                    result = new SimpleDateFormat(DATE_PATTERN_2).parse(text.strValue());
                } catch (ParseException e1) {
                }
            }
            if (result == null) {
                context.pushOperand(Value.err(ErrorCodes.VALUE));
            } else {
                context.pushOperand(Value.date(result));
            }
        }
    }

    /**
     * TIMEVALUE(time_text)
     */
    public static class TimeValue extends DateParser {
        public static final String TIMEVALUE = "TIMEVALUE";

        public static final String TIME_PATTERN_1 = "HH:mm:ss";
        public static final String TIME_PATTERN_2 = "HH:mm";

        @Override
        public String getName() {
            return TIMEVALUE;
        }

        @Override
        public void evaluate(FuncElement element, FormulaEvaluationContext context) {
            Variant text = context.popOperand();
            Date result = null;
            try {
                result = new SimpleDateFormat(TIME_PATTERN_1).parse(text.strValue());
            } catch (ParseException e) {
                try {
                    result = new SimpleDateFormat(TIME_PATTERN_2).parse(text.strValue());
                } catch (ParseException e1) {
                }
            }
            if (result == null) {
                context.pushOperand(Value.err(ErrorCodes.VALUE));
            } else {
                context.pushOperand(Value.date(result));
            }
        }
    }
}
