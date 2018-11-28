package com.ctrip.ferriswheel.quarks.syntax.lr;

import com.ctrip.ferriswheel.quarks.SyntaxParser;

public interface LRParser extends SyntaxParser {

    void setParsingTable(ParsingTable table);

}
