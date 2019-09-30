/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.common.automaton.*;
import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.query.QueryTemplate;
import com.ctrip.ferriswheel.common.table.AutomateConfiguration;
import com.ctrip.ferriswheel.common.variant.*;
import com.ctrip.ferriswheel.core.asset.DefaultPivotAutomaton;
import com.ctrip.ferriswheel.core.asset.DefaultQueryAutomaton;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

// TODO review class names.
public abstract class TableAutomatonInfo implements AutomateConfiguration, Serializable {

    /**
     * Forbid user from extending this class.
     */
    private TableAutomatonInfo() {
    }

    public static AutomateConfiguration fromAutomaton(Automaton automaton) {
        if (automaton instanceof DefaultQueryAutomaton) {
            return ((DefaultQueryAutomaton) automaton).getQueryAutomatonInfo();
        } else if (automaton instanceof DefaultPivotAutomaton) {
            return ((DefaultPivotAutomaton) automaton).getPivotAutomatonInfo();
        } else {
            throw new RuntimeException("Unsupported automaton type.");
        }
    }

    public static class QueryAutomatonInfo extends TableAutomatonInfo implements QueryConfiguration {
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

    public static class QueryTemplateInfo implements QueryTemplate, Serializable {
        private String scheme;
        private Map<String, Parameter> builtinParams;

        public QueryTemplateInfo() {
        }

        public QueryTemplateInfo(String scheme,
                                 Map<String, Parameter> builtinParams) {
            this.scheme = scheme;
            this.builtinParams = builtinParams;
        }

        public QueryTemplateInfo addBuiltinParam(String name, Value value) {
            return addBuiltinParam(name, new DefaultParameter(name, value.dynamic(), value.valueType(), false, false));
        }

        public QueryTemplateInfo addBuiltinParam(String name, Parameter parameter) {
            if (builtinParams == null) {
                builtinParams = new LinkedHashMap<>();
            }
            builtinParams.put(name, parameter);
            return this;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        @Override
        public Parameter getBuiltinParam(String name) {
            return builtinParams == null ? null : builtinParams.get(name);
        }

        @Override
        public Set<String> getBuiltinParamNames() {
            return builtinParams == null ? null : builtinParams.keySet();
        }

        public Map<String, Parameter> getAllBuiltinParams() {
            return builtinParams;
        }

        public void setAllBuiltinParams(Map<String, Parameter> builtinParams) {
            this.builtinParams = builtinParams;
        }
    }

    public static class QueryInfo implements DataQuery, Serializable {
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

        @Override
        public Variant getParam(String name) {
            return parameters == null ? null : parameters.get(name);
        }

        @Override
        public Set<String> getParamNames() {
            return parameters == null ? null : parameters.keySet();
        }

        public Map<String, Variant> getAllParams() {
            return parameters;
        }

        public void setAllParams(Map<String, Variant> parameters) {
            this.parameters = parameters;
        }
    }

    public static class PivotAutomatonInfo extends TableAutomatonInfo implements PivotConfiguration {
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
