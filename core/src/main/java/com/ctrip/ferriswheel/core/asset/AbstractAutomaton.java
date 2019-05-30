package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.automaton.Automaton;

public abstract class AbstractAutomaton extends AssetNode implements Automaton {

    AbstractAutomaton(AssetManager assetManager) {
        super(assetManager);
    }

    @Override
    public void init() {
        // query(Collections.emptyMap());
    }

    @Override
    public void destroy() {
        // dummy
    }

    DefaultTable getTable() {
        return (DefaultTable) getParent();
    }

}
