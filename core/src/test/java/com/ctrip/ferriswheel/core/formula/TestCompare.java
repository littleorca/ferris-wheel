package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import com.ctrip.ferriswheel.core.formula.BinaryElement.*;
import junit.framework.TestCase;

import java.util.ArrayList;

public class TestCompare extends TestCase {
    public void testEq() {
        Equal eq = new Equal();
        assertCompare(true, Value.BLANK, Value.BLANK, eq);
        assertCompare(true, Value.dec(1024), Value.dec(1024), eq);
        assertCompare(false, Value.dec(1024), Value.dec(2048), eq);
        assertCompare(false, Value.dec(2048), Value.dec(1024), eq);
        assertCompare(false, Value.str("hello world"), Value.list(new ArrayList<>()), eq);
    }

    public void testLt() {
        LessThan lt = new LessThan();
        assertCompare(false, Value.BLANK, Value.BLANK, lt);
        assertCompare(false, Value.dec(1024), Value.dec(1024), lt);
        assertCompare(true, Value.dec(1024), Value.dec(2048), lt);
        assertCompare(false, Value.dec(2048), Value.dec(1024), lt);
        assertInvalidCompare(Value.str("hello world"), Value.list(new ArrayList<>()), lt);
    }

    public void testGt() {
        GreaterThan gt = new GreaterThan();
        assertCompare(false, Value.BLANK, Value.BLANK, gt);
        assertCompare(false, Value.dec(1024), Value.dec(1024), gt);
        assertCompare(false, Value.dec(1024), Value.dec(2048), gt);
        assertCompare(true, Value.dec(2048), Value.dec(1024), gt);
        assertInvalidCompare(Value.str("hello world"), Value.list(new ArrayList<>()), gt);
    }

    public void testLe() {
        LessThanOrEqual le = new LessThanOrEqual();
        assertCompare(true, Value.BLANK, Value.BLANK, le);
        assertCompare(true, Value.dec(1024), Value.dec(1024), le);
        assertCompare(true, Value.dec(1024), Value.dec(2048), le);
        assertCompare(false, Value.dec(2048), Value.dec(1024), le);
        assertInvalidCompare(Value.str("hello world"), Value.list(new ArrayList<>()), le);
    }

    public void testGe() {
        GreaterThanOrEqual ge = new GreaterThanOrEqual();
        assertCompare(true, Value.BLANK, Value.BLANK, ge);
        assertCompare(true, Value.dec(1024), Value.dec(1024), ge);
        assertCompare(false, Value.dec(1024), Value.dec(2048), ge);
        assertCompare(true, Value.dec(2048), Value.dec(1024), ge);
        assertInvalidCompare(Value.str("hello world"), Value.list(new ArrayList<>()), ge);
    }

    public void testNe() {
        NotEqual ne = new NotEqual();
        assertCompare(false, Value.BLANK, Value.BLANK, ne);
        assertCompare(false, Value.dec(1024), Value.dec(1024), ne);
        assertCompare(true, Value.dec(1024), Value.dec(2048), ne);
        assertCompare(true, Value.dec(2048), Value.dec(1024), ne);
        assertCompare(true, Value.str("hello world"), Value.list(new ArrayList<>()), ne);
        assertCompare(false, Value.list(new ArrayList<>()), Value.list(new ArrayList<>()), ne);
    }

    private void assertCompare(boolean expected, Variant op1, Variant op2, Compare cmp) {
        FakeEvalContext context = new FakeEvalContext();
        context.pushOperand(op1);
        context.pushOperand(op2);
        cmp.evaluate(context);
        assertEquals(1, context.getOperands().size());
        Variant ret = context.popOperand();
        assertEquals(VariantType.BOOL, ret.valueType());
        assertEquals(expected, ret.booleanValue());
    }

    private void assertInvalidCompare(Variant op1, Variant op2, Compare cmp) {
        FakeEvalContext context = new FakeEvalContext();
        context.pushOperand(op1);
        context.pushOperand(op2);
        cmp.evaluate(context);
        assertEquals(1, context.getOperands().size());
        Variant ret = context.popOperand();
        assertEquals(VariantType.ERROR, ret.valueType());
        assertEquals(ErrorCodes.VALUE, ret.errorValue());
    }
}
