package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import com.ctrip.ferriswheel.core.formula.FakeEvalContext;
import junit.framework.TestCase;

import java.util.Calendar;


public class TestDateParser extends TestCase {
    public void testDateValue() {
        DateParser.DateValue dv = new DateParser.DateValue();
        assertEquals("DATEVALUE", dv.getName());
        FakeEvalContext context = new FakeEvalContext();
        context.getOperands().push(Value.str("2018-5-3"));
        dv.evaluate(null, context);
        Variant ret = context.getOperands().pop();
        assertEquals(VariantType.DATE, ret.valueType());
        Calendar cal = Calendar.getInstance();
        cal.setTime(ret.dateValue());
        assertEquals(2018, cal.get(Calendar.YEAR));
        assertEquals(5, cal.get(Calendar.MONTH) + 1);
        assertEquals(3, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));

        context.getOperands().push(Value.str("2018/5/3"));
        dv.evaluate(null, context);
        ret = context.getOperands().pop();
        assertEquals(VariantType.DATE, ret.valueType());
        cal = Calendar.getInstance();
        cal.setTime(ret.dateValue());
        assertEquals(2018, cal.get(Calendar.YEAR));
        assertEquals(5, cal.get(Calendar.MONTH) + 1);
        assertEquals(3, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));

        context.getOperands().push(Value.str("555:3/2018"));
        dv.evaluate(null, context);
        ret = context.getOperands().pop();
        assertEquals(VariantType.ERROR, ret.valueType());
        assertEquals(ErrorCodes.VALUE, ret.errorValue());
    }

    public void testTimeValue() {
        DateParser.TimeValue tv = new DateParser.TimeValue();
        assertEquals("TIMEVALUE", tv.getName());
        FakeEvalContext context = new FakeEvalContext();
        context.getOperands().push(Value.str("13:45:3"));
        tv.evaluate(null, context);
        Variant ret = context.getOperands().pop();
        assertEquals(VariantType.DATE, ret.valueType());
        Calendar cal = Calendar.getInstance();
        cal.setTime(ret.dateValue());
        assertEquals(1970, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));
        assertEquals(3, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));

        context.getOperands().push(Value.str("13:45"));
        tv.evaluate(null, context);
        ret = context.getOperands().pop();
        assertEquals(VariantType.DATE, ret.valueType());
        cal = Calendar.getInstance();
        cal.setTime(ret.dateValue());
        assertEquals(1970, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));

        context.getOperands().push(Value.str("13-45"));
        tv.evaluate(null, context);
        ret = context.getOperands().pop();
        assertEquals(VariantType.ERROR, ret.valueType());
        assertEquals(ErrorCodes.VALUE, ret.errorValue());
    }
}
