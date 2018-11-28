package com.ctrip.ferriswheel.quarks.exception;

public class QuarksCompileException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public QuarksCompileException() {
        super();
    }

    public QuarksCompileException(String message) {
        super(message);
    }

    public QuarksCompileException(Throwable clause) {
        super(clause);
    }

    public QuarksCompileException(String message, Throwable clause) {
        super(message, clause);
    }

}
