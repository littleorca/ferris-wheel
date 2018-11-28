package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.quarks.LexContext;
import com.ctrip.ferriswheel.quarks.StringDecoder;
import com.ctrip.ferriswheel.quarks.token.DefaultLexContext;
import com.ctrip.ferriswheel.quarks.token.DefaultStringCodec;

public class FormulaLexContext implements LexContext {

    private static volatile FormulaLexContext DEFAULT;

    final String[] operators = new String[]{"+", "-", "*", "/", "^",
            "%", "=", "<", "<=", ">=", ">", "<>", "&", "!", ":"};
    final String[] delimiters = new String[]{",", "(", ")"};
    final String[] keywords = new String[]{};
    final String[] literals = new String[]{"true", "false", "null"};
    final DefaultStringCodec stringCodec = new DefaultStringCodec();

    public static FormulaLexContext getDefaultInstance() {
        if (DEFAULT == null) {
            synchronized (DefaultLexContext.class) {
                if (DEFAULT == null) {
                    DEFAULT = new FormulaLexContext();
                }
            }
        }
        return DEFAULT;
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
        return (ch == '#');
    }

    @Override
    public boolean isIdentifierQuoteEnd(char ch, char quoteStart) {
        return (ch == '!');
    }

    @Override
    public StringDecoder getStringDecoder() {
        return stringCodec;
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
