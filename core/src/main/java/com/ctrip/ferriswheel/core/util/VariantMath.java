package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;

import java.math.BigDecimal;

public class VariantMath {

    public static Variant add(Variant op1, Variant op2) {
        VariantType type = Value.getCompatibleTypeForMath(op1.valueType(), op2.valueType());
        switch (type) {
            case BLANK:
            case DECIMAL:
                return Value.dec(op1.decimalValue()
                        .add(op2.decimalValue(), Value.DecimalValue.mathContext));
            case DATE:
                return Value.date(op1.decimalValue()
                        .add(op2.decimalValue(), Value.DecimalValue.mathContext)
                        .doubleValue());
            case ERROR:
            default:
                return Value.err(ErrorCodes.VALUE);
        }
    }

    public static Variant subtract(Variant op1, Variant op2) {
        VariantType type = Value.getCompatibleTypeForMath(op1.valueType(), op2.valueType());
        switch (type) {
            case BLANK:
            case DECIMAL:
                return Value.dec(op1.decimalValue()
                        .subtract(op2.decimalValue(), Value.DecimalValue.mathContext));
            case DATE:
                return Value.date(op1.decimalValue()
                        .subtract(op2.decimalValue(), Value.DecimalValue.mathContext)
                        .doubleValue());
            case ERROR:
            default:
                return Value.err(ErrorCodes.VALUE);
        }
    }


    public static Variant multiply(Variant op1, Variant op2) {
        VariantType type = Value.getCompatibleTypeForMath(op1.valueType(), op2.valueType());
        switch (type) {
            case BLANK:
            case DECIMAL:
                return Value.dec(op1.decimalValue()
                        .multiply(op2.decimalValue(), Value.DecimalValue.mathContext));
            case DATE:
                return Value.date(op1.decimalValue()
                        .multiply(op2.decimalValue(), Value.DecimalValue.mathContext)
                        .doubleValue());
            case ERROR:
            default:
                return Value.err(ErrorCodes.VALUE);
        }
    }


    public static Variant divide(Variant op1, Variant op2) {
        VariantType type = Value.getCompatibleTypeForMath(op1.valueType(), op2.valueType());
        switch (type) {
            case BLANK:
            case DECIMAL:
                if (BigDecimal.ZERO.equals(op2.decimalValue())) {
                    return Value.err(ErrorCodes.DIV);
                } else {
                    return Value.dec(op1.decimalValue()
                            .divide(op2.decimalValue(), Value.DecimalValue.mathContext));
                }
            case DATE:
                if (BigDecimal.ZERO.equals(op2.decimalValue())) {
                    return Value.err(ErrorCodes.DIV);
                } else {
                    return Value.date(op1.decimalValue()
                            .divide(op2.decimalValue(), Value.DecimalValue.mathContext)
                            .doubleValue());
                }
            case ERROR:
            default:
                return Value.err(ErrorCodes.VALUE);
        }
    }


    public static Variant power(Variant op1, Variant op2) {
        VariantType type = Value.getCompatibleTypeForMath(op1.valueType(), op2.valueType());
        switch (type) {
            case BLANK:
            case DECIMAL:
                Value.DecimalValue dec = (op1 instanceof Value.DecimalValue) ?
                        (Value.DecimalValue) op1 : Value.dec(op1.decimalValue());
                return dec.pow(op2);
            case ERROR:
            default:
                return Value.err(ErrorCodes.VALUE);
        }
    }

}
