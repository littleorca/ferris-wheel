package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import com.ctrip.ferriswheel.core.formula.FakeEvalContext;
import junit.framework.TestCase;

import java.util.Calendar;


public class TestDateFunc extends TestCase {
    public void testGetName() {
        assertEquals("DATE", new DateFunc().getName());
    }

    public void testEval() {
        FakeEvalContext context = new FakeEvalContext();
        context.pushOperand(Value.dec(2018));
        context.pushOperand(Value.dec(5));
        context.pushOperand(Value.dec(3));
        new DateFunc().evaluate(null, context);
        assertEquals(1, context.getOperands().size());
        Variant date = context.popOperand();
        assertEquals(VariantType.DATE, date.valueType());
        Calendar cal = Calendar.getInstance();
        cal.setTime(date.dateValue());
        assertEquals(2018, cal.get(Calendar.YEAR));
        assertEquals(5, cal.get(Calendar.MONTH) + 1);
        assertEquals(3, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }
}
