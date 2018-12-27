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

import com.ctrip.ferriswheel.api.variant.Variant;
import com.ctrip.ferriswheel.api.variant.VariantRule;
import com.ctrip.ferriswheel.api.variant.VariantType;

import java.util.LinkedHashSet;
import java.util.Set;

public class ValueRule implements VariantRule {
    private VariantType type;
    private boolean nullable;
    private Set<Variant> allowedValues;

    public ValueRule() {
    }

    /**
     * construct an instance that copies the specified rule.
     */
    public ValueRule(VariantRule rule) {
        this(rule.getType(),
                rule.isNullable(),
                (rule.getAllowedValues() == null) ?
                        null : new LinkedHashSet<>(rule.getAllowedValues()));
    }

    public ValueRule(VariantType type, boolean nullable, Set<Variant> allowedValues) {
        this.type = type;
        this.nullable = nullable;
        this.allowedValues = allowedValues;
    }

    public ValueRule type(VariantType type) {
        setType(type);
        return this;
    }

    public ValueRule nullable(boolean nullable) {
        setNullable(nullable);
        return this;
    }

    public ValueRule allowValue(Variant var) {
        if (allowedValues == null) {
            allowedValues = new LinkedHashSet<>();
        }
        allowedValues.add(var);
        return this;
    }

    @Override
    public VariantType getType() {
        return type;
    }

    @Override
    public void setType(VariantType type) {
        this.type = type;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public Set<Variant> getAllowedValues() {
        return allowedValues;
    }

    @Override
    public void setAllowedValues(Set<Variant> allowedValues) {
        this.allowedValues = allowedValues;
    }

    @Override
    public boolean check(Variant var) {
        if (var == null || var.isBlank()) {
            return isNullable();
        }
        if (type != null && type != var.valueType()) {
            return false;
        }
        if (allowedValues != null && !allowedValues.isEmpty()
                && !allowedValues.contains(var)) {
            return false;
        }
        return true;
    }
}
