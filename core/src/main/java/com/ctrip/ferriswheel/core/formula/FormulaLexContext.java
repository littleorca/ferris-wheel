package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.quarks.LexContext;
import com.ctrip.ferriswheel.quarks.TokenDecoder;
import com.ctrip.ferriswheel.quarks.token.DefaultLexContext;

public class FormulaLexContext implements LexContext {

    private static volatile FormulaLexContext DEFAULT;

    final String[] operators = new String[]{"+", "-", "*", "/", "^",
            "%", "=", "<", "<=", ">=", ">", "<>", "&", "!", ":"};
    final String[] delimiters = new String[]{",", "(", ")"};
    final String[] keywords = new String[]{};
    final String[] literals = new String[]{
            "true",
            "false",
            "null",
            // error values
            "#NULL!",
            "#DIV/0!",
            "#VALUE!",
            "#REF!",
            "#NAME?",
            "#NUM!",
            "#N/A",
            "#GETTING_DATA",
    };
    final TokenDecoder quotedIdentifierDecoder = new FormulaQuotedIdentifierDecoder();
    final TokenDecoder stringDecoder = new FormulaStringDecoder();

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
        // '#' is used for parsing error values such as #REF!
        if (ch == '$'
                || ch == '_'
                || (ch >= 'a' && ch <= 'z')
                || (ch >= 'A' && ch <= 'Z')
                || ch == '#') {
            return true;

        } else {
            return false;
        }
    }

    @Override
    public boolean isIdentifierPart(char pendingChar, char identifierStart) {
        if (identifierStart == '#') {
            return (pendingChar == '/'
                    || pendingChar == '!'
                    || pendingChar >= 'a' && pendingChar <= 'z'
                    || pendingChar >= 'A' && pendingChar <= 'Z'
                    || pendingChar >= '0' && pendingChar <= '9');

        } else {
            if (isIdentifierStart(pendingChar))
                return true;

            return (pendingChar >= '0' && pendingChar <= '9');
        }
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

    class FormulaQuotedIdentifierDecoder extends FormulaStringDecoder {
        @Override
        public boolean isStartChar(char ch) {
            return ch == '\'';
        }

        @Override
        boolean isEndChar(char ch) {
            if (startQuote == '\'') {
                return ch == startQuote;
            }
            return false;
        }

        @Override
        public boolean feed(char ch) {
            if (ended) {
                return false;
            }
            if (startQuote == '\'') {
                return super.feed(ch);

            } else if (startQuote == '#') {
                if (ch == '!') {
                    ended = true;
                } else {
                    stringBuilder.append(ch);
                }
                return true;

            } else {
                return false;
            }
        }
    }

    class FormulaStringDecoder implements TokenDecoder {
        final char QUOTE_FOR_STRING = '"';
        char startQuote;
        StringBuilder stringBuilder;
        boolean quotePending = false;
        boolean ended = false;

        @Override
        public boolean isStartChar(char ch) {
            return ch == QUOTE_FOR_STRING;
        }

        boolean isEndChar(char ch) {
            return (ch == startQuote);
        }

        @Override
        public void start(char quoteStart) {
            startQuote = quoteStart;
            stringBuilder = new StringBuilder();
            quotePending = false;
            ended = false;
        }

        @Override
        public boolean feed(char ch) {
            if (ended) {
                return false;
            }
            if (quotePending) {
                if (ch == startQuote) {
                    stringBuilder.append(ch);
                    quotePending = false;
                    return true;

                } else {
                    ended = true;
                    return false;
                }

            } else if (isEndChar(ch)) {
                quotePending = true;
                return true;

            } else {
                stringBuilder.append(ch);
                return true;
            }
        }

        @Override
        public boolean isTerminable() {
            return ended || quotePending;
        }

        @Override
        public String finish() {
            if (!isTerminable() || stringBuilder == null) {
                throw new RuntimeException();
            }
            String result = stringBuilder.toString();
            stringBuilder = null;
            return result;
        }
    }
}
