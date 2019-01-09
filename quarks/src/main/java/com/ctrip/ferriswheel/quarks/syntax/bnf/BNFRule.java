package com.ctrip.ferriswheel.quarks.syntax.bnf;

import com.ctrip.ferriswheel.quarks.Symbol;

import java.io.Serializable;
import java.util.List;

public class BNFRule implements Serializable {
    private static final long serialVersionUID = 1L;

    Symbol symbol;
    List<List<Symbol>> choices;

    public Symbol getSymbol() {
        return symbol;
    }

    public List<List<Symbol>> getChoices() {
        return choices;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(symbol.toString());
        sb.append(" ::= \r\n");

        for (List<Symbol> symbols : choices) {
            sb.append(" - ");
            for (Symbol symbol : symbols) {
                sb.append(" ").append(symbol.toString());
            }
            sb.append("\r\n");
        }

        return sb.toString();
    }
}
