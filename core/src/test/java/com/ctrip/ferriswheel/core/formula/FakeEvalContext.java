package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.SheetAsset;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;

import java.util.Stack;

public class FakeEvalContext implements FormulaEvaluationContext {
    private Sheet currentSheet;
    private SheetAsset currentAsset;
    private Stack<Variant> operands = new Stack<>();

    @Override
    public Sheet getCurrentSheet() {
        return currentSheet;
    }

    public void setCurrentSheet(Sheet currentSheet) {
        this.currentSheet = currentSheet;
    }

    @Override
    public SheetAsset getCurrentAsset() {
        return currentAsset;
    }

    public void setCurrentAsset(Table currentTable) {
        this.currentAsset = currentAsset;
    }

    @Override
    public void pushOperand(Variant operand) {
        operands.push(operand);
    }

    @Override
    public Variant popOperand() {
        return operands.pop();
    }

    public Stack<Variant> getOperands() {
        return operands;
    }

    @Override
    public Variant resolveReference(CellReferenceElement referenceElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Variant resolveReference(NameReferenceElement referenceElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Table resolveTable(String sheetName, String tableName) {
        throw new UnsupportedOperationException();
    }
}
