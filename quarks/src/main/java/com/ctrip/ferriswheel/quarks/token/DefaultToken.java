package com.ctrip.ferriswheel.quarks.token;

import com.ctrip.ferriswheel.quarks.Token;

import java.io.Serializable;

/**
 * Default token implementation.
 *
 * @author liuhaifeng
 */
public class DefaultToken implements Token, Serializable {
    private static final long serialVersionUID = 1L;

    protected String src;
    protected int from;
    protected int to;
    protected int line;
    protected Type type;
    protected String token;
    //protected int intToken;
    protected transient int hash;

    protected DefaultToken() {
    }

    public DefaultToken(String src, int from, int to, int line, Type type,
                        String token) {
        this.src = src;
        this.from = from;
        this.to = to;
        this.line = line;
        this.type = type;
        this.token = token;
    }

    @Override
    public String getSource() {
        return src;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getFrom() {
        return from;
    }

    @Override
    public int getTo() {
        return to;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String getString() {
        if (token == null && to > from) {
            token = src.substring(from, to);
        }
        return token;
    }

    /*@Override
    public int getID() {
        return intToken;
    }*/

    @Override
    public int length() {
        return to - from;
    }

    @Override
    public boolean equalsTo(String s) {
        return equalsTo(s, 0, s.length());
    }

    @Override
    public boolean equalsTo(String s, int from, int to) {
        if (length() != to - from)
            return false;

        return src.regionMatches(this.from, s, from, to - from);
    }

    @Override
    public boolean equalsToIgnoreCase(String s) {
        return equalsToIgnoreCase(s, 0, s.length());
    }

    @Override
    public boolean equalsToIgnoreCase(String s, int from, int to) {
        if (length() != to - from)
            return false;

        return src.regionMatches(true, this.from, s, from, to - from);
    }

    @Override
    public boolean startsWith(String s) {
        if (length() < s.length()) {
            return false;
        }
        return src.regionMatches(false, this.from, s, 0, s.length());
    }

    @Override
    public boolean startsWithIgnoreCase(String s) {
        if (length() < s.length()) {
            return false;
        }
        return src.regionMatches(true, this.from, s, 0, s.length());
    }

    @Override
    public String toString() {
        return getString();
    }

    @Override
    public int hashCode() {
        if (hash == 0 && to > from) {
            for (int i = from; i < to; i++) {
                hash = hash * 31 + src.charAt(i);
            }
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || to <= from)
            return false;

        if (obj == this)
            return true;

        if (obj instanceof Token) {
            Token t = (Token) obj;
            return src.equals(t.getSource()) && from == t.getFrom()
                    && to == t.getTo() && type == t.getType();
        }
        return false;
    }

    public String debugInfo() {
        if (src == null || to == from) {
            return "<null>";
        }

        StringBuilder sb = new StringBuilder();
        switch (getType()) {
            case Identifier:
                sb.append("ID");
                break;
            case QuotedIdentifier:
                sb.append("QI");
                break;
            case Keyword:
                sb.append("KW");
                break;
            case Literal:
                sb.append("LI");
                break;
            case String:
                sb.append("ST");
                break;
            case Number:
                sb.append("NU");
                break;
            case Operator:
                sb.append("OP");
                break;
            case Delimiter:
                sb.append("DL");
                break;
        }

        sb.append(" \"").append(getString()).append("\" ").append(from)
                .append("-").append(to);
        return sb.toString();
    }

}
