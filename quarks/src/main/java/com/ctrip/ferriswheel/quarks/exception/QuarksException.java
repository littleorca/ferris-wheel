package com.ctrip.ferriswheel.quarks.exception;

public abstract class QuarksException extends Exception {

    private static final long serialVersionUID = 1L;

    public QuarksException() {
        super();
    }

    public QuarksException(String message) {
        super(message);
    }

    public QuarksException(Throwable clause) {
        super(clause);
    }

    public QuarksException(String message, Throwable clause) {
        super(message, clause);
    }

}
