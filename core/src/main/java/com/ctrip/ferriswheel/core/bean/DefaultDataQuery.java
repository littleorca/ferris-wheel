package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.intf.DataQuery;
import com.ctrip.ferriswheel.core.intf.Variant;
import com.ctrip.ferriswheel.core.util.VariantProperties;

import java.util.Map;
import java.util.Set;

public class DefaultDataQuery extends VariantProperties implements DataQuery {
    private String scheme;

    public DefaultDataQuery() {
    }

    public DefaultDataQuery(TableAutomatonInfo.QueryInfo queryInfo) {
        this(queryInfo.getScheme(), queryInfo.getParameters());
    }

    public DefaultDataQuery(String scheme, Map<? extends String, ? extends Variant> m) {
        super(m);
        this.scheme = scheme;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public Variant getParam(String name) {
        return get(name);
    }

    @Override
    public Set<String> getParamNames() {
        return keySet();
    }

    public Variant setParameter(String name, Variant value) {
        return put(name, value);
    }

}
