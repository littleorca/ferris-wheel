package com.ctrip.ferriswheel.core.intf;

import java.util.Date;
import java.util.Set;

/**
 * By declares query scheme and holds final query parameters, this class
 * completely represents a query which can be executed by correspond
 * {@link DataProvider}.
 *
 * @see DataProvider
 * @see QueryTemplate
 */
public interface DataQuery {
    /**
     * Scheme is a string identifies a sort of query.
     * Typically a scheme requires a correspond {@link DataProvider}.
     *
     * @return
     */
    String getScheme();

    Variant getParam(String name);

    Set<String> getParamNames();

    Integer getInteger(String name);

    Long getLong(String name);

    Float getFloat(String name);

    Double getDouble(String name);

    Boolean getBoolean(String name);

    Date getDate(String name);

    String getString(String name);
}
