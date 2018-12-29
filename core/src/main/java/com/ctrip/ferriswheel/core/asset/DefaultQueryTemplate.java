package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.api.variant.DynamicVariant;
import com.ctrip.ferriswheel.api.query.DataQuery;
import com.ctrip.ferriswheel.api.query.QueryTemplate;
import com.ctrip.ferriswheel.api.variant.Variant;
import com.ctrip.ferriswheel.api.variant.VariantRule;
import com.ctrip.ferriswheel.core.bean.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DefaultQueryTemplate extends AssetNode implements QueryTemplate {
    private String scheme;
    private LinkedHashMap<String, ValueNode> builtinParams = new LinkedHashMap<>();
    private LinkedHashMap<String, VariantRule> userParamRules = new LinkedHashMap<>();

    DefaultQueryTemplate(AssetManager assetManager, QueryTemplate templateInfo) {
        super(assetManager);
        this.scheme = templateInfo.getScheme();
        if (templateInfo.getAllBuiltinParams() != null) {
            templateInfo.getAllBuiltinParams().forEach((name, variable) -> {
                ValueNode param = new ValueNode(getAssetManager(), variable);
                setBuiltinParam(name, param);
            });
        }
        if (templateInfo.getAllUserParamRules() != null) {
            templateInfo.getAllUserParamRules().forEach((name, rule) ->
                    userParamRules.put(name, new ValueRule(rule)));
        }
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public ValueNode getBuiltinParam(String name) {
        return builtinParams.get(name);
    }

    ValueNode setBuiltinParam(String name, ValueNode value) {
        bindChild(value);
        ValueNode old = builtinParams.put(name, value);
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
    public Map<String, DynamicVariant> getAllBuiltinParams() {
        return Collections.unmodifiableMap(builtinParams);
    }

    @Override
    public VariantRule getUserParamRule(String name) {
        return userParamRules.get(name);
    }

    VariantRule setUserParamRule(String name, VariantRule rule) {
        return userParamRules.put(name, rule);
    }

    @Override
    public Set<String> getUserParamNames() {
        return Collections.unmodifiableSet(userParamRules.keySet());
    }

    @Override
    public Map<String, VariantRule> getAllUserParamRules() {
        return Collections.unmodifiableMap(userParamRules);
    }

    public DataQuery renderQuery(Map<String, Variant> userParams) {
        if (scheme == null) {
            throw new IllegalStateException("Query scheme not set.");
        }
        if (userParams == null) {
            userParams = Collections.emptyMap(); // Avoid NPE
        }
        checkUserParameters(userParams);
        userParams = filterBlankValues(userParams);

        DefaultDataQuery query = new DefaultDataQuery();
        query.setScheme(scheme);
        for (Map.Entry<String, ValueNode> entry : builtinParams.entrySet()) {
            if (userParams.containsKey(entry.getKey())) {
                continue; // skip builtin parameter as user overrode it.
            }
            query.setParameter(entry.getKey(), filterVariantClass(entry.getValue()));
        }
        for (Map.Entry<String, Variant> entry : userParams.entrySet()) {
            query.setParameter(entry.getKey(), filterVariantClass(entry.getValue()));
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

    private void checkUserParameters(Map<String, Variant> userParameters) {
        for (Map.Entry<String, Variant> entry : userParameters.entrySet()) {
            VariantRule rule = userParamRules.get(entry.getKey());
            if (rule == null) {
                throw new IllegalArgumentException("User parameter \""
                        + entry.getKey() + "\" is not allowed.");
            }
            if (!rule.check(entry.getValue())) {
                throw new IllegalArgumentException("User parameter \""
                        + entry.getKey() + "\" violates the rule.");
            }
        }

        // FIXME check mandatory params
//        for (Map.Entry<String, VariantRule> entry : userParamRules.entrySet()) {
//            if (!entry.getValue().isNullable() && userParameters.get(entry.getKey()) == null) {
//                throw new IllegalArgumentException("User parameter \""
//                        + entry.getKey() + "\" cannot be null.");
//            }
//        }
    }

    Variant filterVariantClass(Variant var) {
        if (var instanceof Value) {
            return var;
        }
        return Value.from(var); // unknown implementation, maybe throw exception?
    }

    public TableAutomatonInfo.QueryTemplateInfo getQueryTemplateInfo() {
        Map<String, DynamicVariant> params = new LinkedHashMap<>(builtinParams.size());
        builtinParams.forEach((name, param) -> params.put(name, new DynamicVariantImpl(param)));
        Map<String, VariantRule> rules = new LinkedHashMap<>(userParamRules.size());
        userParamRules.forEach((name, rule) -> rules.put(name, new ValueRule(rule)));
        return new TableAutomatonInfo.QueryTemplateInfo(scheme, params, rules);
    }

    @Override
    protected void afterChildUpdate(AssetNode child) {
        setLastUpdateSequenceNumber(child.getLastUpdateSequenceNumber());
    }
}
