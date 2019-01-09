/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.VariantType;
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
