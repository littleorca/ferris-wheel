package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.intf.Table;
import com.ctrip.ferriswheel.core.intf.Variant;
import com.ctrip.ferriswheel.core.ref.CellRef;

public interface ReferenceResolver {
    Variant resolve(CellRef cellRef, FormulaEvaluationContext context);

    Table resolveTable(String sheetName, String tableName, FormulaEvaluationContext context);
}
