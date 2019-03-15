/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
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

package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.variant.Value;
import junit.framework.TestCase;

/**
 * @author liuhaifeng
 */
public class TestVariantFormatter extends TestCase {
    public void testFormatDecimal() {
        assertEquals("12346", VariantFormatter.format(Value.dec(12345.6789), "0"));
        assertEquals("12,346", VariantFormatter.format(Value.dec(12345.6789), "#,##0"));
        assertEquals("12345.7", VariantFormatter.format(Value.dec(12345.6789), "0.0"));
        assertEquals("12345.67890", VariantFormatter.format(Value.dec(12345.6789), "0.00000"));
        assertEquals("12,345.67890", VariantFormatter.format(Value.dec(12345.6789), "#,##0.00000"));
        assertEquals("1234568%", VariantFormatter.format(Value.dec(12345.6789), "0%"));
        assertEquals("1,234,568%", VariantFormatter.format(Value.dec(12345.6789), "#,##0%"));
        assertEquals("1,234,567.9%", VariantFormatter.format(Value.dec(12345.6789), "#,##0.0%"));
        assertEquals("1,234,567.89%", VariantFormatter.format(Value.dec(12345.6789), "#,##0.00%"));
        assertEquals("1,234,567.890%", VariantFormatter.format(Value.dec(12345.6789), "#,##0.000%"));
    }

    public void testFormatDateTime() {
        assertEquals("2019-03-13 09:02:03", VariantFormatter.format(Value.date("2019-03-13T01:02:03.456Z"), "yyyy-MM-dd HH:mm:ss"));
    }
}
