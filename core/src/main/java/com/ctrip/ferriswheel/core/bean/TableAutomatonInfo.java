package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.intf.DataQuery;
import com.ctrip.ferriswheel.core.intf.DynamicVariant;
import com.ctrip.ferriswheel.core.intf.Variant;
import com.ctrip.ferriswheel.core.intf.VariantRule;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO review class names.
public abstract class TableAutomatonInfo implements Serializable {

    /**
     * Forbid user from extending this class.
     */
    private TableAutomatonInfo() {
    }

    public static class QueryAutomatonInfo extends TableAutomatonInfo {
        private QueryTemplateInfo template;
        private Map<String, Variant> parameters;
        private QueryInfo query;

        public QueryAutomatonInfo() {
        }

        public QueryAutomatonInfo(QueryTemplateInfo template) {
            this(template, null, null);
        }

        public QueryAutomatonInfo(QueryTemplateInfo template, Map<String, Variant> parameters, QueryInfo query) {
            this.template = template;
            this.parameters = parameters;
            this.query = query;
        }

        public QueryTemplateInfo getTemplate() {
            return template;
        }

        public void setTemplate(QueryTemplateInfo template) {
            this.template = template;
        }

        public Map<String, Variant> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Variant> parameters) {
            this.parameters = parameters;
        }

        public QueryInfo getQuery() {
            return query;
        }

        public void setQuery(QueryInfo query) {
            this.query = query;
        }
    }

    public static class QueryTemplateInfo implements Serializable {
        private String scheme;
        private Map<String, DynamicVariant> builtinParams;
        private Map<String, VariantRule> userParamRules;

        public QueryTemplateInfo() {
        }

        public QueryTemplateInfo(String scheme,
                                 Map<String, DynamicVariant> builtinParams,
                                 Map<String, VariantRule> userParamRules) {
            this.scheme = scheme;
            this.builtinParams = builtinParams;
            this.userParamRules = userParamRules;
        }

        public QueryTemplateInfo addBuiltinParam(String name, Value value) {
            return addBuiltinParam(name, new DynamicValue(value));
        }

        public QueryTemplateInfo addBuiltinParam(String name, DynamicVariant dynamicValue) {
            if (builtinParams == null) {
                builtinParams = new LinkedHashMap<>();
            }
            builtinParams.put(name, dynamicValue);
            return this;
        }

        public QueryTemplateInfo addUserParamRule(String name, VariantRule rule) {
            if (userParamRules == null) {
                userParamRules = new LinkedHashMap<>();
            }
            userParamRules.put(name, rule);
            return this;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public Map<String, DynamicVariant> getBuiltinParams() {
            return builtinParams;
        }

        public void setBuiltinParams(Map<String, DynamicVariant> builtinParams) {
            this.builtinParams = builtinParams;
        }

        public Map<String, VariantRule> getUserParamRules() {
            return userParamRules;
        }

        public void setUserParamRules(Map<String, VariantRule> userParamRules) {
            this.userParamRules = userParamRules;
        }
    }

    public static class QueryInfo implements Serializable {
        private String scheme;
        private Map<String, Variant> parameters;

        public QueryInfo() {
        }

        public QueryInfo(DataQuery query) {
            this.scheme = query.getScheme();
            if (query.getParamNames() != null) {
                this.parameters = new LinkedHashMap<>(query.getParamNames().size());
                for (String name : query.getParamNames()) {
                    this.parameters.put(name, query.getParam(name));
                }
            }
        }

        public QueryInfo(String scheme, Map<String, Variant> parameters) {
            this.scheme = scheme;
            this.parameters = parameters;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public Map<String, Variant> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Variant> parameters) {
            this.parameters = parameters;
        }
    }

    public static class PivotAutomatonInfo extends TableAutomatonInfo {
        private DynamicVariant data;
        private List<PivotFilter> filters;
        private List<PivotField> rows;
        private List<PivotField> columns;
        private List<PivotValue> values;

        public PivotAutomatonInfo() {
        }

        public PivotAutomatonInfo(DynamicVariant data,
                                  List<PivotFilter> filters,
                                  List<PivotField> rows,
                                  List<PivotField> columns,
                                  List<PivotValue> values) {
            this.data = data;
            this.filters = filters;
            this.rows = rows;
            this.columns = columns;
            this.values = values;
        }

        public DynamicVariant getData() {
            return data;
        }

        public void setData(DynamicVariant data) {
            this.data = data;
        }

        public List<PivotFilter> getFilters() {
            return filters;
        }

        public void setFilters(List<PivotFilter> filters) {
            this.filters = filters;
        }

        public List<PivotField> getRows() {
            return rows;
        }

        public void setRows(List<PivotField> rows) {
            this.rows = rows;
        }

        public List<PivotField> getColumns() {
            return columns;
        }

        public void setColumns(List<PivotField> columns) {
            this.columns = columns;
        }

        public List<PivotValue> getValues() {
            return values;
        }

        public void setValues(List<PivotValue> values) {
            this.values = values;
        }
    }
}
