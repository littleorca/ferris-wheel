package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;
import com.ctrip.ferriswheel.quarks.token.DefaultTokenizer;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringReader;

public class TestDefaultLRProcessor extends TestCase {

    public void testParsingAlgorithmExpression() throws QuarksLexicalException,
            QuarksSyntaxException, IOException {
        String bnf = "Algorithm.bnf";
        String exp = "3+4*(5+6)";
        System.out.println("PROCESS: " + exp);
        LR1ParsingTableConstructor constructor = new LR1ParsingTableConstructor();
        ParsingTable table = constructor.construct(Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(bnf));
        DefaultTokenizer tokenizer = new DefaultTokenizer(new StringReader(exp));
        ListenerMock listener = new ListenerMock();
        DefaultLRProcessor processor = new DefaultLRProcessor(table, tokenizer);
        assertTrue(processor.process(listener));

        String log = listener.getStrLog();
        System.out.println(log);
        assertEquals(28, listener.events.size());
        assertEquals(
                " # BEGIN\n" +
                        " # SHIFT: \"number\": 3\n" +
                        " # REDUCE: <P>: [\"number\"]\n" +
                        " # REDUCE: <M>: [<P>]\n" +
                        " # REDUCE: <A>: [<M>]\n" +
                        " # SHIFT: \"+\": +\n" +
                        " # SHIFT: \"number\": 4\n" +
                        " # REDUCE: <P>: [\"number\"]\n" +
                        " # REDUCE: <M>: [<P>]\n" +
                        " # SHIFT: \"*\": *\n" +
                        " # SHIFT: \"(\": (\n" +
                        " # SHIFT: \"number\": 5\n" +
                        " # REDUCE: <P>: [\"number\"]\n" +
                        " # REDUCE: <M>: [<P>]\n" +
                        " # REDUCE: <A>: [<M>]\n" +
                        " # SHIFT: \"+\": +\n" +
                        " # SHIFT: \"number\": 6\n" +
                        " # REDUCE: <P>: [\"number\"]\n" +
                        " # REDUCE: <M>: [<P>]\n" +
                        " # REDUCE: <A>: [<A>, \"+\", <M>]\n" +
                        " # REDUCE: <E>: [<A>]\n" +
                        " # SHIFT: \")\": )\n" +
                        " # REDUCE: <P>: [\"(\", <E>, \")\"]\n" +
                        " # REDUCE: <M>: [<M>, \"*\", <P>]\n" +
                        " # REDUCE: <A>: [<A>, \"+\", <M>]\n" +
                        " # REDUCE: <E>: [<A>]\n" +
                        " # REDUCE: <quarks>: [<E>]\n" +
                        " # FINISH\n",
                log
        );

        assertFalse(processor.process(listener));
    }
}
