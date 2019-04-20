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

package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.common.variant.Parameter;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.VariantType;

public class ManagedParameter extends ValueNode implements Parameter {
    private String name;
    private VariantType type;
    private boolean mandatory;
    private boolean multiple;

    public ManagedParameter(AssetManager assetManager,
                            String name,
                            Value value,
                            String formulaString,
                            VariantType type,
                            boolean mandatory,
                            boolean multiple) {
        super(assetManager, value, formulaString);
        this.name = name;
        this.type = type;
        this.mandatory = mandatory;
        this.multiple = multiple;
    }

    public ManagedParameter(AssetManager assetManager,
                            String name,
                            DynamicVariant data,
                            VariantType type,
                            boolean mandatory,
                            boolean multiple) {
        super(assetManager, data);
        this.name = name;
        this.type = type;
        this.mandatory = mandatory;
        this.multiple = multiple;
    }

    public ManagedParameter(AssetManager assetManager,
                            Parameter parameter) {
        this(assetManager,
                parameter.getName(),
                parameter.getValue(),
                parameter.getType(),
                parameter.isMandatory(),
                parameter.isMultiple());
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ValueNode getValue() {
        return this;
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
