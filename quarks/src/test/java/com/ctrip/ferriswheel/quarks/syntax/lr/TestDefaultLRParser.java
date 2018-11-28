package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.syntax.ASTNode;
import com.ctrip.ferriswheel.quarks.token.DefaultTokenizer;
import com.ctrip.ferriswheel.quarks.util.ASTUtil;
import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TestDefaultLRParser extends TestCase {

    public void testParsingAlgorithmExpression() throws QuarksLexicalException,
            QuarksSyntaxException, IOException {
        String bnf = "Algorithm.bnf";
        LR1ParsingTableConstructor constructor = new LR1ParsingTableConstructor();
        ParsingTable table = constructor.construct(Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(bnf));
        DefaultTokenizer tokenizer = new DefaultTokenizer(new StringReader(
                "3+4*(5+6)"));
        DefaultLRParser parser = new DefaultLRParser(table, tokenizer);
        ASTNode<Object> node = parser.next();
        assertNotNull(node);
        System.out.println(ASTUtil.dump(node));

        assertEquals("quarks", node.getHandle().getSymbol());
        assertEquals(1, node.getChildCount());

        List<Symbol> symbols = new ArrayList<Symbol>();
        Stack<ASTNode<Object>> nodes = new Stack<ASTNode<Object>>();
        nodes.add(node.getChild(0));

        while (!nodes.isEmpty()) {
            ASTNode<Object> n = nodes.pop();
            symbols.add(n.getHandle());
            for (int i = 0; i < n.getChildCount(); i++) {
                nodes.push(n.getChild(i));
            }
        }

        assertEquals(25, symbols.size());
        int i = 0;
        assertEquals("E", symbols.get(i++).getSymbol());
        assertEquals("A", symbols.get(i++).getSymbol());
        assertEquals("M", symbols.get(i++).getSymbol());
        assertEquals("P", symbols.get(i++).getSymbol());
        assertEquals(")", symbols.get(i++).getSymbol());
        assertEquals("E", symbols.get(i++).getSymbol());
        assertEquals("A", symbols.get(i++).getSymbol());
        assertEquals("M", symbols.get(i++).getSymbol());
        assertEquals("P", symbols.get(i++).getSymbol());
        assertEquals("number", symbols.get(i++).getSymbol());
        assertEquals("+", symbols.get(i++).getSymbol());
        assertEquals("A", symbols.get(i++).getSymbol());
        assertEquals("M", symbols.get(i++).getSymbol());
        assertEquals("P", symbols.get(i++).getSymbol());
        assertEquals("number", symbols.get(i++).getSymbol());
        assertEquals("(", symbols.get(i++).getSymbol());
        assertEquals("*", symbols.get(i++).getSymbol());
        assertEquals("M", symbols.get(i++).getSymbol());
        assertEquals("P", symbols.get(i++).getSymbol());
        assertEquals("number", symbols.get(i++).getSymbol());
        assertEquals("+", symbols.get(i++).getSymbol());
        assertEquals("A", symbols.get(i++).getSymbol());
        assertEquals("M", symbols.get(i++).getSymbol());
        assertEquals("P", symbols.get(i++).getSymbol());
        assertEquals("number", symbols.get(i++).getSymbol());
        assertEquals(25, i);
    }

    public static void main(String[] args) throws QuarksLexicalException,
            QuarksSyntaxException, IOException {
        String bnf = "quarks.bnf";
        LR1ParsingTableConstructor constructor = new LR1ParsingTableConstructor();
        ParsingTable table = constructor.construct(Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(bnf));
        // ClosureUtil.dumpGraphvizDot(constructor.getRootClosure(),
        // new FileOutputStream("/Users/liuhaifeng/Desktop/test.dot"));
        // ClosureUtil.dump(constructor.getRootClosure());
        DefaultTokenizer tokenizer = new DefaultTokenizer(new StringReader(
                "3<5"));
        DefaultLRParser parser = new DefaultLRParser(table, tokenizer);
        ASTNode<Object> node = parser.next();
        System.out.println(ASTUtil.dump(node));

        tokenizer.setInput(new StringReader(
                "false||!func(a, 3>=5, !b)&&false||true||!true&&false"));
        parser = new DefaultLRParser(table, tokenizer);
        node = parser.next();
        System.out.println(ASTUtil.dump(node));

        tokenizer.setInput(new StringReader("func(a)<b"));
        parser = new DefaultLRParser(table, tokenizer);
        node = parser.next();
        System.out.println(ASTUtil.dump(node));

        tokenizer.setInput(new StringReader("3+4*(5 - 2)"));
        parser = new DefaultLRParser(table, tokenizer);
        node = parser.next();
        System.out.println(ASTUtil.dump(node));

        tokenizer.setInput(new StringReader("t.b == 5"));
        parser = new DefaultLRParser(table, tokenizer);
        node = parser.next();
        System.out.println(ASTUtil.dump(node));

        tokenizer.setInput(new StringReader("select * from tbl where true"));
        parser = new DefaultLRParser(table, tokenizer);
        node = parser.next();
        System.out.println(ASTUtil.dump(node));

        tokenizer
                .setInput(new StringReader(
                        "select t.a, 3 as b from tbl as t where t.b == 1 && max(t.c, t.d) < 5 limit 10"));
        parser = new DefaultLRParser(table, tokenizer);
        node = parser.next();
        System.out.println(ASTUtil.dump(node));
    }

}
