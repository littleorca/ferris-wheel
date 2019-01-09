package com.ctrip.ferriswheel.quarks.syntax.bnf;

import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;
import junit.framework.TestCase;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

public class TestBNFParser extends TestCase {

    public void testParser() throws QuarksLexicalException,
            QuarksSyntaxException {
        BNFParser parser = new BNFParser(
                new StringReader(
                        "<MultipleExpression> ::= <MultipleExpression> \"×\" <PrimaryExpression> | <MultipleExpression> \"/\" <PrimaryExpression> | <PrimaryExpression>\r\n"
                                + "<PrimaryExpression> ::= <number>"));
        BNFRule rule = parser.next();

        assertNotNull(rule);
        assertNotNull(rule.getSymbol());
        assertEquals("MultipleExpression", rule.getSymbol().getSymbol());
        assertFalse(rule.getSymbol().isTerminal());

        // ------------------

        assertNotNull(rule.getChoices());
        assertEquals(3, rule.getChoices().size());

        Iterator<List<Symbol>> it = rule.getChoices().iterator();
        List<Symbol> symbols = it.next();

        assertNotNull(symbols);
        assertEquals(3, symbols.size());

        Iterator<Symbol> it2 = symbols.iterator();
        Symbol symbol = it2.next();

        assertNotNull(symbol);
        assertFalse(symbol.isTerminal());
        assertEquals("MultipleExpression", symbol.getSymbol());

        symbol = it2.next();

        assertNotNull(symbol);
        assertTrue(symbol.isTerminal());
        assertEquals("×", symbol.getSymbol());

        symbol = it2.next();

        assertNotNull(symbol);
        assertFalse(symbol.isTerminal());
        assertEquals("PrimaryExpression", symbol.getSymbol());

        // ------------------

        symbols = it.next();

        assertNotNull(symbols);
        assertEquals(3, symbols.size());

        it2 = symbols.iterator();
        symbol = it2.next();

        assertNotNull(symbol);
        assertFalse(symbol.isTerminal());
        assertEquals("MultipleExpression", symbol.getSymbol());

        symbol = it2.next();

        assertNotNull(symbol);
        assertTrue(symbol.isTerminal());
        assertEquals("/", symbol.getSymbol());

        symbol = it2.next();

        assertNotNull(symbol);
        assertFalse(symbol.isTerminal());
        assertEquals("PrimaryExpression", symbol.getSymbol());

        // ------------------

        symbols = it.next();

        assertNotNull(symbols);
        assertEquals(1, symbols.size());

        it2 = symbols.iterator();
        symbol = it2.next();

        assertNotNull(symbol);
        assertFalse(symbol.isTerminal());
        assertEquals("PrimaryExpression", symbol.getSymbol());

        // ===============================
        rule = parser.next();
        assertNotNull(rule);
        assertEquals(1, rule.getChoices().size());
        assertEquals("PrimaryExpression", rule.getSymbol().getSymbol());
        assertEquals(1, rule.getChoices().get(0).size());
        assertEquals("number", rule.getChoices().get(0).get(0).getSymbol());

        // ===============================
        rule = parser.next();
        assertNull(rule);
    }

}
