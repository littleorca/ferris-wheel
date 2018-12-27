package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.formula.FormulaElement;
import com.ctrip.ferriswheel.api.Sheet;
import com.ctrip.ferriswheel.api.table.Table;
import com.ctrip.ferriswheel.api.variant.Variant;

import java.util.Stack;

public class FormulaEvaluator {
    private ReferenceResolver resolver;
    private Sheet currentSheet;
    private Table currentTable;

    public FormulaEvaluator(ReferenceResolver resolver) {
        this.resolver = resolver;
    }

    public Variant evaluate(FormulaElement[] elements) {
        Context context = new Context();
        for (FormulaElement elem : elements) {
            elem.evaluate(context);
        }
        Variant ret = context.operands.pop();
        if (!context.operands.isEmpty()) {
            throw new RuntimeException("Abnormal stack state.");
        }
        return ret;
    }

    public Sheet getCurrentSheet() {
        return currentSheet;
    }

    public void setCurrentSheet(Sheet currentSheet) {
        this.currentSheet = currentSheet;
    }

    public Table getCurrentTable() {
        return currentTable;
    }

    public void setCurrentTable(Table currentTable) {
        this.currentTable = currentTable;
        if (currentTable != null) {
            setCurrentSheet(currentTable.getSheet());
        }
    }

    class Context implements FormulaEvaluationContext {
        Stack<Variant> operands = new Stack<>();

        @Override
        public Sheet getCurrentSheet() {
            return currentSheet;
        }

        @Override
        public Table getCurrentTable() {
            return currentTable;
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

        protected void setOperands(Stack<Variant> operands) {
            this.operands = operands;
        }

        @Override
        public Variant resolveReference(CellRef cellRef) {
            return resolver.resolve(cellRef, this);
        }

        @Override
        public Table resolveTable(String sheetName, String tableName) {
            return resolver.resolveTable(sheetName, tableName, this);
        }
    }
}
