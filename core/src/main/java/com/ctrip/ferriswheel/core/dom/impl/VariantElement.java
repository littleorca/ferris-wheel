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

package com.ctrip.ferriswheel.core.dom.impl;

import com.ctrip.ferriswheel.common.variant.*;
import com.ctrip.ferriswheel.core.dom.helper.TagNames;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VariantElement extends AbstractElement implements DynamicVariant {
    static final String TAG_NAME = TagNames.VARIANT;

    private static final String ATTR_TYPE = "type";
    private static final String ATTR_FORMULA = "formula";
    private static final String ATTR_COLUMN_COUNT = "cols";
    private static final String ATTR_ROW_COUNT = "rows";

    private transient DynamicValue variant;

    @Override
    public String getTagName() {
        return TAG_NAME;
    }

    @Override
    public boolean isFormula() {
        return getVariant().isFormula();
    }

    @Override
    public String getFormulaString() {
        return getVariant().getFormulaString();
    }

    @Override
    public VariantType valueType() {
        return getVariant().valueType();
    }

    @Override
    public boolean isValid() {
        return getVariant().isValid();
    }

    @Override
    public boolean isBlank() {
        return getVariant().isBlank();
    }

    @Override
    public ErrorCode errorValue() {
        return getVariant().errorValue();
    }

    @Override
    public int intValue() {
        return getVariant().intValue();
    }

    @Override
    public long longValue() {
        return getVariant().longValue();
    }

    @Override
    public float floatValue() {
        return getVariant().floatValue();
    }

    @Override
    public double doubleValue() {
        return getVariant().doubleValue();
    }

    @Override
    public BigDecimal decimalValue() {
        return getVariant().decimalValue();
    }

    @Override
    public boolean booleanValue() {
        return getVariant().booleanValue();
    }

    @Override
    public Date dateValue() {
        return getVariant().dateValue();
    }

    @Override
    public String strValue() {
        return getVariant().strValue();
    }

    @Override
    public List<Variant> listValue() {
        return getVariant().listValue();
    }

    @Override
    public int itemCount() {
        return getVariant().itemCount();
    }

    @Override
    public Variant item(int i) {
        return getVariant().item(i);
    }

    @Override
    public int columnCount() {
        return getVariant().itemCount();
    }

    @Override
    public int rowCount() {
        return getVariant().rowCount();
    }

    @Override
    public int compareTo(Variant o) {
        return getVariant().compareTo(o);
    }

    public DynamicValue getVariant() {
        if (variant != null) {
            return variant;
        }

        String formulaString = getAttribute(ATTR_FORMULA);
        String typeAttr = getAttribute(ATTR_TYPE);
        VariantType type = typeAttr == null ? null : VariantType.valueOf(typeAttr);
        if (type == null) {
            return new DynamicValue(formulaString);
        }

        Value value;

        if (type == VariantType.LIST) {
            final List<Variant> list = new ArrayList<>(getChildCount());
            forEachChild(child -> {
                if (child instanceof VariantElement) {
                    list.add(((VariantElement) child).getVariant().getVariant());
                } else {
                    // log error?
                }
            });
            value = Value.list(list);

        } else {
            value = Value.parse(type, getTextContent());
        }

        variant = new DynamicValue(formulaString, value);
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = null;

        if (variant instanceof DynamicVariant && ((DynamicVariant) variant).getFormulaString() != null) {
            setAttribute(ATTR_FORMULA, ((DynamicVariant) variant).getFormulaString());
        } else {
            removeAttribute(ATTR_FORMULA);
        }

        if (variant.valueType() != null) {
            setAttribute(ATTR_TYPE, variant.valueType().name());
        } else {
            removeAttribute(ATTR_TYPE);
        }

        if (variant.valueType() == VariantType.LIST) {
            setAttribute(ATTR_COLUMN_COUNT, String.valueOf(variant.columnCount()));
            setAttribute(ATTR_ROW_COUNT, String.valueOf(variant.rowCount()));
            setTextContent(null);

            for (Variant varItem : variant.listValue()) {
                VariantElement varItemElement = getOwnerDocument().createElement(VariantElement.class);
                varItemElement.setVariant(varItem);
                appendChild(varItemElement);
            }

        } else {
            removeAttribute(ATTR_COLUMN_COUNT);
            removeAttribute(ATTR_ROW_COUNT);
            setTextContent(Value.from(variant).strValue());
        }
    }
}
