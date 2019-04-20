package com.ctrip.ferriswheel.core.util;

import java.io.IOException;
import java.util.regex.Pattern;

public class EscapeHelper {
    //    public static final String RE_SIMPLE_NAME = "^[a-zA-Z0-9\\_$\\u4E00-\\u9FA5]+$";
    private static final String RE_SIMPLE_NAME = "^[a-zA-Z0-9\\_$]+$";
    private static final char CHAR_SINGLE_QUOTE = '\'';
    private static final String STR_SINGLE_QUOTE = String.valueOf(CHAR_SINGLE_QUOTE);

    public static String escapeNameIfNeeded(String str) {
        Pattern p = Pattern.compile(RE_SIMPLE_NAME);
        if (p.matcher(str).matches()) {
            return str;
        }
        return escapeName(str);
    }

    public static String escapeName(String str) {
        StringBuilder sb = new StringBuilder(STR_SINGLE_QUOTE);
        int offset = 0, pos;
        while ((pos = str.indexOf(CHAR_SINGLE_QUOTE, offset)) != -1) {
            sb.append(str, offset, pos).append(CHAR_SINGLE_QUOTE).append(CHAR_SINGLE_QUOTE);
            offset = pos + 1;
        }
        if (offset < str.length()) {
            sb.append(str, offset, str.length());
        }
        sb.append(CHAR_SINGLE_QUOTE);
        return sb.toString();
    }

    public static String unescapeNameIfNeeded(String str) {
        if (!str.startsWith(STR_SINGLE_QUOTE)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        int pos;
        try {
            pos = unescapeName(str, 0, sb);
        } catch (IOException e) {
            // string builder should not encounter IOException.
            throw new RuntimeException(e);
        }
        if (pos != str.length()) {
            throw new IllegalArgumentException();
        }
        return sb.toString();
    }

    public static int unescapeName(String str, int pos, Appendable appendable) throws IOException {
        if (!str.startsWith(STR_SINGLE_QUOTE)) {
            throw new IllegalArgumentException();
        }
        int offset = pos + 1;
        while ((pos = str.indexOf(CHAR_SINGLE_QUOTE, offset)) != -1) {
            if (pos > offset) {
                appendable.append(str, offset, pos);
            }
            offset = pos + 1;
            if (offset < str.length() && str.charAt(offset) == CHAR_SINGLE_QUOTE) {
                appendable.append(CHAR_SINGLE_QUOTE);
                offset++;
            } else {
                return offset;
            }
        }
        throw new IllegalArgumentException();
    }
}
