package com.ctrip.ferriswheel.core.intf;

import com.ctrip.ferriswheel.core.formula.ErrorCode;
import com.ctrip.ferriswheel.core.formula.ErrorCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface Variant extends Comparable<Variant>, Serializable {
    /**
     * Get value type.
     *
     * @return
     */
    VariantType valueType();

    /**
     * Get boolean value which indicates if the value is valid.
     * Invalid value means there are something wrong,
     * e.g.: an invalid formula generates invalid value.
     *
     * @return
     */
    boolean isValid();

    boolean isBlank();

    ErrorCode errorValue();

    int intValue();

    long longValue();

    float floatValue();

    double doubleValue();

    BigDecimal decimalValue();

    boolean booleanValue();

    Date dateValue();

    String strValue();

    List<Variant> listValue();

    int itemCount();

    Variant item(int i);

    /**
     * Column count for list value of 2D array.
     *
     * @return
     */
    int columnCount();

    /**
     * Row count for list value of 2D array.
     *
     * @return
     */
    int rowCount();
}
