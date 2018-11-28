package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.formula.ErrorCode;
import com.ctrip.ferriswheel.core.formula.Formula;
import com.ctrip.ferriswheel.core.intf.DynamicVariant;
import com.ctrip.ferriswheel.core.intf.Variant;
import com.ctrip.ferriswheel.core.intf.VariantType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DynamicValue implements DynamicVariant {
    private Formula formula;
    private Variant value;

    public DynamicValue() {
        this(null, Value.BLANK);
    }

    public DynamicValue(String formulaString) {
        this(formulaString, null);
    }

    public DynamicValue(Value value) {
        this(null, value);
    }

    /**
     * Construct a copy of the specified variable.
     *
     * @param variable
     */
    public DynamicValue(DynamicVariant variable) {
        this(
                (variable == null || variable.getFormulaString() == null) ?
                        null : variable.getFormulaString(),

                (variable != null && variable instanceof DynamicValue) ?
                        Value.from(((DynamicValue) variable).getValue()) : Value.from(variable)
        );
    }

    public DynamicValue(String formulaString, Value value) {
        if (formulaString != null) {
            Formula formula = new Formula(formulaString);
            checkFormula(formula);
            this.formula = formula;
        }
        this.value = (value == null) ? Value.BLANK : value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicValue that = (DynamicValue) o;
        return Objects.equals(formula, that.formula) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formula, value);
    }

    @Override
    public VariantType valueType() {
        refreshIfNeeded();
        return value.valueType();
    }

    @Override
    public boolean isValid() {
        return value != null && value.isValid();
    }

    @Override
    public boolean isBlank() {
        refreshIfNeeded();
        return value.isBlank();
    }

    @Override
    public ErrorCode errorValue() {
        refreshIfNeeded();
        return value.errorValue();
    }

    @Override
    public int intValue() {
        refreshIfNeeded();
        return value.intValue();
    }

    @Override
    public long longValue() {
        refreshIfNeeded();
        return value.longValue();
    }

    @Override
    public float floatValue() {
        refreshIfNeeded();
        return value.floatValue();
    }

    @Override
    public double doubleValue() {
        refreshIfNeeded();
        return value.doubleValue();
    }

    @Override
    public BigDecimal decimalValue() {
        refreshIfNeeded();
        return value.decimalValue();
    }

    @Override
    public boolean booleanValue() {
        refreshIfNeeded();
        return value.booleanValue();
    }

    @Override
    public Date dateValue() {
        refreshIfNeeded();
        return value.dateValue();
    }

    @Override
    public String strValue() {
        refreshIfNeeded();
        return value.strValue();
    }

    @Override
    public List<Variant> listValue() {
        refreshIfNeeded();
        return value.listValue();
    }

    @Override
    public int itemCount() {
        refreshIfNeeded();
        return value.itemCount();
    }

    @Override
    public Variant item(int i) {
        refreshIfNeeded();
        return value.item(i);
    }

    @Override
    public int columnCount() {
        refreshIfNeeded();
        return value.columnCount();
    }

    @Override
    public int rowCount() {
        refreshIfNeeded();
        return value.rowCount();
    }

    @Override
    public boolean isFormula() {
        return formula != null;
    }

    @Override
    public String getFormulaString() {
        return formula == null ? null : formula.getString();
    }

    /**
     * Override this method to check formula.
     */
    protected void checkFormula(Formula formula) {
    }

    /**
     * Override this method to make sure the formula is evaluated.
     */
    protected void refreshIfNeeded() {
    }

    @Override
    public int compareTo(Variant o) {
        refreshIfNeeded();
        return value.compareTo(o);
    }


    public Formula getFormula() {
        return formula;
    }

    public void setFormula(Formula formula) {
        this.formula = formula;
    }

    public Variant getValue() {
        return value;
    }

    public void setValue(Variant value) {
        this.value = (value == null) ? Value.BLANK : value;
    }
}
