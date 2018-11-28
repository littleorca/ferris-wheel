package com.ctrip.ferriswheel.core.intf;

import java.util.Set;

public interface VariantRule {
    VariantType getType();

    void setType(VariantType type);

    boolean isNullable();

    void setNullable(boolean nullable);

    Set<Variant> getAllowedValues();

    void setAllowedValues(Set<Variant> allowedValues);

    boolean check(Variant var);
}
