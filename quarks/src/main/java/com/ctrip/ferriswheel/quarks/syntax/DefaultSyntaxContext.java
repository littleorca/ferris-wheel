package com.ctrip.ferriswheel.quarks.syntax;

import com.ctrip.ferriswheel.quarks.Symbol;
import com.ctrip.ferriswheel.quarks.SyntaxContext;
import com.ctrip.ferriswheel.quarks.syntax.bnf.BNFSymbol;
import com.ctrip.ferriswheel.quarks.syntax.bnf.BuiltinSymbols;

public class DefaultSyntaxContext implements SyntaxContext {
    private static final long serialVersionUID = 1L;
    private static final DefaultSyntaxContext DEFAULT_INSTANCE = new DefaultSyntaxContext();

    public static final Symbol RootSymbol = BNFSymbol
            .nonTerminalSymbol(BuiltinSymbols.QUARKS);
    public static final Symbol NumberSymbol = BNFSymbol
            .terminalSymbol(BuiltinSymbols.NUMBER);
    public static final Symbol StringSymbol = BNFSymbol
            .terminalSymbol(BuiltinSymbols.STRING);
    public static final Symbol IdentifierSymbol = BNFSymbol
            .terminalSymbol(BuiltinSymbols.IDENTIFIER);
    public static final Symbol QuotedIdentifierSymbol = BNFSymbol
            .terminalSymbol(BuiltinSymbols.QUOTED_IDENTIFIER);
    public static final Symbol TerminatorSymbol = BNFSymbol.terminator();

    public static SyntaxContext getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    @Override
    public Symbol getRootSymbol() {
        return RootSymbol;
    }

    @Override
    public Symbol getNumberSymbol() {
        return NumberSymbol;
    }

    @Override
    public Symbol getStringSymbol() {
        return StringSymbol;
    }

    @Override
    public Symbol getIdentifierSymbol() {
        return IdentifierSymbol;
    }

    @Override
    public Symbol getQuotedIdentifierSymbol() {
        return QuotedIdentifierSymbol;
    }

    @Override
    public Symbol getTerminatorSymbol() {
        return TerminatorSymbol;
    }

}
