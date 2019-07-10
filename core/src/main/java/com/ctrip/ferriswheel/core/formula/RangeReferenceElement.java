package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.ref.RangeReference;

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

        final int top = rangeReference.getTop() == -1 ? 0 : rangeReference.getTop();
        final int bottom = rangeReference.getBottom() == -1 ? table.getRowCount() - 1 : rangeReference.getBottom();
        final int left = rangeReference.getLeft() == -1 ? 0 : rangeReference.getLeft();
        final int right = rangeReference.getRight() == -1 ? table.getColumnCount() - 1 : rangeReference.getRight();
        List<Variant> valueList = new LinkedList<>();
        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                valueList.add(Value.from(table.getCell(row, col).getData()));
            }
        }
        context.pushOperand(new Value.ListValue(valueList, right - left + 1));
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
