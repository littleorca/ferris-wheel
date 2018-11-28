package com.ctrip.ferriswheel.core.intf;

/**
 * TODO review this interface
 */
public interface TableAutomaton extends Asset {

    void init();

    void execute(boolean forceUpdate);

    void destroy();
}
