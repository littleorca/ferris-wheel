package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;
import com.ctrip.ferriswheel.quarks.syntax.lr.Closure.Item;

import java.util.List;

public interface ParsingTable {
    enum Action {
        Error, Shift, Reduce, Accept
    }

    interface Instruction {
        Action getAction();

        int getState();

        Item getRule();
    }

    Instruction getInstruction(int state, Symbol input)
            throws QuarksSyntaxException;

    List<Item> getAcceptableRules(int state);

    List<Symbol> getAcceptableSymbols(int state);

}
