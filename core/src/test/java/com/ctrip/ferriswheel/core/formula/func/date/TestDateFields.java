package com.ctrip.ferriswheel.core.formula.func.date;

import com.ctrip.ferriswheel.common.variant.impl.Value;
import com.ctrip.ferriswheel.core.formula.FakeEvalContext;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import junit.framework.TestCase;

import java.util.Calendar;


public class TestDateFields extends TestCase {
    private final int YEAR = 2018;
    private final int MONTH = 5;
    private final int DATE = 3;
    private final int HOUR_OF_DAY = 13;
    private final int MINUTE = 45;
    private final int SECOND = 3;

    public void testGetFields() {
        doTest("YEAR", YEAR, new DateFields.Year());
        doTest("MONTH", MONTH, new DateFields.Month());
        doTest("DAY", DATE, new DateFields.Day());
        doTest("HOUR", HOUR_OF_DAY, new DateFields.Hour());
        doTest("MINUTE", MINUTE, new DateFields.Minute());
        doTest("SECOND", SECOND, new DateFields.Second());
    }

    private void doTest(String expectedName, int expectedRet, DateFields func) {
        assertEquals(expectedName, func.getName());

        FakeEvalContext context = new FakeEvalContext();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(YEAR, MONTH - 1, DATE, HOUR_OF_DAY, MINUTE, SECOND);
        context.pushOperand(Value.date(cal.getTime()));

        func.evaluate(null, context);
        Variant ret = context.popOperand();
        assertEquals(VariantType.DECIMAL, ret.valueType());
        assertEquals(expectedRet, ret.intValue());
    }
}
