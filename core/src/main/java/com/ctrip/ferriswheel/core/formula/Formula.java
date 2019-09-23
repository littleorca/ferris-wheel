package com.ctrip.ferriswheel.core.formula;

public class Formula {
    private String string;
    private FormulaElement[] elements;
    private boolean resolved = false;

    public Formula() {
    }

    public Formula(String string) {
        this(string, FormulaParser.parse(string), true);
    }

    public Formula(String string, FormulaElement[] elements, boolean resolved) {
        this.string = string;
        this.elements = elements;
        this.resolved = resolved;
    }

    public boolean isVolatile() {
        if (elements == null) {
            return false;
        }
        for (FormulaElement elem : elements) {
            if (elem.isVolatile()) {
                return true;
            }
        }
        return false;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public FormulaElement[] getElements() {
        return elements;
    }

    public void setElements(FormulaElement[] elements) {
        this.elements = elements;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}
