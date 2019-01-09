package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;

import java.io.IOException;

public class TestLR1ParsingTableConstructor {

    public static void main(String[] args) throws QuarksLexicalException,
            QuarksSyntaxException, IOException {
        String bnf = "quarks.bnf";
        LR1ParsingTableConstructor constructor = new LR1ParsingTableConstructor();
        ParsingTable table = constructor.construct(Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(bnf));
        System.out.println(table);
    }

}
