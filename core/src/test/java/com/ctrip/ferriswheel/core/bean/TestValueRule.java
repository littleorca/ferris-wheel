package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.intf.VariantType;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;

public class TestValueRule extends TestCase {

    public void testType() {
        ValueRule rule = new ValueRule();
        assertTrue(rule.check(Value.str("test")));
        rule.setType(VariantType.DECIMAL);
        assertEquals(VariantType.DECIMAL, rule.getType());
        assertFalse(rule.check(Value.str("test")));
        rule.setType(VariantType.STRING);
        assertEquals(VariantType.STRING, rule.getType());
        assertTrue(rule.check(Value.str("test")));
    }

    public void testNullable() {
        ValueRule rule = new ValueRule();
        rule.setNullable(false);
        assertFalse(rule.isNullable());
        assertFalse(rule.check(null));
        assertTrue(rule.check(Value.str("test")));
        rule.setNullable(true);
        assertTrue(rule.isNullable());
        assertTrue(rule.check(null));
        assertTrue(rule.check(Value.str("test")));
    }

    public void testAllowedValues() {
        ValueRule rule = new ValueRule();
        assertTrue(rule.check(Value.str("test")));
        rule.setAllowedValues(new HashSet<>(Arrays.asList(Value.dec(1),
                Value.bool(false))));
        assertEquals(2, rule.getAllowedValues().size());
        assertFalse(rule.check(Value.str("test")));
        rule.setAllowedValues(new HashSet<>(Arrays.asList(Value.dec(1),
                Value.bool(false),
                Value.str("111"))));
        assertEquals(3, rule.getAllowedValues().size());
        assertFalse(rule.check(Value.str("test")));
        rule.setAllowedValues(new HashSet<>(Arrays.asList(Value.dec(1),
                Value.bool(false),
                Value.str("111"),
                Value.str("test"))));
        assertEquals(4, rule.getAllowedValues().size());
        assertTrue(rule.check(Value.str("test")));
    }
}
