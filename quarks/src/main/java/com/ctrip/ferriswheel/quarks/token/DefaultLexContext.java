package com.ctrip.ferriswheel.quarks.token;

import com.ctrip.ferriswheel.quarks.LexContext;
import com.ctrip.ferriswheel.quarks.StringDecoder;

public class DefaultLexContext implements LexContext {
    private static final long serialVersionUID = 1L;

    private static volatile DefaultLexContext DEFAULT;

    final String[] operators;
    final String[] delimiters;
    final String[] keywords;
    final String[] literals;
    final StringDecoder stringDecoder;

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
                            new DefaultStringCodec());
                }
            }
        }
        return DEFAULT;
    }

    public DefaultLexContext(String[] operators, String[] delimiters,
                             String[] keywords, String[] literals) {
        this(operators, delimiters, keywords, literals, new DefaultStringCodec());
    }

    public DefaultLexContext(String[] operators, String[] delimiters,
                             String[] keywords, String[] literals,
                             StringDecoder stringDecoder) {
        this.operators = operators;
        this.delimiters = delimiters;
        this.keywords = keywords;
        this.literals = literals;
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
    public boolean isIdentifierPart(char ch) {
        if (isIdentifierStart(ch))
            return true;

        return (ch >= '0' && ch <= '9');
    }

    @Override
    public boolean isIdentifierQuoteStart(char ch) {
        return (ch == '`');
    }

    @Override
    public boolean isIdentifierQuoteEnd(char ch, char quoteStart) {
        return isIdentifierQuoteStart(quoteStart) && ch == quoteStart;
    }

    @Override
    public StringDecoder getStringDecoder() {
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
