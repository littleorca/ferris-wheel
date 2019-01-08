package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.core.formula.FakeEvalContext;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import junit.framework.TestCase;

import java.util.Calendar;

public class TestWeekNum extends TestCase {
    public void testGetName() {
        assertEquals("WEEKNUM", new WeekNum().getName());
    }

    public void testEval() {
        FakeEvalContext context = new FakeEvalContext();
        Calendar cal = Calendar.getInstance();
        cal.set(2018, 5 - 1, 3);
        context.getOperands().push(Value.date(cal.getTime()));
        new WeekNum().evaluate(null, context);
        Variant ret = context.getOperands().pop();
        assertEquals(VariantType.DECIMAL, ret.valueType());
        assertEquals(18, ret.intValue());
    }
}
