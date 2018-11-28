package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.Token;
import com.ctrip.ferriswheel.quarks.exception.QuarksSyntaxException;

import java.util.List;

public interface LREventListener {
    void onBegin();

    void onShift(Symbol handle, Token token) throws QuarksSyntaxException;

    void onReduce(Symbol handle, List<Symbol> sequence) throws QuarksSyntaxException;

    void onFinish() throws QuarksSyntaxException;
}
