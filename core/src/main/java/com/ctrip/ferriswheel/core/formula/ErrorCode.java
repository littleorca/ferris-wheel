package com.ctrip.ferriswheel.core.formula;

public enum ErrorCode {
    OK("OK"),
    UNKNOWN("UNKNOWN"),
    ILLEGAL_REF("REF"),
    ILLEGAL_VALUE("VALUE"),
    DIV_0("DIV/0");

    public static ErrorCode valueOf(int code) {
        if (code < 0 || code >= values().length) {
            throw new IndexOutOfBoundsException("Invalid code: " + code);
        }
        return values()[code];
    }

    private final String displayName;

    ErrorCode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public int getCode() {
        return ordinal();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFullName() {
        return String.valueOf("#" + displayName + "!");
    }
}
