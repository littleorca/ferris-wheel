package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.quarks.Token;

public class ParamsElement extends FormulaElement {
    private int count;

    public ParamsElement(Token token, String tokenString, int count, int slices) {
        super(token, tokenString, slices);
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increaseCount() {
        this.count++;
    }
}
