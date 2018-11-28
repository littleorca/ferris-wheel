package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.core.ref.CellRef;
import com.ctrip.ferriswheel.core.intf.Sheet;
import com.ctrip.ferriswheel.core.intf.Table;
import com.ctrip.ferriswheel.core.intf.Variant;
import com.ctrip.ferriswheel.core.ref.CellRef;

public interface FormulaEvaluationContext {
    Sheet getCurrentSheet();

    Table getCurrentTable();

    void pushOperand(Variant operand);

    Variant popOperand();

    Variant resolveReference(CellRef cellRef);

    Table resolveTable(String sheetName, String tableName);
}
