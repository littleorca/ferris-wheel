package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.util.VariantMath;
import com.ctrip.ferriswheel.quarks.Token;

public abstract class UnaryElement extends FormulaElement {

    public UnaryElement(Token token, String tokenString) {
        super(token, tokenString);
    }

    public UnaryElement(Token token, String tokenString, int slices) {
        super(token, tokenString, slices);
    }

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        Variant operand = context.popOperand();
        context.pushOperand(evaluate(operand));
    }

    protected abstract Variant evaluate(Variant operand);

    /**
     * +d (=d, meaningless)
     */
    public static class Positive extends UnaryElement {

        public Positive(Token token, String tokenString) {
            super(token, tokenString);
        }

        public Positive(Token token, String tokenString, int slices) {
            super(token, tokenString, slices);
        }

        @Override
        protected Variant evaluate(Variant operand) {
            if (operand.valueType() == VariantType.LIST) {
                return Value.err(ErrorCodes.VALUE);
            } else {
                return operand;
            }
        }
    }

    public static class Negative extends UnaryElement {

        public Negative(Token token, String tokenString) {
            super(token, tokenString);
        }

        public Negative(Token token, String tokenString, int slices) {
            super(token, tokenString, slices);
        }

        @Override
        protected Variant evaluate(Variant operand) {
            VariantType type = VariantType.compatible(VariantType.DECIMAL, operand.valueType());
            if (type == VariantType.DECIMAL) {
                return Value.dec(operand.decimalValue().negate());
            } else {
                return Value.err(ErrorCodes.VALUE);
            }
        }
    }

    public static class Percent extends UnaryElement {

        public Percent(Token token, String tokenString) {
            super(token, tokenString);
        }

        public Percent(Token token, String tokenString, int slices) {
            super(token, tokenString, slices);
        }

        @Override
        protected Variant evaluate(Variant operand) {
            VariantType type = VariantType.compatible(VariantType.DECIMAL, operand.valueType());
            if (type == VariantType.DECIMAL) {
                return VariantMath.divide(operand, Value.dec(100));
            } else {
                return Value.err(ErrorCodes.VALUE);
            }
        }
    }
}
