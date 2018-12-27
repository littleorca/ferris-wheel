package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.FakeEvalContext;
import com.ctrip.ferriswheel.api.variant.Variant;
import com.ctrip.ferriswheel.api.variant.VariantType;
import junit.framework.TestCase;

import java.util.Calendar;


public class TestWeekDay extends TestCase {
    public void testGetName() {
        assertEquals("WEEKDAY", new WeekDay().getName());
    }

    public void testEval() {
        FakeEvalContext context = new FakeEvalContext();
        Calendar cal = Calendar.getInstance();
        cal.set(2018, 5 - 1, 3); // thursday = 5
        context.getOperands().push(Value.date(cal.getTime()));
        new WeekDay().evaluate(null, context);
        Variant ret = context.getOperands().pop();
        assertEquals(VariantType.DECIMAL, ret.valueType());
        assertEquals(5, ret.intValue());
    }
}
