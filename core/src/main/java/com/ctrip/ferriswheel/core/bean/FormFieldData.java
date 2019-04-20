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

package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.common.form.FormField;
import com.ctrip.ferriswheel.common.form.FormFieldBinding;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;

import java.io.Serializable;
import java.util.List;

public class FormFieldData implements FormField, Serializable {
    private String name;
    private VariantType type;
    private Variant value;
    private boolean mandatory;
    private boolean multiple;
    private String label;
    private String tips;
    private Variant options;
    private List<FormFieldBinding> bindings;

    public FormFieldData() {
    }

    public FormFieldData(String name,
                         VariantType type,
                         Variant value,
                         boolean mandatory,
                         boolean multiple,
                         String label,
                         String tips,
                         Variant options,
                         List<FormFieldBinding> bindings) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.mandatory = mandatory;
        this.multiple = multiple;
        this.label = label;
        this.tips = tips;
        this.options = options;
        this.bindings = bindings;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public VariantType getType() {
        return type;
    }

    public void setType(VariantType type) {
        this.type = type;
    }

    @Override
    public Variant getValue() {
        return value;
    }

    public void setValue(Variant value) {
        this.value = value;
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

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    @Override
    public Variant getOptions() {
        return options;
    }

    public void setOptions(Variant options) {
        this.options = options;
    }

    @Override
    public int getBindingCount() {
        return bindings == null ? 0 : bindings.size();
    }

    @Override
    public FormFieldBinding getBinding(int index) {
        return bindings == null ? null : bindings.get(index);
    }

    public List<FormFieldBinding> getBindings() {
        return bindings;
    }

    public void setBindings(List<FormFieldBinding> bindings) {
        this.bindings = bindings;
    }

}
