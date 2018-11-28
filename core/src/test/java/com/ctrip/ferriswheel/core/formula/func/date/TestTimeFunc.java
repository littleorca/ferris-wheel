package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.FakeEvalContext;
import com.ctrip.ferriswheel.core.intf.Variant;
import com.ctrip.ferriswheel.core.intf.VariantType;
import junit.framework.TestCase;

import java.util.Calendar;


public class TestTimeFunc extends TestCase {
    public void testGetName() {
        assertEquals("TIME", new TimeFunc().getName());
    }

    public void testEval() {
        FakeEvalContext context = new FakeEvalContext();
        context.pushOperand(Value.dec(13));
        context.pushOperand(Value.dec(45));
        context.pushOperand(Value.dec(3));
        new TimeFunc().evaluate(null, context);
        assertEquals(1, context.getOperands().size());
        Variant date = context.popOperand();
        assertEquals(VariantType.DATE, date.valueType());
        Calendar cal = Calendar.getInstance();
        cal.setTime(date.dateValue());
        assertEquals(1970, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));
        assertEquals(3, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }
}
