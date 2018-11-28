package com.ctrip.ferriswheel.quarks.exception;

import com.ctrip.ferriswheel.quarks.Token;
import com.ctrip.ferriswheel.quarks.Tokenizer;

public class QuarksSyntaxException extends QuarksException {
    private static final long serialVersionUID = 1L;

    private Token token;
    private int line;
    private int column;

    public QuarksSyntaxException() {
        super();
    }

    public QuarksSyntaxException(Tokenizer tokenizer) {
        super();
        this.token = tokenizer.getToken();
        this.line = tokenizer.getLine();
        this.column = tokenizer.getColumn();
    }

    public QuarksSyntaxException(String message) {
        super(message);
    }

    public QuarksSyntaxException(String message, Tokenizer tokenizer) {
        super(message + "@L" + tokenizer.getLine() + "C"
                + tokenizer.getColumn());
        this.token = tokenizer.getToken();
        this.line = tokenizer.getLine();
        this.column = tokenizer.getColumn();
    }

    public QuarksSyntaxException(Throwable clause, Token token) {
        super(clause);
        this.token = token;
    }

    public QuarksSyntaxException(Throwable clause, Tokenizer tokenizer) {
        super(clause);
        this.token = tokenizer.getToken();
        this.line = tokenizer.getLine();
        this.column = tokenizer.getColumn();
    }

    public QuarksSyntaxException(String message, Throwable clause) {
        super(message, clause);
    }

    public QuarksSyntaxException(String message, Throwable clause,
            Tokenizer tokenizer) {
        super(message + "@L" + tokenizer.getLine() + "C"
                + tokenizer.getColumn(), clause);
        this.token = tokenizer.getToken();
        this.line = tokenizer.getLine();
        this.column = tokenizer.getColumn();
    }

    public Token getErrorToken() {
        return token;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

}
