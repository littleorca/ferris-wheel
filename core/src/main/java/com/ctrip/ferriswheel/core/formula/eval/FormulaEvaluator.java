package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.SheetAsset;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.asset.Asset;
import com.ctrip.ferriswheel.core.formula.CellReferenceElement;
import com.ctrip.ferriswheel.core.formula.FormulaElement;
import com.ctrip.ferriswheel.core.formula.NameReferenceElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FormulaEvaluator {
    private ReferenceResolver resolver;
    private Sheet currentSheet;
    private SheetAsset currentAsset;

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

    public List<Variant> evaluatePartial(FormulaElement[] partialElements) {
        Context context = new Context();
        for (FormulaElement elem : partialElements) {
            elem.evaluate(context);
        }
        return new ArrayList<>(context.operands);
    }

    public Sheet getCurrentSheet() {
        return currentSheet;
    }

    public void setCurrentSheet(Sheet currentSheet) {
        this.currentSheet = currentSheet;
    }

    public SheetAsset getCurrentAsset() {
        return currentAsset;
    }

    public void setCurrentAsset(SheetAsset currentAsset) {
        this.currentAsset = currentAsset;
        if (currentAsset != null) {
            // TODO review this cast
            setCurrentSheet((Sheet) ((Asset) currentAsset).getParent());
        }
    }

    class Context implements FormulaEvaluationContext {
        Stack<Variant> operands = new Stack<>();

        @Override
        public Sheet getCurrentSheet() {
            return currentSheet;
        }

        @Override
        public SheetAsset getCurrentAsset() {
            return currentAsset;
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
        public Variant resolveReference(CellReferenceElement referenceElement) {
            return resolver.resolve(referenceElement, this);
        }

        @Override
        public Variant resolveReference(NameReferenceElement referenceElement) {
            return resolver.resolve(referenceElement, this);
        }

        @Override
        public Table resolveTable(String sheetName, String tableName) {
            return resolver.resolveTable(sheetName, tableName, this);
        }
    }
}
