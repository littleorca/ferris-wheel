package com.ctrip.ferriswheel.core.util;

import junit.framework.TestCase;

public class TestEscapeHelper extends TestCase {

    public void testEscapeAndUnescape() {
        assertEscapeAndUnescape("a", "a");
        assertEscapeAndUnescape("Z", "Z");
        assertEscapeAndUnescape("9", "9");
        assertEscapeAndUnescape("_", "_");
        assertEscapeAndUnescape("$", "$");
        assertEscapeAndUnescape("中", "\"中\"");
        assertEscapeAndUnescape("aZ9_$中", "\"aZ9_$中\"");

        assertEscapeAndUnescape("'", "\"\\'\"");
        assertEscapeAndUnescape("\"", "\"\\\"\"");
        assertEscapeAndUnescape("\r", "\"\\r\"");
        assertEscapeAndUnescape("\n", "\"\\n\"");
        assertEscapeAndUnescape("\\", "\"\\\\\"");
    }

    private void assertEscapeAndUnescape(String plain, String escaped) {
        assertEquals(escaped, EscapeHelper.escape(plain));
        assertEquals(plain, EscapeHelper.unescape(escaped));
    }

}
