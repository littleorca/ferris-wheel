package com.ctrip.ferriswheel.core.formula.eval;

import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.asset.Asset;
import com.ctrip.ferriswheel.core.formula.CellReferenceElement;
import com.ctrip.ferriswheel.core.formula.NameReferenceElement;
import com.ctrip.ferriswheel.core.formula.RangeReferenceElement;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.NameReference;

public interface ReferenceResolver {
    Variant resolve(CellReferenceElement referenceElement, FormulaEvaluationContext context);

    Variant resolve(RangeReferenceElement referenceElement, FormulaEvaluationContext context);

    Variant resolve(NameReferenceElement referenceElement, FormulaEvaluationContext context);

    Asset getReferredAsset(CellReference cellReference);

    Asset getReferredAsset(NameReference nameReference);

    Asset getAssetById(long assetId);
}
