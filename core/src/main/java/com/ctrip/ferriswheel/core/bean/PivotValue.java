package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.intf.AggregateType;

import java.io.Serializable;

public class PivotValue implements Serializable {
    private String field;
    private AggregateType aggregateType;
    private String label;

    public PivotValue() {
    }

    public PivotValue(String field, AggregateType aggregateType, String label) {
        this.field = field;
        this.aggregateType = aggregateType;
        this.label = label;
    }

    public PivotValue(PivotValue another) {
        this(another.field, another.aggregateType, another.label);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public AggregateType getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
