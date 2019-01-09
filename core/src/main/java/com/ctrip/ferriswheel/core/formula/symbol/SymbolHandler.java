package com.ctrip.ferriswheel.core.formula.symbol;

import com.ctrip.ferriswheel.core.formula.FormulaElement;
import com.ctrip.ferriswheel.quarks.Symbol;

import java.util.List;
import java.util.Stack;

public interface SymbolHandler {
    void reduce(Stack<FormulaElement> stack, Symbol handle, List<Symbol> sequence);
}
