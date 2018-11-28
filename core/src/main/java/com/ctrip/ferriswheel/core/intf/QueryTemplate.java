package com.ctrip.ferriswheel.core.intf;

import java.util.Map;
import java.util.Set;

/**
 * Compare to {@link DataQuery}, a query template is not a final query but
 * a mechanism provides flexible way of generating final query.
 *
 * @see DataQuery
 * @see DataProvider
 */
public interface QueryTemplate extends Asset {
    /**
     * Get query scheme.
     *
     * @return
     */
    String getScheme();

    /**
     * Get builtin parameter.
     *
     * @param name
     * @return
     */
    Variant getBuiltinParam(String name);

    /**
     * Get builtin parameter names.
     *
     * @return
     */
    Set<String> getBuiltinParamNames();

    /**
     * Get user parameter rule.
     *
     * @param name
     * @return
     */
    VariantRule getUserParamRule(String name);

    /**
     * Get user parameter names.
     *
     * @return
     */
    Set<String> getUserParamNames();

    /**
     * Render final query which can be executed by {@link DataProvider}.
     *
     * @param userParams
     * @return
     */
    DataQuery renderQuery(Map<String, Variant> userParams);
}
