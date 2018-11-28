package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.intf.Variant;
import com.ctrip.ferriswheel.core.intf.VariantRule;
import com.ctrip.ferriswheel.core.intf.VariantType;

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
