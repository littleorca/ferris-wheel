package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.util.VariantMath;

public abstract class BinaryElement extends FormulaElement {

    @Override
    public void evaluate(FormulaEvaluationContext context) {
        Variant op2 = context.popOperand();
        Variant op1 = context.popOperand();
        if (op1.valueType() == VariantType.ERROR) {
            context.pushOperand(op1);
            return;
        } else if (op2.valueType() == VariantType.ERROR) {
            context.pushOperand(op2);
            return;
        }
        Variant result = evaluate(op1, op2);
        context.pushOperand(result);
    }

    protected abstract Variant evaluate(Variant op1, Variant op2);

    public static class Add extends BinaryElement {
        @Override
        protected Variant evaluate(Variant op1, Variant op2) {
            return VariantMath.add(op1, op2);
        }
    }

    public static class Subtract extends BinaryElement {
        @Override
        protected Variant evaluate(Variant op1, Variant op2) {
            return VariantMath.subtract(op1, op2);
        }
    }

    public static class Multiply extends BinaryElement {
        @Override
        protected Variant evaluate(Variant op1, Variant op2) {
            return VariantMath.multiply(op1, op2);
        }
    }

    public static class Divide extends BinaryElement {
        @Override
        protected Variant evaluate(Variant op1, Variant op2) {
            return VariantMath.divide(op1, op2);
        }
    }

    public static class Power extends BinaryElement {
        @Override
        protected Variant evaluate(Variant op1, Variant op2) {
            return VariantMath.power(op1, op2);
        }
    }

    public static class Concat extends BinaryElement {
        @Override
        protected Variant evaluate(Variant op1, Variant op2) {
            return Value.str(op1.strValue() + op2.strValue());
        }
    }

    public static abstract class Compare extends BinaryElement {

        @Override
        protected Variant evaluate(Variant op1, Variant op2) {
            try {
                return Value.bool(explain(op1.compareTo(op2)));
            } catch (Exception e) {
                return explainError(e);
            }
        }

        protected abstract boolean explain(int ret);

        protected Variant explainError(Exception e) {
            return Value.err(ErrorCodes.VALUE);
        }
    }

    public static class Equal extends Compare {
        @Override
        protected boolean explain(int ret) {
            return ret == 0;
        }

        @Override
        protected Variant explainError(Exception e) {
            if (e instanceof UnsupportedOperationException) {
                return Value.bool(false);
            }
            return super.explainError(e);
        }
    }

    public static class LessThan extends Compare {
        @Override
        protected boolean explain(int ret) {
            return ret < 0;
        }
    }

    public static class GreaterThan extends Compare {
        @Override
        protected boolean explain(int ret) {
            return ret > 0;
        }
    }

    public static class LessThanOrEqual extends Compare {
        @Override
        protected boolean explain(int ret) {
            return ret <= 0;
        }
    }

    public static class GreaterThanOrEqual extends Compare {
        @Override
        protected boolean explain(int ret) {
            return ret >= 0;
        }
    }

    public static class NotEqual extends Compare {
        @Override
        protected boolean explain(int ret) {
            return ret != 0;
        }

        @Override
        protected Variant explainError(Exception e) {
            if (e instanceof UnsupportedOperationException) {
                return Value.bool(true);
            }
            return super.explainError(e);
        }
    }
}
