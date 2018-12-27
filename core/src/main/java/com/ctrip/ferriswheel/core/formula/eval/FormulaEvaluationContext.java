package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.api.Sheet;
import com.ctrip.ferriswheel.api.table.Table;
import com.ctrip.ferriswheel.api.variant.Variant;

public interface FormulaEvaluationContext {
    Sheet getCurrentSheet();

    Table getCurrentTable();

    void pushOperand(Variant operand);

    Variant popOperand();

    Variant resolveReference(CellRef cellRef);

    Table resolveTable(String sheetName, String tableName);
}
