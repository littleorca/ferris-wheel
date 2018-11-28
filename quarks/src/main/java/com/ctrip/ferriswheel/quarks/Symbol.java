package com.ctrip.ferriswheel.quarks;

import java.io.Serializable;

public interface Symbol extends Serializable {

    String getSymbol();
    
    boolean isTerminal();

    boolean isTerminator();
}
