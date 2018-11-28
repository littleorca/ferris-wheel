package com.ctrip.ferriswheel.core.formula;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalcChain {
    private List<Long> orderedNodes;
    private Map<Long, Integer> nodeIndices;

    public CalcChain(List<Long> orderedNodes) {
        this.orderedNodes = orderedNodes;
        this.nodeIndices = new HashMap<>(orderedNodes.size());
        for (int i = 0; i < orderedNodes.size(); i++) {
            this.nodeIndices.put(orderedNodes.get(i), i);
        }
    }

    public Long get(int index) {
        return orderedNodes.get(index);
    }

    public int size() {
        return orderedNodes.size();
    }
}
