package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.SheetAsset;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.formula.CellReferenceElement;
import com.ctrip.ferriswheel.core.formula.NameReferenceElement;
import com.ctrip.ferriswheel.core.formula.RangeReferenceElement;

public interface FormulaEvaluationContext {
    Sheet getCurrentSheet();

    SheetAsset getCurrentAsset();

    void pushOperand(Variant operand);

    Variant popOperand();

    Variant resolveReference(CellReferenceElement referenceElement);

    Variant resolveReference(RangeReferenceElement referenceElement);

    Variant resolveReference(NameReferenceElement referenceElement);
}
