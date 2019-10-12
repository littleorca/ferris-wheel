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

import com.ctrip.ferriswheel.common.form.FormField;
import com.ctrip.ferriswheel.common.form.FormFieldBinding;
import com.ctrip.ferriswheel.common.variant.*;

import java.util.Collections;
import java.util.List;

public class DefaultFormField extends NamedAssetNode implements FormField {
    private VariantType type;
    private Value value;
    private boolean mandatory;
    private boolean multiple;
    private String label;
    private String tips;
    private final ValueNode options;
    private final AssetList<DefaultFormFieldBinding> bindings;

    DefaultFormField(String name, AssetManager assetManager) {
        super(name, assetManager);
        this.value = Value.BLANK;
        this.options = new ValueNode(assetManager, Value.BLANK, null);
        this.bindings = new AssetList<>(this);

        bindChild(options);
    }

    void fillFieldData(FormField fieldData) {
        setType(fieldData.getType());
        setValue(fieldData.getValue());
        setMandatory(fieldData.isMandatory());
        setMultiple(fieldData.isMultiple());
        setLabel(fieldData.getLabel());
        setTips(fieldData.getTips());
        setOptions(fieldData.getOptions());

        bindings.clear();
        for (int i = 0; i < fieldData.getBindingCount(); i++) {
            bindings.add(i, new DefaultFormFieldBinding(getAssetManager(),
                    fieldData.getBinding(i)));
        }
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

    void setValue(Variant value) {
        this.value = Value.from(value);
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
    public ValueNode getOptions() {
        return options;
    }

    public void setOptions(Variant options) {
        if (options instanceof DynamicVariant) {
            this.options.setDynamicVariant((DynamicVariant) options);
        } else {
            this.options.setDynamicVariant(new DynamicValue(null, Value.from(options)));
        }
    }

    @Override
    public int getBindingCount() {
        return bindings == null ? 0 : bindings.size();
    }

    @Override
    public DefaultFormFieldBinding getBinding(int index) {
        return bindings == null ? null : bindings.get(index);
    }

    public List<FormFieldBinding> getBindings() {
        return Collections.unmodifiableList(bindings);
    }

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        if (getEvaluatedRevision() == 0) {
            if ((value == null || value.isBlank()) && getBindingCount() > 0) {
                // try to initialize field value by the first binding
                DefaultFormFieldBinding binding = getBinding(0);
                if (binding.getData() != null) {
                    value = Value.from(binding.getData());
                }
            }
        }
        return EvaluationState.DONE;
    }
}
