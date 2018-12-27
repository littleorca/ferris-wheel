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

import junit.framework.TestCase;

public class TestColorImpl extends TestCase {
    public void testCssString() {
        ColorImpl c = new ColorImpl("#000");
        assertEquals(0, c.getRed(), 0.0000001);
        assertEquals(0, c.getGreen(), 0.0000001);
        assertEquals(0, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new ColorImpl("#000000");
        assertEquals(0, c.getRed(), 0.0000001);
        assertEquals(0, c.getGreen(), 0.0000001);
        assertEquals(0, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new ColorImpl("#fff");
        assertEquals(1, c.getRed(), 0.0000001);
        assertEquals(1, c.getGreen(), 0.0000001);
        assertEquals(1, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new ColorImpl("#ffffff");
        assertEquals(1, c.getRed(), 0.0000001);
        assertEquals(1, c.getGreen(), 0.0000001);
        assertEquals(1, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new ColorImpl("#9cf");
        assertEquals(0.6000000, c.getRed(), 0.0000001);
        assertEquals(0.8000000, c.getGreen(), 0.0000001);
        assertEquals(1, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new ColorImpl("#f0F9fC");
        assertEquals(0.9411765, c.getRed(), 0.0000001);
        assertEquals(0.9764706, c.getGreen(), 0.0000001);
        assertEquals(0.9882353, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        try {
            new ColorImpl((String) null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl("");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl(" ");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl("0");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl("#");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl("#00");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl("#0000");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl("#00000");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl("#0000000");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl("#ggg");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl("#ggg");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new ColorImpl((ColorImpl) null);
            fail();
        } catch (NullPointerException e) {
        }
    }
}
