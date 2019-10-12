package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.variant.*;
import com.ctrip.ferriswheel.core.formula.*;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluator;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.HotAreaReference;
import com.ctrip.ferriswheel.core.ref.NameReference;
import com.ctrip.ferriswheel.core.ref.RangeReference;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ValueNode extends AssetNode implements VariantNode {
    private DynamicValue data;
    private Formula formula;

    ValueNode(AssetManager assetManager, Value value, String formulaString) {
        super(assetManager);
        this.data = new DynamicValue(formulaString, value);
        this.formula = (formulaString == null || formulaString.isEmpty()) ? null : new Formula(formulaString);
    }

    ValueNode(AssetManager assetManager, DynamicVariant data) {
        super(assetManager);
        this.data = new DynamicValue(data);
        this.formula = data.isFormula() ? new Formula(data.getFormulaString()) : null;
    }

    @Override
    protected void beforeEvaluating(EvaluationContext context) {
        if (!isFormula()) {
            return;
        }

        for (FormulaElement fe : formula) {
            if (fe instanceof CellReferenceElement) {
                // TODO check reference and formula string
            } else if (fe instanceof RangeReferenceElement) {
                // TODO check reference and formula string
            } else if (fe instanceof NameReferenceElement) {
                // TODO check reference and formula string
            }
        }
    }

    @Override
    public EvaluationState doEvaluate(EvaluationContext context) {
        if (!isFormula()) {
            return EvaluationState.DONE;
        }
        if (getFormula() == null) {
            throw new IllegalArgumentException();
        }
        SheetAssetNode sheetAsset = parent(SheetAssetNode.class);
        FormulaEvaluator evaluator = context.getFormulaEvaluator();
        evaluator.setCurrentSheet(sheetAsset.getSheet());
        evaluator.setCurrentAsset(sheetAsset);
        Variant value = evaluator.evaluate(getFormula());
        if (!value.equals(getData())) {
            doUpdateValue(value);
        }
        return EvaluationState.DONE;
    }

    protected void doUpdateValue(Variant newValue) {
        setValue(newValue);
    }

    @Override
    public boolean isVolatile() {
        return isFormula() && formula.isVolatile();
    }

    public boolean isFormula() {
        return data.isFormula();
    }

    public DynamicValue getData() {
        return data;
    }

    protected void setValue(Variant value) {
        this.data.setVariant(value);
        markDirty();
    }

    public String getFormulaString() {
        return data.getFormulaString();
    }

    protected Formula getFormula() {
        return formula;
    }

    protected void setFormula(Formula formula) {
        this.data.setFormulaString(formula == null ? null : formula.getString());
        this.formula = formula;
        markDirty();
        resolveFormulaIfNeeded();
    }

    protected void setDynamicVariant(DynamicVariant variable) {
        this.data = new DynamicValue(variable);
        if (variable != null && variable.isFormula()) {
            this.formula = new Formula(variable.getFormulaString());
        } else {
            this.formula = null;
        }
        markDirty();
        resolveFormulaIfNeeded();
    }

    private void resolveFormulaIfNeeded() {
        if (isAttached()) {
            getAssetManager().getReferenceMaintainer().resolveFormula(this);
        }
    }

    protected void erase() {
        setFormula(null);
        setValue(Value.BLANK);
    }

    @Override
    protected void afterAttached() {
        getAssetManager().getReferenceMaintainer().resolveFormula(this);
    }

    @Override
    protected void onExternalDependencyChange() {
        super.onExternalDependencyChange();

        if (isFormula()) {
            for (FormulaElement fe : formula) {
                if (fe instanceof CellReferenceElement) {
                    CellReference ref = ((CellReferenceElement) fe).getCellReference();
                    checkHotAreaReference(ref);

                } else if (fe instanceof RangeReferenceElement) {
                    RangeReference ref = ((RangeReferenceElement) fe).getRangeReference();
                    checkHotAreaReference(ref);

                } else if (fe instanceof NameReferenceElement) {
                    NameReference ref = ((NameReferenceElement) fe).getNameReference();
                    checkNameReference(ref);
                }
            }
        }
    }

    protected void checkHotAreaReference(HotAreaReference ref) {
        if (!ref.isValid()) {
            return;
        }
        if (getAssetManager().get(ref.getHotAreaId()) == null) {
            ref.setHotAreaId(Asset.UNSPECIFIED_ASSET_ID);
        }
    }

    protected void checkNameReference(NameReference ref) {
        if (!ref.isValid()) {
            return;
        }
        if (getAssetManager().get(ref.getTargetId())==null) {
            ref.setTargetId(Asset.UNSPECIFIED_ASSET_ID);
        }
    }

    @Override
    public VariantType valueType() {
        return data.valueType();
    }

    @Override
    public boolean isValid() {
        return data.isValid();
    }

    @Override
    public boolean isBlank() {
        return data.isBlank();
    }

    @Override
    public ErrorCode errorValue() {
        return data.errorValue();
    }

    @Override
    public int intValue() {
        return data.intValue();
    }

    @Override
    public long longValue() {
        return data.longValue();
    }

    @Override
    public float floatValue() {
        return data.floatValue();
    }

    @Override
    public double doubleValue() {
        return data.doubleValue();
    }

    @Override
    public BigDecimal decimalValue() {
        return data.decimalValue();
    }

    @Override
    public boolean booleanValue() {
        return data.booleanValue();
    }

    @Override
    public Date dateValue() {
        return data.dateValue();
    }

    @Override
    public String strValue() {
        return data.strValue();
    }

    @Override
    public List<Variant> listValue() {
        return data.listValue();
    }

    @Override
    public int itemCount() {
        return data.itemCount();
    }

    @Override
    public Variant item(int i) {
        return data.item(i);
    }

    @Override
    public int columnCount() {
        return data.columnCount();
    }

    @Override
    public int rowCount() {
        return data.rowCount();
    }

    @Override
    public int compareTo(Variant o) {
        return data.compareTo(o);
    }
}
