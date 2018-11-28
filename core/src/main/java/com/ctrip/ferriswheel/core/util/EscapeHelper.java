package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.quarks.token.DefaultStringCodec;

import java.io.IOException;
import java.util.regex.Pattern;

public class EscapeHelper {
//    public static final String RE_SIMPLE_NAME = "^[a-zA-Z0-9\\_$\\u4E00-\\u9FA5]+$";
    public static final String RE_SIMPLE_NAME = "^[a-zA-Z0-9\\_$]+$";

    public static String escape(String str) {
        Pattern p = Pattern.compile(RE_SIMPLE_NAME);
        if (p.matcher(str).matches()) {
            return str;
        }
        StringBuilder sb = new StringBuilder("\"");
        try {
            DefaultStringCodec.encode(str, 0, str.length(), sb);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        sb.append('"');
        return sb.toString();
    }

    public static String unescape(String str) {
        if (!str.startsWith("\"")) {
            return str;
        }
        if (str.length() < 2 || !str.endsWith("\"")) {
            throw new IllegalArgumentException();
        }
        return DefaultStringCodec.decode(str, 1, str.length() - 1);
    }

    public static int unescape(String str, int pos, Appendable appendable)
            throws IOException {
        if (str.charAt(pos) != '"') {
            throw new IllegalArgumentException();
        }
        return DefaultStringCodec.decode(str, pos, appendable);
    }
}
