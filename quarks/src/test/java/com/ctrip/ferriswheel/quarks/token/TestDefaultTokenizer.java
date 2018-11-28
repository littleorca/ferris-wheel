package com.ctrip.ferriswheel.quarks.token;

import com.ctrip.ferriswheel.quarks.Token;
import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import junit.framework.TestCase;

import java.io.StringReader;

public class TestDefaultTokenizer extends TestCase {

    DefaultTokenizer getTokenizer() {
        return getTokenizer(null);
    }

    DefaultTokenizer getTokenizer(String src) {
        StringReader reader = null;
        if (src != null)
            reader = new StringReader(src);
        return new DefaultTokenizer(reader, new DefaultLexContext(
                /* operators --> */new String[]{"+", "-", "*", "/", "=", "%", "<",
                "<=", ">=", ">", "==", "!=", "(", ")", ".", "[", "]", "&&",
                "||", "!", "++", "--", "&", "|", "^", "~", "<<", ">>", "+=",
                "-=", "*=", "/=", "%=", "&=", "|=", "^=", "~=", "<<=", ">>=",
                ":", "?"},
                /* delimiters -> */new String[]{",", ";", "{", "}", "//", "/*", "*/"},
                /* keywords ---> */new String[]{"and", "or", "not", "select", "as",
                "from", "where", "is", "in"},
                /* literals ---> */new String[]{"true", "false", "null"}));
    }

    public void testIsNumberStart() {
        DefaultTokenizer tokenizer = getTokenizer();
        String s = ".0123456789";
        for (int i = 0; i < s.length(); i++) {
            assertTrue("'" + s.charAt(i)
                            + "' is supposed to be a legal leading letter of number.",
                    tokenizer.isNumberStart(s.charAt(i)));
        }

        s = "$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_~`!@#$%^&*()-_=+[{]}\\|;:'\",<>/?";
        for (int i = 0; i < s.length(); i++) {
            assertFalse(
                    "'"
                            + s.charAt(i)
                            + "' is supposed to be an illegal leading letter of number.",
                    tokenizer.isNumberStart(s.charAt(i)));
        }
    }

    public void testTokenizeString() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer("  \"abc\\\'\\\" \\r\\nd\\te\\u0024\\\\OCT*\\44\\044\\448\\0\\1\"  ");

        assertTrue(tokenizer.next());
        String expected = "abc\'\" \r\nd\te$\\OCT*$$$8\0\1";
        assertEquals(Token.Type.String, tokenizer.getType());
        assertEquals(expected, tokenizer.getString());

        assertFalse(tokenizer.next());
        assertNull(tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(tokenizer.getFrom(), tokenizer.getTo());
    }

    public void testTokenizeStringInMixMode() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer("''a");

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.String, tokenizer.getType());
        assertEquals("", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());

        assertFalse(tokenizer.next());
        assertNull(tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(tokenizer.getFrom(), tokenizer.getTo());
    }

    public void testTokenizeNumber() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer(" 123 123l  123L  1.23 .123  1.23f 1.2F .1f .1F  "
                + "0  0.1 0.1f 0755  0x09aF 0xAf09 01 00");

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("123", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("123l", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("123L", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1.23", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals(".123", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1.23f", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1.2F", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals(".1f", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals(".1F", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("0", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("0.1", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("0.1f", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("0755", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("0x09aF", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("0xAf09", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("01", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("00", tokenizer.getString());

        assertFalse(tokenizer.next());
        assertNull(tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(tokenizer.getFrom(), tokenizer.getTo());

        tokenizer = getTokenizer("0");

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("0", tokenizer.getString());
    }

    public void testTokenizeIdentifier() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer("hello \r\nor hey \t$a $0  $_");

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("hello", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Keyword, tokenizer.getType());
        assertEquals("or", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("hey", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("$a", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("$0", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("$_", tokenizer.getString());

        assertFalse(tokenizer.next());
        assertNull(tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(tokenizer.getFrom(), tokenizer.getTo());
    }

    public void testTokenizeKeywordAndLiteral() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer("int a in b null false true");

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("int", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Keyword, tokenizer.getType());
        assertEquals("in", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("b", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Literal, tokenizer.getType());
        assertEquals("null", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Literal, tokenizer.getType());
        assertEquals("false", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Literal, tokenizer.getType());
        assertEquals("true", tokenizer.getString());

        assertFalse(tokenizer.next());
        assertNull(tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(tokenizer.getFrom(), tokenizer.getTo());
    }

    public void testTokenizeByTrie() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer("{and (a.b) .4f +c+=b[1]>>=}");

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Delimiter, tokenizer.getType());
        assertEquals("{", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Keyword, tokenizer.getType());
        assertEquals("and", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("(", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals(".", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("b", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals(")", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals(".4f", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("+", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("c", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("+=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("b", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("[", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("]", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals(">>=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Delimiter, tokenizer.getType());
        assertEquals("}", tokenizer.getString());

        assertFalse(tokenizer.next());
        assertNull(tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(tokenizer.getFrom(), tokenizer.getTo());
    }

    public void testTokenizeLineComment() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer("a=1//assign");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals("assign", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertFalse(tokenizer.next());

        tokenizer = getTokenizer("// empty block");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals(" empty block", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertFalse(tokenizer.next());

        tokenizer = getTokenizer("a=1// empty block\nb=2");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals(" empty block", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("b", tokenizer.getString());
        assertEquals(2, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("2", tokenizer.getString());
        assertEquals(2, tokenizer.getLine());

        assertFalse(tokenizer.next());

        tokenizer = getTokenizer("//");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertFalse(tokenizer.next());
    }

    public void testTokenizeSingleLineBlockComment() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer("a=1/*assign*/");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals("assign", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertFalse(tokenizer.next());

        tokenizer = getTokenizer("/* empty block */");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals(" empty block ", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertFalse(tokenizer.next());

        tokenizer = getTokenizer("/*assign*/a=1");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals("assign", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertFalse(tokenizer.next());

        tokenizer = getTokenizer("a/*assign*/=1");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals("assign", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertFalse(tokenizer.next());

        tokenizer = getTokenizer("/**/");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertFalse(tokenizer.next());
    }

    public void testMultiLineBlockComment() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer("a/*this\ncomment\noccupy\nmulti-lines*/=1");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals("this", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals("comment", tokenizer.getString());
        assertEquals(2, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals("occupy", tokenizer.getString());
        assertEquals(3, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertEquals("multi-lines", tokenizer.getString());
        assertEquals(4, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1", tokenizer.getString());
        assertEquals(4, tokenizer.getLine());

        assertFalse(tokenizer.next());

        tokenizer = getTokenizer("/*\n*/");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Comment, tokenizer.getType());
        assertNull(tokenizer.getString());
        assertEquals(2, tokenizer.getLine());

        assertFalse(tokenizer.next());
    }

    public void testBypassComment() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer("a=1/*hello*/+2//world");
        tokenizer.setBypassComment(true);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("+", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("2", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertFalse(tokenizer.next());

        tokenizer = getTokenizer("a=1/*hello\nworld\n!*/+//world\n2");
        tokenizer.setBypassComment(true);

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Identifier, tokenizer.getType());
        assertEquals("a", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("=", tokenizer.getString());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("1", tokenizer.getString());
        assertEquals(1, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Operator, tokenizer.getType());
        assertEquals("+", tokenizer.getString());
        assertEquals(3, tokenizer.getLine());

        assertTrue(tokenizer.next());
        assertEquals(Token.Type.Number, tokenizer.getType());
        assertEquals("2", tokenizer.getString());
        assertEquals(4, tokenizer.getLine());

        assertFalse(tokenizer.next());
    }

    public void testMultilineStringInput() throws QuarksLexicalException {
        DefaultTokenizer tokenizer = getTokenizer();
        tokenizer.setInput("\n\nabc=\n12/* \nhel */ lo\n ! */\n+666\nworld\n\n");
        tokenizer.setBypassComment(false);

        assertTrue(tokenizer.next());
        assertEquals("abc", tokenizer.getString());
        assertTrue(tokenizer.next());
        assertEquals("=", tokenizer.getString());
        assertTrue(tokenizer.next());
        assertEquals("12", tokenizer.getString());
        assertTrue(tokenizer.next());
        assertEquals(" \nhel ", tokenizer.getString());
        assertTrue(tokenizer.next());
        assertEquals("lo", tokenizer.getString());
        assertTrue(tokenizer.next());
        assertEquals("!", tokenizer.getString());
        assertTrue(tokenizer.next());
        assertEquals("*/", tokenizer.getString());
        assertTrue(tokenizer.next());
        assertEquals("+", tokenizer.getString());
        assertTrue(tokenizer.next());
        assertEquals("666", tokenizer.getString());
        assertTrue(tokenizer.next());
        assertEquals("world", tokenizer.getString());
        assertFalse(tokenizer.next());
    }

    public void testRaiseException() {
        QuarksLexicalException err = null;
        DefaultTokenizer t = getTokenizer();

        t.setInput(new StringReader("\r\n\n'123"));
        try {
            t.next();
        } catch (QuarksLexicalException e) {
            err = e;
        }

        assertNotNull(err);
        assertEquals(3, err.getLine());
        assertEquals(4, err.getColumn());
        err.printErrorLocation();

        err = null;

        t.setInput(new StringReader("'\\r\\n\\n123"));
        try {
            t.next();
        } catch (QuarksLexicalException e) {
            err = e;
        }

        assertNotNull(err);
        assertEquals(1, err.getLine());
        assertEquals(10, err.getColumn());
        err.printErrorLocation();
    }

}
