package com.ctrip.ferriswheel.core.intf;

import java.util.Map;

public interface QueryAutomaton extends TableAutomaton {

    /**
     * Query with specified user parameters.
     *
     * @param userParameters
     */
    void query(Map<String, Variant> userParameters);

    /**
     * Get query.
     *
     * @return
     */
    DataQuery getQuery();

}
