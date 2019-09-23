package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;
import com.ctrip.ferriswheel.quarks.Token;

public class FuncElement extends FormulaElement {
    private final Function function;
    private int argc;

    public FuncElement(Token token, String name, int argc, int slices) {
        super(token, name, slices);
        function = FormulaParser.getFunction(name);
        setArgc(argc);
    }

    public Function getFunction() {
        return function;
    }

    public int getArgc() {
        return argc;
    }

    public void setArgc(int argc) {
        if (!function.checkArgc(argc)) {
            throw new IllegalArgumentException("Argc " + argc + " not accepted by function \""
                    + function.getName().toUpperCase() + "\"");
        }
        this.argc = argc;
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        function.evaluate(this, context);
    }

    @Override
    public boolean isVolatile() {
        return function.isVolatile();
    }
}
