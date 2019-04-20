package com.ctrip.ferriswheel.quarks.token;

import com.ctrip.ferriswheel.quarks.LexContext;
import com.ctrip.ferriswheel.quarks.TokenDecoder;

public class DefaultLexContext implements LexContext {
    private static final long serialVersionUID = 1L;

    private static volatile DefaultLexContext DEFAULT;

    final String[] operators;
    final String[] delimiters;
    final String[] keywords;
    final String[] literals;
    final TokenDecoder quotedIdentifierDecoder;
    final TokenDecoder stringDecoder;

    public static DefaultLexContext getDefaultInstance() {
        if (DEFAULT == null) {
            synchronized (DefaultLexContext.class) {
                if (DEFAULT == null) {
                    DEFAULT = new DefaultLexContext(
                            /* operators --> */new String[]{"+", "-", "*", "/", "=",
                            "%", "<", "<=", ">=", ">", "==", "!=", "(", ")",
                            ".", "[", "]", "&&", "||", "!", "++", "--", "&",
                            "|", "^", "~", "<<", ">>", "+=", "-=", "*=", "/=",
                            "%=", "&=", "|=", "^=", "~=", "<<=", ">>=", ":",
                            "?", "->"},
                            /* delimiters -> */new String[]{",", ";", "{", "}", "//", "/*", "*/"},
                            /* keywords ---> */new String[]{},
                            /* literals ---> */new String[]{"true", "false", "null"},
                            new DummyDecoder(),
                            new DefaultStringDecoder());
                }
            }
        }
        return DEFAULT;
    }

    public DefaultLexContext(String[] operators, String[] delimiters,
                             String[] keywords, String[] literals) {
        this(operators, delimiters, keywords, literals,
                new DummyDecoder(), new DefaultStringDecoder());
    }

    public DefaultLexContext(String[] operators, String[] delimiters,
                             String[] keywords, String[] literals,
                             TokenDecoder quotedIdentifierDecoder,
                             TokenDecoder stringDecoder) {
        this.operators = operators;
        this.delimiters = delimiters;
        this.keywords = keywords;
        this.literals = literals;
        this.quotedIdentifierDecoder = quotedIdentifierDecoder;
        this.stringDecoder = stringDecoder;
    }

    @Override
    public boolean isIdentifierStart(char ch) {
        if (ch == '$' || ch == '_' || (ch >= 'a' && ch <= 'z')
                || (ch >= 'A' && ch <= 'Z'))
            return true;

        return false;
    }

    @Override
    public boolean isIdentifierPart(char pendingChar, char identifierStart) {
        if (isIdentifierStart(pendingChar))
            return true;

        return (pendingChar >= '0' && pendingChar <= '9');
    }

    @Override
    public TokenDecoder getQuotedIdentifierDecoder() {
        return quotedIdentifierDecoder;
    }

    @Override
    public TokenDecoder getStringDecoder() {
        return stringDecoder;
    }

    @Override
    public boolean isLineComment(String token) {
        return "//".equals(token);
    }

    @Override
    public boolean isBlockComment(String token) {
        return "/*".equals(token);
    }

    @Override
    public String getBlockCommentTerminator() {
        return "*/";
    }

    @Override
    public String[] getOperators() {
        return operators;
    }

    @Override
    public String[] getDelimiters() {
        return delimiters;
    }

    @Override
    public String[] getKeywords() {
        return keywords;
    }

    @Override
    public String[] getLiterals() {
        return literals;
    }

}
