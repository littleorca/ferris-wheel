/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
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
 */

package com.ctrip.ferriswheel.common.variant;

public class DefaultParameter implements Parameter {
    private String name;
    private DynamicVariant value;
    private VariantType type;
    private boolean mandatory;
    private boolean multiple;

    public DefaultParameter() {
    }

    public DefaultParameter(String name, DynamicVariant value) {
        this(name, value, value.valueType(), false, false);
    }

    public DefaultParameter(Parameter another) {
        this(another.getName(),
                another.getValue(),
                another.getType(),
                another.isMandatory(),
                another.isMultiple());
    }

    public DefaultParameter(String name,
                            DynamicVariant value,
                            VariantType type,
                            boolean mandatory,
                            boolean multiple) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.mandatory = mandatory;
        this.multiple = multiple;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DynamicVariant getValue() {
        return value;
    }

    public void setValue(DynamicVariant value) {
        this.value = value;
    }

    @Override
    public VariantType getType() {
        return type;
    }

    public void setType(VariantType type) {
        this.type = type;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }
}
