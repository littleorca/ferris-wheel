package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.ref.RangeRef;

import java.util.LinkedList;
import java.util.List;

public class RangeReferenceElement extends ReferenceElement {
    private RangeRef rangeRef;

    public RangeReferenceElement(RangeRef rangeRef) {
        this.rangeRef = rangeRef;
    }


    @Override
    public void evaluate(FormulaEvaluationContext context) {
        if (!rangeRef.isValid()) {
            context.pushOperand(Value.err(ErrorCodes.ILLEGAL_REF));
            return;
        }

        Table table = context.resolveTable(rangeRef.sheetName(), rangeRef.tableName());

        if (table == null) {
            context.pushOperand(Value.err(ErrorCodes.ILLEGAL_REF));
            return;
        }

        final int top = rangeRef.getTop() == -1 ? 0 : rangeRef.getTop();
        final int bottom = rangeRef.getBottom() == -1 ? table.getRowCount() - 1 : rangeRef.getBottom();
        final int left = rangeRef.getLeft() == -1 ? 0 : rangeRef.getLeft();
        final int right = rangeRef.getRight() == -1 ? table.getColumnCount() - 1 : rangeRef.getRight();
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
        return rangeRef.toString();
    }

    public RangeRef getRangeRef() {
        return rangeRef;
    }

    void setRangeRef(RangeRef rangeRef) {
        this.rangeRef = rangeRef;
    }

}
