package com.ctrip.ferriswheel.core.formula.func;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.api.variant.Variant;
import com.ctrip.ferriswheel.api.variant.VariantType;
import com.ctrip.ferriswheel.core.util.VariantMath;

import java.math.BigDecimal;
import java.util.List;

public class Average implements Function {
    public static final String AVG = "AVERAGE";

    @Override
    public String getName() {
        return AVG;
    }

    @Override
    public boolean checkArgc(int argc) {
        return argc > 0;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        Variant result = Value.dec(BigDecimal.ZERO);
        int count = 0;

        for (int i = 0;
             i < element.getArgc() && result.valueType() != VariantType.ERROR;
             i++) {

            Variant elem = context.popOperand();
            if (elem instanceof Value.ListValue) {
                List<Variant> valueList = elem.listValue();
                for (Variant val : valueList) {
                    result = VariantMath.add(result, val);
                    if (result.valueType() == VariantType.ERROR) {
                        break;
                    }
                    count++;
                }

            } else {
                result = VariantMath.add(result, elem);
                count++;
            }
        }

        if (result.valueType() != VariantType.ERROR) {
            result = VariantMath.divide(result, Value.dec(count));
        }

        context.pushOperand(result);
    }

}
