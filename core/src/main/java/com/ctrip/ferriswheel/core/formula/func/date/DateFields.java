package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;
import com.ctrip.ferriswheel.api.variant.Variant;

import java.util.Calendar;

public abstract class DateFields implements Function {
    @Override
    public boolean checkArgc(int argc) {
        return argc == 1;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Variant date = context.popOperand();
        context.pushOperand(getField(date));
    }

    protected abstract Variant getField(Variant date);

    /**
     * YEAR(serial_number)
     */
    public static class Year extends DateFields {
        public static final String YEAR = "YEAR";

        @Override
        public String getName() {
            return YEAR;
        }

        @Override
        protected Variant getField(Variant date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date.dateValue());
            return Value.dec(cal.get(Calendar.YEAR));
        }
    }

    /**
     * MONTH(serial_number)
     */
    public static class Month extends DateFields {
        public static final String MONTH = "MONTH";

        @Override
        public String getName() {
            return MONTH;
        }

        @Override
        protected Variant getField(Variant date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date.dateValue());
            return Value.dec(cal.get(Calendar.MONTH) + 1);
        }
    }

    /**
     * DAY(serial_number)
     */
    public static class Day extends DateFields {
        public static final String DAY = "DAY";

        @Override
        public String getName() {
            return DAY;
        }

        @Override
        protected Variant getField(Variant date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date.dateValue());
            return Value.dec(cal.get(Calendar.DAY_OF_MONTH));
        }
    }

    /**
     * HOUR(serial_number)
     */
    public static class Hour extends DateFields {
        public static final String HOUR = "HOUR";

        @Override
        public String getName() {
            return HOUR;
        }

        @Override
        protected Variant getField(Variant date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date.dateValue());
            return Value.dec(cal.get(Calendar.HOUR_OF_DAY));
        }
    }

    /**
     * MINUTE(serial_number)
     */
    public static class Minute extends DateFields {
        public static final String MINUTE = "MINUTE";

        @Override
        public String getName() {
            return MINUTE;
        }

        @Override
        protected Variant getField(Variant date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date.dateValue());
            return Value.dec(cal.get(Calendar.MINUTE));
        }
    }

    /**
     * SECOND(serial_number)
     */
    public static class Second extends DateFields {
        public static final String SECOND = "SECOND";

        @Override
        public String getName() {
            return SECOND;
        }

        @Override
        protected Variant getField(Variant date) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date.dateValue());
            return Value.dec(cal.get(Calendar.SECOND));
        }
    }
}
