package com.ctrip.ferriswheel.quarks.token;

import junit.framework.TestCase;

public class TestDefaultLexContext extends TestCase {
    private DefaultLexContext lex = DefaultLexContext.getDefaultInstance();

    public void testIsIdentifierStart() {
        String s = "$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
        for (int i = 0; i < s.length(); i++) {
            assertTrue(
                    "'"
                            + s.charAt(i)
                            + "' is supposed to be a legal leading letter of identifier.",
                    lex.isIdentifierStart(s.charAt(i)));
        }

        s = "0123456789~`!@#%^&*()-=+[{]}\\|;:'\",<.>/?";
        for (int i = 0; i < s.length(); i++) {
            assertFalse(
                    "'"
                            + s.charAt(i)
                            + "' is supposed to be an illegal leading letter of identifier.",
                    lex.isIdentifierStart(s.charAt(i)));
        }
    }

    public void testIsIdentifierChar() {
        String s = "$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789";

        for (int i = 0; i < s.length(); i++) {
            assertTrue("'" + s.charAt(i)
                            + "' is supposed to be a legal letter of identifier.",
                    lex.isIdentifierPart(s.charAt(i), s.charAt(0)));
        }

        s = "~`!@#%^&*()-=+[{]}\\|;:'\",<.>/?";
        for (int i = 0; i < s.length(); i++) {
            assertFalse(
                    "'"
                            + s.charAt(i)
                            + "' is supposed to be an illegal leading letter of identifier.",
                    lex.isIdentifierPart(s.charAt(i), s.charAt(0)));
        }
    }

    public void testOthers() {
        String[] operators = lex.getOperators();
        assertNotNull(operators);
        assertTrue(operators.length > 0);

        String[] delimiters = lex.getDelimiters();
        assertNotNull(delimiters);
        assertTrue(delimiters.length > 0);

        String[] keywords = lex.getKeywords();
        assertNotNull(keywords);
        assertTrue(keywords.length == 0);

        String[] literals = lex.getLiterals();
        assertNotNull(literals);
        assertTrue(literals.length > 0);
    }

}
