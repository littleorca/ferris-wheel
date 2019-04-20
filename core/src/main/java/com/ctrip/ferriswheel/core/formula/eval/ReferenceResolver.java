package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.CellReferenceElement;
import com.ctrip.ferriswheel.core.formula.NameReferenceElement;

public interface ReferenceResolver {
    Variant resolve(CellReferenceElement referenceElement, FormulaEvaluationContext context);

    Variant resolve(NameReferenceElement referenceElement, FormulaEvaluationContext context);

    Table resolveTable(String sheetName, String tableName, FormulaEvaluationContext context);
}
