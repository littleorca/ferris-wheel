package com.ctrip.ferriswheel.quarks.exception;


public class QuarksEvaluationNotFoundException extends QuarksException {

    private static final long serialVersionUID = 1L;

    public QuarksEvaluationNotFoundException() {
        super();
    }

    public QuarksEvaluationNotFoundException(String message) {
        super(message);
    }

    public QuarksEvaluationNotFoundException(Throwable clause) {
        super(clause);
    }

    public QuarksEvaluationNotFoundException(String message, Throwable clause) {
        super(message, clause);
    }

}
