package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.api.table.Table;
import com.ctrip.ferriswheel.api.variant.Variant;

public interface ReferenceResolver {
    Variant resolve(CellRef cellRef, FormulaEvaluationContext context);

    Table resolveTable(String sheetName, String tableName, FormulaEvaluationContext context);
}
