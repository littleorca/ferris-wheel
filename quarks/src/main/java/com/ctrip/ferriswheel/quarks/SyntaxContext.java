package com.ctrip.ferriswheel.quarks;

import java.io.Serializable;

public interface SyntaxContext extends Serializable {

    Symbol getRootSymbol();

    Symbol getNumberSymbol();

    Symbol getStringSymbol();

    Symbol getIdentifierSymbol();

    Symbol getQuotedIdentifierSymbol();

    Symbol getTerminatorSymbol();

}
