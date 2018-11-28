package com.ctrip.ferriswheel.core.intf;

import java.io.IOException;

public interface DataProvider {
    /**
     * Determine if the provider can handle the specified query or not.
     *
     * @param query
     * @return
     */
    boolean acceptsQuery(DataQuery query);

    /**
     * Execute query.
     *
     * @param query
     * @return
     */
    DataSet execute(DataQuery query) throws IOException;
}
