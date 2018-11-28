package com.ctrip.ferriswheel.quarks.exception;


public class QuarksEvaluationException extends QuarksException {

    private static final long serialVersionUID = 1L;

    public static final int ERR_CODE_IDENTIFIER_NOT_FOUND = 0;
    
    private boolean throwDirectly;
    
    private int errorCode = -1;

    public QuarksEvaluationException() {
        super();
    }

    public QuarksEvaluationException(String message) {
        super(message);
    }

    public QuarksEvaluationException(Throwable clause) {
        super(clause);
    }

    public QuarksEvaluationException(String message, Throwable clause) {
        super(message, clause);
    }

    public boolean isThrowDirectly() {
        return throwDirectly;
    }

    public void setThrowDirectly(boolean throwDirectly) {
        this.throwDirectly = throwDirectly;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    
}
