package com.ctrip.ferriswheel.core.formula.func;

import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;

public interface Function {
    String getName();

    boolean checkArgc(int argc);

    void evaluate(FuncElement element, FormulaEvaluationContext context);
}
