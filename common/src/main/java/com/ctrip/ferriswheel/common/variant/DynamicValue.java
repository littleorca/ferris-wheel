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

package com.ctrip.ferriswheel.common.variant;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DynamicValue implements DynamicVariant {
    private String formulaString;
    private Variant variant;

    public DynamicValue() {
        this(null, Value.BLANK);
    }

    public DynamicValue(String formulaString) {
        this(formulaString, null);
    }

    public DynamicValue(Value variant) {
        this(null, variant);
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
                        Value.from(((DynamicValue) variable).getVariant()) : Value.from(variable)
        );
    }

    public DynamicValue(String formulaString, Value variant) {
        this.formulaString = formulaString;
        this.variant = (variant == null) ? Value.BLANK : variant;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynamicValue that = (DynamicValue) o;
        return Objects.equals(formulaString, that.formulaString) &&
                Objects.equals(variant, that.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formulaString, variant);
    }

    @Override
    public VariantType valueType() {
        return variant.valueType();
    }

    @Override
    public boolean isValid() {
        return variant != null && variant.isValid();
    }

    @Override
    public boolean isBlank() {
        return variant.isBlank();
    }

    @Override
    public ErrorCode errorValue() {
        return variant.errorValue();
    }

    @Override
    public int intValue() {
        return variant.intValue();
    }

    @Override
    public long longValue() {
        return variant.longValue();
    }

    @Override
    public float floatValue() {
        return variant.floatValue();
    }

    @Override
    public double doubleValue() {
        return variant.doubleValue();
    }

    @Override
    public BigDecimal decimalValue() {
        return variant.decimalValue();
    }

    @Override
    public boolean booleanValue() {
        return variant.booleanValue();
    }

    @Override
    public Date dateValue() {
        return variant.dateValue();
    }

    @Override
    public String strValue() {
        return variant.strValue();
    }

    @Override
    public List<Variant> listValue() {
        return variant.listValue();
    }

    @Override
    public int itemCount() {
        return variant.itemCount();
    }

    @Override
    public Variant item(int i) {
        return variant.item(i);
    }

    @Override
    public int columnCount() {
        return variant.columnCount();
    }

    @Override
    public int rowCount() {
        return variant.rowCount();
    }

    @Override
    public boolean isFormula() {
        return formulaString != null && !formulaString.isEmpty();
    }

    @Override
    public int compareTo(Variant o) {
        return variant.compareTo(o);
    }

    @Override
    public String getFormulaString() {
        return formulaString;
    }

    public void setFormulaString(String formulaString) {
        this.formulaString = formulaString;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = (variant == null) ? Value.BLANK : variant;
    }
}
