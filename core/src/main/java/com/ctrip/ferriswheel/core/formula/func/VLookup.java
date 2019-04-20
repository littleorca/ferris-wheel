package com.ctrip.ferriswheel.core.formula.func;

import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.FuncElement;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;

/**
 * VLOOKUP (lookup_value, table_array, col_index_num, [range_lookup])
 */
public class VLookup implements Function {
    public static final String VLOOKUP = "VLOOKUP";

    @Override
    public String getName() {
        return VLOOKUP;
    }

    @Override
    public boolean checkArgc(int argc) {
        return argc == 3 || argc == 4;
    }

    @Override
    public void evaluate(FuncElement element, FormulaEvaluationContext context) {
        boolean rangeLookup = true;
        if (element.getArgc() == 4) {
            rangeLookup = context.popOperand().booleanValue();
        }
        final int colIndexNum = context.popOperand().intValue() - 1;
        Variant tableArray = context.popOperand();
        Variant lookupValue = context.popOperand();

        final int columnCount = tableArray.columnCount();
        final int rowCount = tableArray.rowCount();

        Variant result = null;

        for (int i = 0; i < rowCount; i++) {
            final int offset = i * columnCount;
            Variant var = tableArray.item(offset);
            final int compare = lookupValue.compareTo(var);
            if (compare == 0) {
                result = tableArray.item(offset + colIndexNum);
                break;
            } else if (rangeLookup && compare < 0) {
                if (i > 0) {
                    result = tableArray.item(columnCount * (i - 1) + colIndexNum);
                } else {
                    result = Value.err(ErrorCodes.NA); // #N/A!
                }
                break;
            }
        }
        if (result == null && rangeLookup && rowCount > 0) {
            result = tableArray.item(columnCount * (rowCount - 1) + colIndexNum);
        }
        if (result == null) {
            result = Value.err(ErrorCodes.NA); // TODO #N/A! or #VALUE!?;
        }

        context.pushOperand(result);
    }
}
