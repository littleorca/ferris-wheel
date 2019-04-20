package com.ctrip.ferriswheel.core.formula.func.text;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.formula.func.Function;

public abstract class StringFunctions implements Function {
    /**
     * MID(text, start_num, num_chars)
     */
    public static class Mid extends StringFunctions {
        public static final String MID = "MID";

        @Override
        public String getName() {
            return MID;
        }

        @Override
        public boolean checkArgc(int argc) {
            return argc == 3;
        }

        @Override
        public void evaluate(FuncElement element, FormulaEvaluationContext context) {
            Variant numChars = context.popOperand();
            Variant startNum = context.popOperand();
            Variant text = context.popOperand();
            String str = text.strValue();
            int start = startNum.intValue();
            int num = numChars.intValue();
            if (start < 1 || num < 0) {
                context.pushOperand(Value.err(ErrorCodes.VALUE));
                return;
            }
            if (start > str.length() || num == 0) {
                context.pushOperand(Value.str(""));
                return;
            }

            String result = str.substring(start - 1, Math.min(start - 1 + num, str.length()));
            context.pushOperand(Value.str(result));
        }
    }
}
