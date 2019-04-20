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

package com.ctrip.ferriswheel.common.variant.impl;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import junit.framework.TestCase;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class TestValue extends TestCase {

    public void testError() {
        ErrorCodes ec = ErrorCodes.REF;
        Value.ErrorValue ev = Value.err(ec);
        TestCase.assertTrue(ev.booleanValue());
        TestCase.assertEquals(ec.getCode(), ev.intValue());
        TestCase.assertEquals(ec.getCode(), ev.longValue());
        TestCase.assertEquals(ec.getCode(), ev.floatValue(), 0.0001);
        TestCase.assertEquals(ec.getCode(), ev.doubleValue(), 0.0001);
        TestCase.assertEquals(ec.getName(), ev.strValue());
        try {
            ev.listValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
        TestCase.assertFalse(Value.err(ErrorCodes.OK).booleanValue());
    }

    public void testBlank() {
        TestCase.assertEquals(ErrorCodes.OK, Value.BLANK.errorValue());
        TestCase.assertEquals(0, Value.BLANK.intValue());
        TestCase.assertEquals(0, Value.BLANK.longValue());
        TestCase.assertFalse(Value.BLANK.booleanValue());
        TestCase.assertEquals(Value.date(0).dateValue(), Value.BLANK.dateValue());
        TestCase.assertEquals("", Value.BLANK.strValue());
        TestCase.assertEquals(ErrorCodes.OK, Value.BLANK.errorValue());
        try {
            Value.BLANK.listValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testDecimal() {
        TestCase.assertEquals(ErrorCodes.OK, Value.dec(0).errorValue());
        TestCase.assertEquals(0, Value.dec(0).intValue());
        TestCase.assertEquals(3, Value.dec(3).longValue());
        TestCase.assertEquals(10.24, Value.dec(10.24).floatValue(), 0.0001);
        TestCase.assertEquals(10.24, Value.dec(10.24).doubleValue(), 0.0001);
        TestCase.assertFalse(Value.dec(0).booleanValue());
        TestCase.assertTrue(Value.dec(1).booleanValue());
        TestCase.assertTrue(Value.dec(2).booleanValue());
        TestCase.assertTrue(Value.dec(-2).booleanValue());
        TestCase.assertEquals(Value.date(0).dateValue(), Value.dec(0).dateValue());
        TestCase.assertEquals("1024", Value.dec(1024).strValue());
        try {
            Value.dec(10).listValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testBool() {
        TestCase.assertEquals(ErrorCodes.OK, Value.TRUE.errorValue());
        TestCase.assertEquals(0, Value.FALSE.intValue());
        TestCase.assertEquals(0, Value.FALSE.longValue());
        TestCase.assertEquals(0, Value.FALSE.floatValue(), 0.00001);
        TestCase.assertEquals(0, Value.FALSE.doubleValue(), 0.00001);
        TestCase.assertEquals(1, Value.TRUE.intValue());
        TestCase.assertEquals(1, Value.TRUE.longValue());
        TestCase.assertEquals(1, Value.TRUE.floatValue(), 0.0001);
        TestCase.assertEquals(1, Value.TRUE.doubleValue(), 0.0001);
        TestCase.assertTrue(Value.TRUE.booleanValue());
        TestCase.assertFalse(Value.FALSE.booleanValue());
        TestCase.assertEquals(Value.date(0).dateValue(), Value.FALSE.dateValue());
        TestCase.assertEquals(Value.date(1).dateValue(), Value.TRUE.dateValue());
        TestCase.assertEquals("TRUE", Value.TRUE.strValue());
        TestCase.assertEquals("FALSE", Value.FALSE.strValue());
        try {
            Value.TRUE.listValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            Value.FALSE.listValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testDate() {
        TestCase.assertEquals(ErrorCodes.OK, Value.date(0).errorValue());
        TestCase.assertEquals(ErrorCodes.OK, Value.date(new Date()).errorValue());
        TestCase.assertEquals(0, Value.date(0).intValue());
        TestCase.assertEquals(0, Value.date(0).longValue());
        TestCase.assertEquals(0, Value.date(0).floatValue(), 0.0001);
        TestCase.assertEquals(0, Value.date(0).doubleValue(), 0.0001);
        TestCase.assertEquals(3, Value.date(3.5).longValue());
        TestCase.assertEquals(3.5, Value.date(3.5).doubleValue(), 0.0001);
        TestCase.assertEquals(-10.24, Value.date(-10.24).doubleValue(), 0.0001);
        TestCase.assertFalse(Value.date(0).booleanValue());
        TestCase.assertTrue(Value.date(1).booleanValue());
        TestCase.assertTrue(Value.date(2).booleanValue());
        TestCase.assertTrue(Value.date(-2).booleanValue());
        Date date = new Date();
        TestCase.assertEquals(date, Value.date(date).dateValue());
        TestCase.assertEquals("0001-01-01T00:00:00Z", Value.date(0).strValue());
        try {
            Value.date(10).listValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testString() {
        TestCase.assertEquals(ErrorCodes.OK, Value.str("").errorValue());
        TestCase.assertEquals(ErrorCodes.OK, Value.str("hello world").errorValue());
        TestCase.assertEquals(1, Value.str("1.2").intValue());
        TestCase.assertEquals(1, Value.str("1.2").longValue());
        TestCase.assertEquals(1.2, Value.str("1.2").floatValue(), 0.0001);
        TestCase.assertEquals(1.2, Value.str("1.2").doubleValue(), 0.0001);
        TestCase.assertTrue(Value.str("true").booleanValue());
        TestCase.assertFalse(Value.str("false").booleanValue());
        TestCase.assertFalse(Value.str("").booleanValue());
        TestCase.assertFalse(Value.str("hello world").booleanValue());
        TestCase.assertEquals(Value.date("2018-05-04T00:00:00Z").dateValue(), Value.str("2018-05-04T00:00:00Z").dateValue());
        TestCase.assertEquals("hello world", Value.str("hello world").strValue());
        try {
            Value.str("hello world").decimalValue();
            fail();
        } catch (NumberFormatException e) {
        }
        try {
            Value.str("hello world").dateValue();
            fail();
        } catch (DateTimeParseException e) {
        }
        try {
            Value.str("hello world").listValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testList() {
        Value.ListValue ls = Value.list(Collections.emptyList());
        TestCase.assertEquals(ErrorCodes.OK, ls.errorValue());
        try {
            ls.decimalValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            ls.booleanValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            ls.dateValue();
            fail();
        } catch (UnsupportedOperationException e) {
        }
        TestCase.assertEquals("[]", ls.strValue());
        TestCase.assertEquals(0, ls.itemCount());

        ls = Value.list(Arrays.asList(Value.str("hello"), Value.dec(10)));
        TestCase.assertEquals(2, ls.itemCount());
        Variant s = ls.item(0);
        assertEquals(VariantType.STRING, s.valueType());
        assertEquals("hello", s.strValue());
        Variant d = ls.item(1);
        assertEquals(VariantType.DECIMAL, d.valueType());
        assertEquals(10, d.intValue());
    }

    public void testCompareErrorToError() {
        checkCompare(0, Value.err(ErrorCodes.VALUE), Value.err(ErrorCodes.VALUE));
        checkUncomparable(Value.err(ErrorCodes.VALUE), Value.err(ErrorCodes.REF));
    }

    public void testCompareErrorToBlank() {
        checkUncomparable(Value.err(ErrorCodes.REF), Value.BLANK);
    }

    public void testCompareErrorToDecimal() {
        checkUncomparable(Value.err(ErrorCodes.REF), Value.dec(10));
    }

    public void testCompareErrorToBoolean() {
        checkUncomparable(Value.err(ErrorCodes.REF), Value.FALSE);
    }

    public void testCompareErrorToDate() {
        checkUncomparable(Value.err(ErrorCodes.REF), Value.date(new Date()));
    }

    public void testCompareErrorToString() {
        checkUncomparable(Value.err(ErrorCodes.REF), Value.str("hello world"));
    }

    public void testCompareErrorToList() {
        checkUncomparable(Value.err(ErrorCodes.REF), Value.list(Arrays.asList(Value.str("test"))));
    }

    public void testCompareBlankToBlank() {
        checkCompare(0, Value.BLANK, Value.BLANK);
    }

    public void testCompareBlankToDecimal() {
        checkCompare(0, Value.BLANK, Value.dec(0));
        checkCompare(-1, Value.BLANK, Value.dec(10));
        checkCompare(1, Value.BLANK, Value.dec(-10));
    }

    public void testCompareBlankToBoolean() {
        checkCompare(0, Value.BLANK, Value.FALSE);
        checkCompare(-1, Value.BLANK, Value.TRUE);
    }

    public void testCompareBlankToDate() {
        checkCompare(0, Value.BLANK, Value.date(0));
        checkCompare(-1, Value.BLANK, Value.date(1));
        checkCompare(1, Value.BLANK, Value.date(-1));
    }

    public void testCompareBlankToString() {
        checkCompare(0, Value.BLANK, Value.str(""));
        checkCompare(-1, Value.BLANK, Value.str("hello"));
    }

    public void testCompareBlankToList() {
        checkUncomparable(Value.BLANK, Value.list(new ArrayList<>()));
        checkUncomparable(Value.BLANK, Value.list(Arrays.asList(Value.str("hello"))));
    }

    public void testCompareDecimalToDecimal() {
        checkCompare(0, Value.dec(2), Value.dec(2));
        checkCompare(-1, Value.dec(2), Value.dec(3));
    }

    public void testCompareDecimalToBool() {
        checkCompare(0, Value.dec(1), Value.TRUE);
        checkCompare(0, Value.dec(0), Value.FALSE);
        checkCompare(-1, Value.dec(0), Value.TRUE);
        checkCompare(1, Value.dec(1), Value.FALSE);
    }

    public void testCompareDecimalToDate() {
        TestCase.assertEquals(0, Value.BLANK.compareTo(Value.date(0)));
        TestCase.assertEquals(-1, Value.BLANK.compareTo(Value.date(1)));
        TestCase.assertEquals(1, Value.BLANK.compareTo(Value.date(-1)));
    }

    public void testCompareDecimalToString() {
        checkCompare(-1, Value.dec(0), Value.str(""));
        checkCompare(-1, Value.dec(1), Value.str(""));
        checkCompare(-1, Value.dec(1), Value.str("hello"));
    }

    public void testCompareDecimalToList() {
        checkUncomparable(Value.dec(0), Value.list(new ArrayList<>()));
        checkUncomparable(Value.dec(1), Value.list(Arrays.asList(Value.str("hello"))));
    }

    public void testCompareBooleanToBoolean() {
        checkCompare(0, Value.TRUE, Value.TRUE);
        checkCompare(0, Value.FALSE, Value.FALSE);
        checkCompare(-1, Value.FALSE, Value.TRUE);
    }

    public void testCompareBooleanToDate() {
        TestCase.assertEquals(0, Value.FALSE.compareTo(Value.date(0)));
        TestCase.assertEquals(-1, Value.FALSE.compareTo(Value.date(1)));
        TestCase.assertEquals(1, Value.FALSE.compareTo(Value.date(-1)));
        TestCase.assertEquals(1, Value.TRUE.compareTo(Value.date(0)));
    }

    public void testCompareDateToDate() {
        final long ts = 123456789012L;
        checkCompare(0, Value.date(Instant.ofEpochMilli(ts)), Value.date(Instant.ofEpochMilli(ts)));
        checkCompare(-1, Value.date(Instant.ofEpochMilli(ts)), Value.date(Instant.ofEpochMilli(ts + 1024)));
    }

    public void testCompareDateToString() {
        checkCompare(-1, Value.date(new Date(0)), Value.str(""));
        checkCompare(-1, Value.date(new Date(0)), Value.str("hello world"));
        checkCompare(-1, Value.date(new Date()), Value.str(""));
        checkCompare(-1, Value.date(new Date()), Value.str("hello world"));
    }

    public void testCompareDateToList() {
        Date date = new Date();
        checkUncomparable(Value.date(date), Value.list(Collections.emptyList()));
        checkUncomparable(Value.date(date), Value.list(Arrays.asList(Value.date(date))));
    }

    public void testCompareStringToString() {
        checkCompare(0, Value.str(""), Value.str(""));
        checkCompare(0, Value.str("hello world"), Value.str("hello world"));
        checkCompare(-1, Value.str(""), Value.str("hello world"));
        checkCompare(-1, Value.str("hello world 1"), Value.str("hello world 2"));
    }

    public void testCompareStringToList() {
        checkUncomparable(Value.str(""), Value.list(Collections.emptyList()));
        checkUncomparable(Value.str("hello"), Value.list(Arrays.asList(Value.str("hello"))));
    }

    public void testCompareListToList() {
        checkCompare(0, Value.list(new ArrayList<>()), Value.list(new ArrayList<>()));
        checkCompare(0, Value.list(new ArrayList<>()), Value.list(Collections.emptyList()));
        checkUncomparable(Value.list(Arrays.asList(Value.str("hello"))), Value.list(Arrays.asList(Value.str("world"))));
    }

    private void checkCompare(int expected, Variant var1, Variant var2) {
        if (expected == 0) {
            assertEquals(0, var1.compareTo(var2));
            assertEquals(0, var2.compareTo(var1));
        } else if (expected < 0) {
            assertTrue(var1.compareTo(var2) < 0);
            assertTrue(var2.compareTo(var1) > 0);
        } else {
            assertTrue(var1.compareTo(var2) > 0);
            assertTrue(var2.compareTo(var1) < 0);
        }
    }

    private void checkUncomparable(Variant var1, Variant var2) {
        try {
            var1.compareTo(var2);
            fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            var2.compareTo(var1);
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }
}
