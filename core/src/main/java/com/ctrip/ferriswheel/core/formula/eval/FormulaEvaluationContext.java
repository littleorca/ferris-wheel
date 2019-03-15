package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.SimpleReferenceElement;

public interface FormulaEvaluationContext {
    Sheet getCurrentSheet();

    Table getCurrentTable();

    void pushOperand(Variant operand);

    Variant popOperand();

    Variant resolveReference(SimpleReferenceElement referenceElement);

    Table resolveTable(String sheetName, String tableName);
}
