package com.ctrip.ferriswheel.core.bean;

import java.io.Serializable;

public class PivotField implements Serializable {
    private String field;

    public PivotField() {
    }

    public PivotField(PivotField another) {
        this(another.field);
    }

    public PivotField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
