package com.ctrip.ferriswheel.core.formula.func;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.FakeEvalContext;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.intf.Variant;
import com.ctrip.ferriswheel.core.intf.VariantType;
import junit.framework.TestCase;

import java.util.Arrays;

public class TestVLookup extends TestCase {
    private FakeEvalContext context = new FakeEvalContext();

    public void test() {
        context.pushOperand(Value.str("foo"));
        context.pushOperand(Value.list(Arrays.asList(
                Value.str("f1"), Value.str("Hello"), Value.str("world"),
                Value.str("foo"), Value.str("foobar"), Value.str("bar"),
                Value.str("f2"), Value.str("nihao"), Value.str("shijie")
        ), 3));
        context.pushOperand(Value.dec(2));
        context.pushOperand(Value.bool(false));

        new VLookup().evaluate(new FuncElement(null, "VLOOKUP", 4, 4), context);
        Variant result = context.popOperand();
        assertTrue(context.getOperands().isEmpty());

        assertEquals(VariantType.STRING, result.valueType());
        assertEquals("foobar", result.strValue());
    }
}
