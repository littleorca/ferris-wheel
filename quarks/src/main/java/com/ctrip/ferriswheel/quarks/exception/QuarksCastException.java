package com.ctrip.ferriswheel.quarks.exception;


public class QuarksCastException extends QuarksException {
    
    private static final long serialVersionUID = 1L;

    public QuarksCastException() {
        super();
    }

    public QuarksCastException(String message) {
        super(message);
    }

    public QuarksCastException(Throwable clause) {
        super(clause);
    }

    public QuarksCastException(String message, Throwable clause) {
        super(message, clause);
    }

}
