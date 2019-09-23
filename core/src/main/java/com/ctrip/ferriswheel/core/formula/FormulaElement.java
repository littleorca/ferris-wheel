package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.quarks.Token;

public class FormulaElement {
    private Token token;
    private String tokenString;
    private int slices = 1;

    public FormulaElement() {
    }

    public FormulaElement(Token token, String tokenString) {
        this.token = token;
        this.tokenString = tokenString;
    }

    public FormulaElement(Token token, String tokenString, int slices) {
        this.token = token;
        this.tokenString = tokenString;
        this.slices = slices;
    }

    public void evaluate(FormulaEvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    public boolean isVolatile() {
        return false;
    }

    @Override
    public String toString() {
        return "[" + getTokenString() + "," + slices + ']';
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getTokenString() {
        return tokenString != null ? tokenString : token != null ? token.getString() : null;
    }

    public void setTokenString(String tokenString) {
        this.tokenString = tokenString;
    }

    public int getSlices() {
        return slices;
    }

    public void setSlices(int slices) {
        this.slices = slices;
    }

}
