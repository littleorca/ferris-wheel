package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.ref.RangeReference;
import com.ctrip.ferriswheel.core.ref.TableRange;

import java.util.LinkedList;
import java.util.List;

public class RangeReferenceElement extends ReferenceElement {
    private RangeReference rangeReference;

    public RangeReferenceElement(RangeReference rangeReference) {
        this.rangeReference = rangeReference;
    }


    @Override
    public void evaluate(FormulaEvaluationContext context) {
        if (!rangeReference.isAlive()) {
            context.pushOperand(Value.err(ErrorCodes.REF));
            return;
        }

        Table table = context.resolveTable(rangeReference.getSheetName(), rangeReference.getAssetName());

        if (table == null) {
            context.pushOperand(Value.err(ErrorCodes.REF));
            return;
        }

        TableRange validRange = rangeReference.getOverlappedRectangle(table);
        if (validRange == null) {
            context.pushOperand(Value.err(ErrorCodes.REF));
            return;
        }

        List<Variant> valueList = new LinkedList<>();
        for (int row = validRange.getTop(); row <= validRange.getBottom(); row++) {
            for (int col = validRange.getLeft(); col <= validRange.getRight(); col++) {
                valueList.add(Value.from(table.getCell(row, col).getData()));
            }
        }
        final int actualResultColumns = validRange.getRight() - validRange.getLeft() + 1;
        context.pushOperand(new Value.ListValue(valueList, actualResultColumns));
    }

    @Override
    public String toString() {
        return rangeReference.toString();
    }

    public RangeReference getRangeReference() {
        return rangeReference;
    }

    void setRangeReference(RangeReference rangeReference) {
        this.rangeReference = rangeReference;
    }

}
