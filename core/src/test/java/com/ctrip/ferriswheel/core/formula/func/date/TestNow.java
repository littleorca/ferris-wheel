package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.core.formula.FakeEvalContext;
import com.ctrip.ferriswheel.api.variant.Variant;
import com.ctrip.ferriswheel.api.variant.VariantType;
import junit.framework.TestCase;

public class TestNow extends TestCase {
    public void testGetName() {
        assertEquals("NOW", new Now().getName());
    }

    public void testEval() {
        FakeEvalContext context = new FakeEvalContext();
        new Now().evaluate(null, context);
        assertEquals(1, context.getOperands().size());
        Variant date = context.popOperand();
        assertEquals(VariantType.DATE, date.valueType());
        assertEquals(System.currentTimeMillis(), date.dateValue().getTime(), 1000);
    }
}
