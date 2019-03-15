package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.SimpleReferenceElement;

public interface ReferenceResolver {
    Variant resolve(SimpleReferenceElement referenceElement, FormulaEvaluationContext context);

    Table resolveTable(String sheetName, String tableName, FormulaEvaluationContext context);
}
