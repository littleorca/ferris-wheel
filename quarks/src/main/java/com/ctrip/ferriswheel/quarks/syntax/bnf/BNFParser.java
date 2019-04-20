package com.ctrip.ferriswheel.quarks.syntax.bnf;

import com.ctrip.ferriswheel.quarks.*;
import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;
import com.ctrip.ferriswheel.quarks.syntax.DefaultSyntaxContext;
import com.ctrip.ferriswheel.quarks.token.DefaultLexContext;
import com.ctrip.ferriswheel.quarks.token.DefaultTokenizer;

import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

public class BNFParser {

    final LexContext lexContext = new DefaultLexContext(new String[] { "::=",
            "|", "<", ">" }, new String[0], new String[0], new String[0]);
    final SyntaxContext syntaxContext;

    DefaultTokenizer tokenizer;
    Symbol inputBuffer;

    public BNFParser() {
        tokenizer = new DefaultTokenizer(null, lexContext);
        this.syntaxContext = DefaultSyntaxContext.getDefaultInstance();
    }

    public BNFParser(Reader in) {
        this(in, DefaultSyntaxContext.getDefaultInstance());
    }

    public BNFParser(Reader in, SyntaxContext syntaxContext) {
        tokenizer = new DefaultTokenizer(in, lexContext);
        this.syntaxContext = syntaxContext;
    }

    public void setInput(Reader in) {
        tokenizer.setInput(in);
        this.inputBuffer = null;
    }

    public BNFRule next() throws QuarksLexicalException, QuarksSyntaxException {
        BNFRule rule = new BNFRule();

        if (inputBuffer != null) {
            if (inputBuffer.isTerminal())
                throw new QuarksSyntaxException("Illegal BNF rule.", tokenizer);

            rule.symbol = inputBuffer;
            inputBuffer = null;

        } else {
            if (!tokenizer.next())
                return null;

            assertCurrent(tokenizer, Token.Type.Operator, "<");
            assertNext(tokenizer, Token.Type.Identifier, null);
            rule.symbol = BNFSymbol.nonTerminalSymbol(tokenizer.getString());
            assertNext(tokenizer, Token.Type.Operator, ">");
            assertNext(tokenizer, Token.Type.Operator, "::=");
        }

        List<List<Symbol>> choices = new LinkedList<List<Symbol>>();
        List<Symbol> symbols = new LinkedList<Symbol>();

        while (tokenizer.next()) {
            if (tokenizer.getType() == Token.Type.Operator && tokenizer.equalsTo("|")) {
                if (symbols.size() < 1) {
                    throw new QuarksSyntaxException("empty symbol sequence.",
                            tokenizer);
                }

                choices.add(symbols);
                symbols = new LinkedList<Symbol>();

            } else if (tokenizer.getType() == Token.Type.Operator
                    && tokenizer.equalsTo("<")) {
                assertNext(tokenizer, Token.Type.Identifier, null);
                if (tokenizer.equalsTo(syntaxContext.getNumberSymbol()
                        .getSymbol())
                        || tokenizer.equalsTo(syntaxContext.getStringSymbol()
                                .getSymbol())
                        || tokenizer.equalsTo(syntaxContext
                                .getIdentifierSymbol().getSymbol())
                        || tokenizer.equalsTo(syntaxContext
                                .getQuotedIdentifierSymbol().getSymbol())) {
                    symbols.add(BNFSymbol.terminalSymbol(tokenizer.getString()));

                } else {
                    symbols.add(BNFSymbol.nonTerminalSymbol(tokenizer
                            .getString()));
                }
                assertNext(tokenizer, Token.Type.Operator, ">");

            } else if (tokenizer.getType() == Token.Type.String) {
                symbols.add(BNFSymbol.terminalSymbol(tokenizer.getString()));

            } else if (tokenizer.getType() == Token.Type.Operator
                    && tokenizer.equalsTo("::=")) {
                if (symbols.size() < 2)
                    throw new QuarksSyntaxException("Illegal BNF rule.", tokenizer);
                inputBuffer = symbols.remove(symbols.size() - 1);
                if (inputBuffer.isTerminal())
                    throw new QuarksSyntaxException("Illegal BNF rule.", tokenizer);

                break;

            } else {
                throw new QuarksSyntaxException("Unexpected token: "
                        + tokenizer.getType() + ": " + tokenizer.getString(),
                        tokenizer);
            }
        }

        if (symbols.size() < 1)
            throw new QuarksSyntaxException("empty symbol sequence.", tokenizer);

        choices.add(symbols);
        rule.choices = choices;

        return rule;
    }

    void assertNext(Tokenizer tokenizer, Token.Type type, String str)
            throws QuarksSyntaxException, QuarksLexicalException {
        if (!tokenizer.next())
            throw new QuarksSyntaxException("Expects more token.", tokenizer);
        assertCurrent(tokenizer, type, str);
    }

    void assertCurrent(Tokenizer tokenizer, Token.Type type, String str)
            throws QuarksSyntaxException, QuarksLexicalException {
        if (type != null && type != tokenizer.getType())
            throw new QuarksSyntaxException("Unexpected token type: "
                    + tokenizer.getType() + ", expects type is: " + type + ".",
                    tokenizer);
        if (str != null && !tokenizer.equalsTo(str))
            throw new QuarksSyntaxException("Unexpected token, expects \""
                    + str + "\" here.", tokenizer);
    }

}
