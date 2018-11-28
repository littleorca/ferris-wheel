package com.ctrip.ferriswheel.core.formula;

public class FormulaParserException extends RuntimeException {
    public FormulaParserException() {
    }

    public FormulaParserException(String message) {
        super(message);
    }

    public FormulaParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormulaParserException(Throwable cause) {
        super(cause);
    }

    public FormulaParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
