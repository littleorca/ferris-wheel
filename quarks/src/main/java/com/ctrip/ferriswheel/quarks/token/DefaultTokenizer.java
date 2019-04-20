package com.ctrip.ferriswheel.quarks.token;

import com.ctrip.ferriswheel.quarks.LexContext;
import com.ctrip.ferriswheel.quarks.Token;
import com.ctrip.ferriswheel.quarks.TokenDecoder;
import com.ctrip.ferriswheel.quarks.Tokenizer;
import com.ctrip.ferriswheel.quarks.exception.QuarksLexicalException;
import com.ctrip.ferriswheel.quarks.util.Trie;

import java.io.*;

/**
 * The default lexical analysis tool implementation which implements
 * {@link Tokenizer} interface. If combined with {@link DefaultLexContext}, the
 * main features will be:
 * <ul>
 * <li>Java style identifier.</li>
 * <li>Java style string escape rule.</li>
 * <li>C/Java style number format.</li>
 * <li>Case insensitive keywords.</li>
 * </ul>
 *
 * @author liuhaifeng
 */
public class DefaultTokenizer extends DefaultToken implements Tokenizer {
    private static final long serialVersionUID = 1L;

    /**
     * Examples:
     * <p>
     * .12 => INIT, FLOAT_CANDIDATE, FLOAT, FLOAT, FINISH<br>
     * .1f => INIT, FLOAT_CANDIDATE, FLOAT, FLOAT_END, FINISH<br>
     * 123 => INIT, INT, INT, INT, FINISH<br>
     * 12L => INIT, INT, INT, LONG_END, FINISH<br>
     * 1.2 => INIT, INT, FLOAT_NEW, FLOAT,FINISH<br>
     * 012 => INIT, OCT_CANDIDATE, OCT, OCT, FINISH<br>
     * 0x1 => INIT, OCT_CANDIDATE, HEX_CANDIDATE, HEX, FINISH<br>
     * 0 => INIT, OCT_CANDIDATE, FINISH<br>
     *
     * @see {@link DefaultTokenizer#parseNumber()}
     */
    enum NumberState {
        INIT, FLOAT_CANDIDATE, FLOAT_NEW, FLOAT, FLOAT_END, INT, LONG_END, OCT_CANDIDATE, OCT, HEX_CANDIDATE, HEX, FINISH
    }

    private int next;
    private boolean isInBlockComment;
    private boolean bypassComment;
    private LexContext lex;
    private Trie<Type> tokenTrie;
    private BufferedReader reader;

    public DefaultTokenizer() {
        this((Reader) null, null);
    }

    public DefaultTokenizer(Reader in) {
        this(in, null);
    }

    public DefaultTokenizer(LexContext lex) {
        this(null, lex);
    }

    public DefaultTokenizer(Reader in, LexContext lex) {
        if (in != null) {
            if (in instanceof BufferedReader)
                this.reader = (BufferedReader) in;
            else
                this.reader = new BufferedReader(in);
        }
        this.line = 0;
        this.isInBlockComment = false;
        this.bypassComment = true;
        setLexContext(lex);
    }

    public void setBypassComment(boolean bypassComment) {
        this.bypassComment = bypassComment;
    }

    @Override
    public void setLexContext(LexContext lexContext) {
        this.lex = lexContext == null ? DefaultLexContext.getDefaultInstance()
                : lexContext;
        this.tokenTrie = new Trie<>();

        for (String operator : this.lex.getOperators()) {
            tokenTrie.put(operator, Type.Operator);
        }

        for (String delimiter : this.lex.getDelimiters()) {
            tokenTrie.put(delimiter, Type.Delimiter);
        }

        for (String keyword : this.lex.getKeywords()) {
            tokenTrie.put(keyword, Type.Keyword);
        }

        for (String literal : this.lex.getLiterals()) {
            tokenTrie.put(literal, Type.Literal);
        }
    }

    @Override
    public void setInput(Reader in) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }

        if (in instanceof BufferedReader)
            this.reader = (BufferedReader) in;
        else
            this.reader = new BufferedReader(in);

        this.src = null;
        this.from = this.to = this.next = 0;
        this.line = 0;
        this.type = null;
        this.token = null;
    }

    /**
     * Caution:
     * In this mode, line number is not processed at present.
     *
     * @param in
     */
    public void setInput(String in) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            } finally {
                this.reader = null;
            }
        }
        this.src = in;
        this.from = this.to = this.next = 0;
        this.line = -1;
        this.type = null;
        this.token = null;
    }

    @Override
    public boolean next() throws QuarksLexicalException {
        boolean ret;
        while ((ret = moveOn()) && bypassComment && type == Type.Comment) ;
        return ret;
    }

    protected boolean moveOn() throws QuarksLexicalException {
        type = null;
        token = null;
        from = to = next;
        char ch;

        TokenDecoder quotedIdentifierDecoder = lex.getQuotedIdentifierDecoder();
        TokenDecoder stringDecoder = lex.getStringDecoder();

        while (true) { // seek to next non-whitespace character.
            if (src == null || from >= src.length()) {
                if (reader == null)
                    return false;

                from = to = next = 0;
                try {
                    src = reader.readLine();

                } catch (IOException e) {
                    throw new QuarksLexicalException(e);
                }

                if (src == null)
                    return false;

                line++;

                /**
                 * As we read as more as possible when processing block comment,
                 * The only case we need to check block comment flag is when we
                 * entered a new line.
                 */
                if (isInBlockComment) {
                    type = Type.Comment;
                    to = src.lastIndexOf(lex.getBlockCommentTerminator());
                    if (to < from) {
                        next = to = src.length();
                    } else {
                        next = to + lex.getBlockCommentTerminator().length();
                        isInBlockComment = false;
                    }
                    return true;
                }
            }

            while (from < src.length()) {
                ch = src.charAt(from);
                if (Character.isWhitespace(ch)) {
                    from++;
                } else {
                    break;
                }
            }

            if (from < src.length()) {
                break;
            }
        }

        ch = src.charAt(from);

        if (quotedIdentifierDecoder.isStartChar(ch)) {
            to = from + 1;
            return parseQuotedIdentifier(ch);

        } else if (stringDecoder.isStartChar(ch)) {
            to = from + 1;
            return parseString(ch);

        } else {
            to = from;

            if (isNumberStart(ch)) {
                if (parseNumber())
                    return true;
            }

            if (lex.isIdentifierStart(ch)) {
                to = from;

                if (parseIdentifier()) {
                    checkTokenTrie();
                    return true;
                }
            }

            Trie.Entry<Type> entry = tokenTrie.matches(src, to);

            if (entry != null) {
                to += entry.getKey().length();
                type = entry.getValue();
                next = to;

                if (type == Type.Delimiter) {
                    if (lex.isLineComment(getString())) {
                        token = null;
                        type = Type.Comment;
                        from = next;
                        to = src.length();
                        next = to;

                    } else if (lex.isBlockComment(getString())) {
                        token = null;
                        type = Type.Comment;
                        from = next;
                        to = src.indexOf(lex.getBlockCommentTerminator());
                        if (to < from) {
                            to = src.length();
                            isInBlockComment = true;
                            next = to;

                        } else {
                            next = to + lex.getBlockCommentTerminator().length();
                        }
                    }
                }

                return true;
            }

            raiseException("Unrecognized character: '" + ch + "'.");
        }

        return true;
    }

    boolean parseString(char quoteStart) throws QuarksLexicalException {
        TokenDecoder decoder = lex.getStringDecoder();
        decoder.start(quoteStart);
        while (to < src.length()) {
            char ch = src.charAt(to);
            boolean accepted = decoder.feed(ch);
            if (accepted) {
                to++;
            } else {
                break;
            }
        }
        if (decoder.isTerminable()) {
            type = Type.String;
            token = decoder.finish();
            next = to;
            return true;
        } else {
            raiseException("Unexpected end.");
            return false; // useless as above line will certainly throw an exception.
        }
    }

    /**
     * About the states:
     * <p>
     * <ul>
     * <li>when state changes to <code>'FINISH'</code>, <code>'to'</code> points
     * to the next character out of the number.</li>
     * </ul>
     *
     * @return
     * @throws QuarksLexicalException
     */
    boolean parseNumber() throws QuarksLexicalException {
        NumberState ns = NumberState.INIT;
        char ch;
        int start = to;

        for (; to < src.length(); to++) {
            ch = src.charAt(to);

            switch (ns) {
                case INIT:
                    if (ch == '.')
                        ns = NumberState.FLOAT_CANDIDATE;
                    else if (ch == '0')
                        ns = NumberState.OCT_CANDIDATE;
                    else if (Character.isDigit(ch))
                        ns = NumberState.INT;
                    else
                        raiseException("Illegal leading character of number.");
                    break;
                case FLOAT_CANDIDATE: // dot may be an operator.
                    if (Character.isDigit(ch))
                        ns = NumberState.FLOAT;
                    else {
                        to = start;
                        return false;
                    }
                    break;
                case FLOAT_NEW:
                    if (Character.isDigit(ch))
                        ns = NumberState.FLOAT;
                    else
                        raiseException("Illegal float number, expects digit character.");
                    break;
                case FLOAT:
                    if (ch == 'f' || ch == 'F')
                        ns = NumberState.FLOAT_END;
                    else if (!Character.isDigit(ch))
                        ns = NumberState.FINISH;
                    break;
                case OCT_CANDIDATE:
                    if (ch == '.')
                        ns = NumberState.FLOAT_NEW;
                    else if (ch == 'x')
                        ns = NumberState.HEX_CANDIDATE;
                    else if (ch >= '0' && ch <= '7')
                        ns = NumberState.OCT;
                    else
                        ns = NumberState.FINISH;
                    break;
                case OCT:
                    if (ch < '0' || ch > '7')
                        ns = NumberState.FINISH;
                    break;
                case HEX_CANDIDATE:
                    if (Character.isDigit(ch) || (ch >= 'a' && ch <= 'f')
                            || (ch >= 'A' && ch <= 'F'))
                        ns = NumberState.HEX;
                    else
                        raiseException("Illegal hex number, expects [0-9a-fA-F].");
                    break;
                case HEX:
                    if (!(Character.isDigit(ch) || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F')))
                        ns = NumberState.FINISH;
                    break;
                case INT:
                    if (ch == '.')
                        ns = NumberState.FLOAT_NEW;
                    else if (ch == 'l' || ch == 'L')
                        ns = NumberState.LONG_END;
                    else if (!Character.isDigit(ch))
                        ns = NumberState.FINISH;
                    break;
                case LONG_END:
                case FLOAT_END:
                    ns = NumberState.FINISH;
                    break;
                case FINISH:
                    raiseException("Impossible!");
            }

            if (ns == NumberState.FINISH) {
                next = to;
                type = Type.Number;
                return true;
            }
        }

        if (ns == NumberState.OCT_CANDIDATE)
            ns = NumberState.INT;

        switch (ns) {
            case INT:
            case LONG_END:
            case FLOAT:
            case FLOAT_END:
            case OCT:
            case HEX:
                next = to;
                type = Type.Number;
                return true;

            default:
                to = start;
                return false;
        }
    }

    boolean parseIdentifier() {
        char startCh = src.charAt(to);
        to++;
        while (to < src.length() && (lex.isIdentifierPart(src.charAt(to), startCh)))
            to++;

        next = to;
        type = Type.Identifier;
        return true;
    }

    boolean parseQuotedIdentifier(char quoteStart) throws QuarksLexicalException {
        TokenDecoder decoder = lex.getQuotedIdentifierDecoder();
        decoder.start(quoteStart);
        while (to < src.length()) {
            char ch = src.charAt(to);
            boolean accepted = decoder.feed(ch);
            if (accepted) {
                to++;
            } else {
                break;
            }
        }
        if (decoder.isTerminable()) {
            type = Type.QuotedIdentifier;
            token = decoder.finish();
            next = to;
            return true;
        } else {
            raiseException("Unexpected end.");
            return false; // useless as above line will certainly throw an exception.
        }
    }

    boolean checkTokenTrie() {
        Type matchedType = tokenTrie.get(src, from, to);
        if (matchedType != null) {
            type = matchedType;
            return true;

        } else {
            return false;
        }
    }

    boolean isNumberStart(char ch) {
        return (ch == '.' || (ch >= '0' && ch <= '9'));
    }

    boolean isOperatorStart(char ch) {
        return false;
    }

    boolean isDelimiterStart(char ch) {
        return false;
    }

    @Override
    public Token getToken() {
        if (src == null || to <= from)
            return null;

        return new DefaultToken(src, from, to, line, type, token);
    }

    @Override
    public boolean isEmpty() {
        return to <= from;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return from;
    }

    private void raiseException(String message) throws QuarksLexicalException {
        raiseException(message, to);
    }

    private void raiseException(String message, int pos)
            throws QuarksLexicalException {
        type = null;
        token = null;
        from = to;
        if (next < to)
            next = to;
        throw new QuarksLexicalException(src, getLine(), pos, message);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Quarks Lexical Analysis Program");
        System.out.println("Options:");
        System.out.println("    -f <input file>     "
                + "-- read input from specified file instead of stdin.");
        System.out.println("    -o <output file>    "
                + "-- write output to the specified file.");

        BufferedReader reader = null;
        BufferedWriter writer = null;
        boolean useStdin = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-f".equals(arg)) {
                if (++i >= args.length) {
                    System.err
                            .println("Bad option '-f', please specify input file path.");
                    System.exit(1);
                }

                try {
                    reader = new BufferedReader(new InputStreamReader(
                            new FileInputStream(args[i])));
                } catch (FileNotFoundException e) {
                    System.err.println("Invalid input file: " + args[i]);
                    System.exit(1);
                }

            } else if ("-d".equals(arg)) {
                if (++i >= args.length) {
                    System.err
                            .println("Bad option '-d', please specify output file path.");
                    System.exit(1);
                }

                try {
                    writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(args[i])));
                } catch (FileNotFoundException e) {
                    System.err.println("Invalid output file: " + args[i]);
                    System.exit(1);
                }
            }
        }

        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(System.in));
            useStdin = true;
            System.out.print("Quarks Tokenizer> ");
            System.out.flush();
        }

        if (writer == null) {
            writer = new BufferedWriter(new OutputStreamWriter(System.out));
        }

        DefaultTokenizer t = new DefaultTokenizer(reader);
        try {
            while (t.next()) {
                writer.write(t.debugInfo());
                writer.write("\n");
                writer.flush();
            }

        } catch (QuarksLexicalException e) {
            e.printErrorLocation();
            System.err.println(e.getMessage());
        }

        if (useStdin) {
            System.out.print("Quarks Tokenizer> ");
            System.out.flush();
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
