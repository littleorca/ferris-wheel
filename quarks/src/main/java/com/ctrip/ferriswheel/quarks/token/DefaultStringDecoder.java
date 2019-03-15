package com.ctrip.ferriswheel.quarks.token;

import com.ctrip.ferriswheel.quarks.TokenDecoder;

import java.io.IOException;

public class DefaultStringDecoder implements TokenDecoder {
    private enum State {
        Normal,
        EscapePending,
        Unicode1,
        Unicode2,
        Unicode3,
        Unicode4,
        Octave2,
        Octave3,
        Ended
    }

    public static final char SINGLE_QUOTE = '\'';
    public static final char DOUBLE_QUOTE = '"';
    public static final char DEFAULT_QUOTE = DOUBLE_QUOTE;

    private Appendable appendable;
    private State state;
    private char quoteStart;
    private int d, n;

    @Override
    public boolean isStartChar(char ch) {
        return ch == SINGLE_QUOTE || ch == DOUBLE_QUOTE;
    }

    boolean isQuoteEnd(char ch, char quoteStart) {
        return ch == quoteStart;
    }

    @Override
    public void start(char quoteStart) {
        if (quoteStart != SINGLE_QUOTE && quoteStart != DOUBLE_QUOTE) {
            throw new IllegalArgumentException();
        }
        start(quoteStart, new StringBuilder());
    }

    public void start(char quoteStart, Appendable appendable) {
        this.appendable = appendable;
        this.state = State.Normal;
        this.quoteStart = quoteStart;
    }

    @Override
    public boolean feed(char ch) {
        try {
            return feed0(ch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean feed0(char ch) throws IOException {
        switch (state) {
            case Normal:
                if (isQuoteEnd(ch, quoteStart)) {
                    state = State.Ended;
                    return true;
                } else if (ch == '\\') {
                    state = State.EscapePending;
                } else {
                    appendable.append(ch); // state not changed
                }
                return true;
            case EscapePending:
                switch (ch) {
                    case 'b':
                        appendable.append('\b');
                        state = State.Normal;
                        break;
                    case 't':
                        appendable.append('\t');
                        state = State.Normal;
                        break;
                    case 'n':
                        appendable.append('\n');
                        state = State.Normal;
                        break;
                    case 'f':
                        appendable.append('\f');
                        state = State.Normal;
                        break;
                    case 'r':
                        appendable.append('\r');
                        state = State.Normal;
                        break;
                    case '"':
                        appendable.append('"');
                        state = State.Normal;
                        break;
                    case '\'':
                        appendable.append('\'');
                        state = State.Normal;
                        break;
                    case '\\':
                        appendable.append('\\');
                        state = State.Normal;
                        break;
                    case 'u':
                        state = State.Unicode1;
                        break;
                    default:
                        d = Character.digit(ch, 8);
                        if (d == -1) {
                            throw new IllegalArgumentException();
                        }
                        state = State.Octave2;
                }
                return true;
            case Unicode1:
                d = Character.digit(ch, 16);
                if (d == -1) {
                    throw new IllegalArgumentException();
                }
                state = State.Unicode2;
                return true;
            case Unicode2:
                n = Character.digit(ch, 16);
                if (n == -1) {
                    throw new IllegalArgumentException();
                }
                d = d * 16 + n;
                state = State.Unicode3;
                return true;
            case Unicode3:
                n = Character.digit(ch, 16);
                if (n == -1) {
                    throw new IllegalArgumentException();
                }
                d = d * 16 + n;
                state = State.Unicode4;
                return true;
            case Unicode4:
                n = Character.digit(ch, 16);
                if (n == -1) {
                    throw new IllegalArgumentException();
                }
                d = d * 16 + n;
                appendable.append((char) d);
                state = State.Normal;
                return true;
            case Octave2:
                n = Character.digit(ch, 8);
                if (n == -1) {
                    appendable.append((char) d);
                    state = State.Normal;
                    return feed(ch);
                }
                d = d * 8 + n;
                state = state.Octave3;
                return true;
            case Octave3:
                n = Character.digit(ch, 8);
                if (n == -1) {
                    appendable.append((char) d);
                    state = State.Normal;
                    return feed(ch);
                }
                d = d * 8 + n;
                appendable.append((char) d);
                state = state.Normal;
                return true;
            case Ended:
                return false;
            default:
                throw new RuntimeException(); // should never enter unless there's a bug.
        }
    }

    @Override
    public boolean isTerminable() {
        return state == State.Ended;
    }

    @Override
    public String finish() {
        try {
            if (!isTerminable() || appendable == null) {
                throw new RuntimeException();
            }
            return appendable.toString();

        } finally {
            appendable = null;
        }
    }

    /**
     * Decode the specified string.
     *
     * @param encoded Encoded string without quotes.
     * @return
     */
    public static String decode(String encoded) {
        return decode(encoded, 0, encoded.length());
    }

    /**
     * Decode the specified region of the encoded string. The region should not contain quotes.
     *
     * @param encoded
     * @param start
     * @param end
     * @return
     */
    public static String decode(String encoded, int start, int end) {
        StringBuilder sb = new StringBuilder();
        try {
            decode(encoded, start, end, sb);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return sb.toString();
    }

    /**
     * Decode the specified region of the encoded string and append the result.
     * The region of encoded string should not contain quotes.
     *
     * @param encoded
     * @param start
     * @param end
     * @param appendable
     * @throws IOException
     */
    public static void decode(String encoded, int start, int end, Appendable appendable)
            throws IOException {
        DefaultStringDecoder codec = new DefaultStringDecoder();
        codec.start(DEFAULT_QUOTE, appendable);
        for (int pos = start; pos < end; pos++) {
            codec.feed0(encoded.charAt(pos));
        }
        codec.feed(DEFAULT_QUOTE);
        if (!codec.isTerminable()) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Decode source string from start position which must be the start quote, util the end quote.
     *
     * @param source
     * @param start
     * @param appendable
     * @return Position after the end quote.
     * @throws IOException
     */
    public static int decode(String source, int start, Appendable appendable) throws IOException {
        DefaultStringDecoder codec = new DefaultStringDecoder();
        codec.start(source.charAt(start), appendable);
        int pos = start + 1;
        while (pos < source.length()) {
            boolean accepted = codec.feed0(source.charAt(pos));
            pos++;
            if (!accepted) {
                break;
            }
        }
        if (!codec.isTerminable()) {
            throw new IllegalArgumentException();
        }
        return pos;
    }

    /**
     * Encode the specified string.
     *
     * @param src
     * @return The encode result without quotes.
     */
    public static String encode(String src) {
        return encode(src, 0, src.length());
    }

    /**
     * Encode the specified region of the string.
     *
     * @param src
     * @param start
     * @param end
     * @return The encode result without quotes
     */
    public static String encode(String src, int start, int end) {
        StringBuilder sb = new StringBuilder();
        try {
            encode(src, start, end, sb);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return sb.toString();
    }

    /**
     * Encode the specified region of the string and append the result without quotes.
     *
     * @param src
     * @param start
     * @param end
     * @param sb
     * @return length of encoded result string.
     * @throws IOException
     */
    public static int encode(String src, int start, int end, Appendable sb) throws IOException {
        int len = 0;
        for (int pos = start; pos < end; pos++) {
            char ch = src.charAt(pos);
            switch (ch) {
                case '\b':
                    sb.append("\\b");
                    len += 2;
                    break;
                case '\t':
                    sb.append("\\t");
                    len += 2;
                    break;
                case '\n':
                    sb.append("\\n");
                    len += 2;
                    break;
                case '\f':
                    sb.append("\\f");
                    len += 2;
                    break;
                case '\r':
                    sb.append("\\r");
                    len += 2;
                    break;
                case '\"':
                    sb.append("\\\"");
                    len += 2;
                    break;
                case '\'':
                    sb.append("\\'");
                    len += 2;
                    break;
                case '\\':
                    sb.append("\\\\");
                    len += 2;
                    break;
                default:
                    sb.append(ch);
                    len += 1;
            }
        }
        return len;
    }
}
