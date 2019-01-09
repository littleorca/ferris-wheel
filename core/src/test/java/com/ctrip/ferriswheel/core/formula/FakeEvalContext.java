package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.core.ref.CellRef;

import java.util.Stack;

public class FakeEvalContext implements FormulaEvaluationContext {
    private Sheet currentSheet;
    private Table currentTable;
    private Stack<Variant> operands = new Stack<>();

    @Override
    public Sheet getCurrentSheet() {
        return currentSheet;
    }

    public void setCurrentSheet(Sheet currentSheet) {
        this.currentSheet = currentSheet;
    }

    @Override
    public Table getCurrentTable() {
        return currentTable;
    }

    public void setCurrentTable(Table currentTable) {
        this.currentTable = currentTable;
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
    public Variant resolveReference(CellRef cellRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Table resolveTable(String sheetName, String tableName) {
        throw new UnsupportedOperationException();
    }
}
