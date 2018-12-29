package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.api.variant.DynamicVariant;
import com.ctrip.ferriswheel.api.variant.ErrorCode;
import com.ctrip.ferriswheel.api.variant.Variant;
import com.ctrip.ferriswheel.api.variant.VariantType;
import com.ctrip.ferriswheel.core.bean.DynamicVariantImpl;
import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.Formula;
import com.ctrip.ferriswheel.core.formula.FormulaElement;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ValueNode extends AssetNode implements VariantNode {
    private DynamicVariantImpl data;
//    private transient boolean dirty;

    ValueNode(AssetManager assetManager, Value value, String formulaString) {
        super(assetManager);
        this.data = new DynamicVariantImpl(formulaString, value);
    }

    ValueNode(AssetManager assetManager, DynamicVariant data) {
        super(assetManager);
        this.data = new DynamicVariantImpl(data);
    }

    public boolean isFormula() {
        return data.isFormula();
    }

    public DynamicVariantImpl getData() {
        return data;
    }

    protected void setValue(Variant value) {
        this.data.setVariant(value);
        updateSequenceNumber();
    }

    public String getFormulaString() {
        return data.getFormulaString();
    }

    protected Formula getFormula() {
        return data.getFormula();
    }

    protected void setFormula(Formula formula) {
        this.data.setFormula(formula);
        updateSequenceNumber();
    }

    protected void setDynamicVariant(DynamicVariant variable) {
        this.data = new DynamicVariantImpl(variable);
        updateSequenceNumber();
    }

//    public boolean isDirty() {
//        return dirty;
//    }
//
//    protected void setDirty(boolean dirty) {
//        this.dirty = dirty;
//    }

    protected FormulaElement[] getFormulaElements() {
        return getFormula() == null ? null : getFormula().getElements();
    }

    protected void erase() {
        setFormula(null);
        setValue(Value.BLANK);
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
