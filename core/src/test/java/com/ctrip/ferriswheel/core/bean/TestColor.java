package com.ctrip.ferriswheel.core.bean;

import junit.framework.TestCase;

public class TestColor extends TestCase {
    public void testCssString() {
        Color c = new Color("#000");
        assertEquals(0, c.getRed(), 0.0000001);
        assertEquals(0, c.getGreen(), 0.0000001);
        assertEquals(0, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new Color("#000000");
        assertEquals(0, c.getRed(), 0.0000001);
        assertEquals(0, c.getGreen(), 0.0000001);
        assertEquals(0, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new Color("#fff");
        assertEquals(1, c.getRed(), 0.0000001);
        assertEquals(1, c.getGreen(), 0.0000001);
        assertEquals(1, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new Color("#ffffff");
        assertEquals(1, c.getRed(), 0.0000001);
        assertEquals(1, c.getGreen(), 0.0000001);
        assertEquals(1, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new Color("#9cf");
        assertEquals(0.6000000, c.getRed(), 0.0000001);
        assertEquals(0.8000000, c.getGreen(), 0.0000001);
        assertEquals(1, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        c = new Color("#f0F9fC");
        assertEquals(0.9411765, c.getRed(), 0.0000001);
        assertEquals(0.9764706, c.getGreen(), 0.0000001);
        assertEquals(0.9882353, c.getBlue(), 0.0000001);
        assertEquals(1, c.getAlpha(), 0.0000001);

        try {
            new Color((String) null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color("");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color(" ");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color("0");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color("#");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color("#00");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color("#0000");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color("#00000");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color("#0000000");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color("#ggg");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color("#ggg");
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new Color((Color) null);
            fail();
        } catch (NullPointerException e) {
        }
    }
}
