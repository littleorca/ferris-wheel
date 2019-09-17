package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.query.QueryTemplate;
import com.ctrip.ferriswheel.common.variant.DefaultParameter;
import com.ctrip.ferriswheel.common.variant.Parameter;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.bean.DefaultDataQuery;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DefaultQueryTemplate extends AssetNode implements QueryTemplate {
    private String scheme;
    private LinkedHashMap<String, ManagedParameter> builtinParams = new LinkedHashMap<>();

    DefaultQueryTemplate(AssetManager assetManager, QueryTemplate templateInfo) {
        super(assetManager);
        this.scheme = templateInfo.getScheme();
        if (templateInfo.getAllBuiltinParams() != null) {
            templateInfo.getAllBuiltinParams().forEach((name, param) -> {
                setBuiltinParam(name, param);
            });
        }
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    void setScheme(String scheme) {
        this.scheme = scheme;
        markDirty();
    }

    @Override
    public Parameter getBuiltinParam(String name) {
        return builtinParams.get(name);
    }

    Parameter setBuiltinParam(String name, Parameter param) {
        ManagedParameter managedParam = new ManagedParameter(getAssetManager(), param);
        bindChild(managedParam);
        ManagedParameter old = builtinParams.put(name, managedParam);
        if (old != null) {
            unbindChild(old);
        }
        return old;
    }

    @Override
    public Set<String> getBuiltinParamNames() {
        return Collections.unmodifiableSet(builtinParams.keySet());
    }

    @Override
    public Map<String, Parameter> getAllBuiltinParams() {
        return Collections.unmodifiableMap(builtinParams);
    }

    public DataQuery renderQuery(Map<String, Variant> userParams) {
        if (scheme == null) {
            throw new IllegalStateException("Query scheme not set.");
        }
        if (userParams == null) {
            userParams = Collections.emptyMap(); // Avoid NPE
        }
        userParams = filterBlankValues(userParams);

        DefaultDataQuery query = new DefaultDataQuery();
        query.setScheme(scheme);
        for (Map.Entry<String, ManagedParameter> entry : builtinParams.entrySet()) {
            if (userParams.containsKey(entry.getKey())) {
                query.setParameter(entry.getKey(), filterVariantClass(userParams.get(entry.getKey())));
            } else {
                query.setParameter(entry.getKey(), filterVariantClass(entry.getValue().getValue()));
            }
        }
        return query;
    }

    private Map<String, Variant> filterBlankValues(Map<String, Variant> userParams) {
        if (userParams == null) {
            return null;
        }
        Map<String, Variant> map = new LinkedHashMap<>(userParams.size());
        for (Map.Entry<String, Variant> entry : userParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    Variant filterVariantClass(Variant var) {
        if (var instanceof Value) {
            return var;
        }
        return Value.from(var); // unknown implementation, maybe throw exception?
    }

    public TableAutomatonInfo.QueryTemplateInfo getQueryTemplateInfo() {
        Map<String, Parameter> params = new LinkedHashMap<>(builtinParams.size());
        builtinParams.forEach((name, param) -> params.put(name, new DefaultParameter(param)));
        return new TableAutomatonInfo.QueryTemplateInfo(scheme, params);
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        // TODO TBD
        return EvaluationState.DONE;
    }
}
