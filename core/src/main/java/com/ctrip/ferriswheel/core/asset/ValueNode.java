package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.bean.DynamicValue;
import com.ctrip.ferriswheel.core.formula.ErrorCode;
import com.ctrip.ferriswheel.core.formula.Formula;
import com.ctrip.ferriswheel.core.formula.FormulaElement;
import com.ctrip.ferriswheel.core.bean.DynamicValue;
import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.formula.ErrorCode;
import com.ctrip.ferriswheel.core.formula.Formula;
import com.ctrip.ferriswheel.core.formula.FormulaElement;
import com.ctrip.ferriswheel.core.intf.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class ValueNode extends AssetNode implements VariantNode {
    private DynamicValue variable;
//    private transient boolean dirty;

    ValueNode(AssetManager assetManager, Value value, String formulaString) {
        super(assetManager);
        this.variable = new DynamicValue(formulaString, value);
    }

    ValueNode(AssetManager assetManager, DynamicVariant variable) {
        super(assetManager);
        this.variable = new DynamicValue(variable);
    }

    public boolean isFormula() {
        return variable.isFormula();
    }

    public Variant getValue() {
        return variable.getValue();
    }

    protected void setValue(Variant value) {
        this.variable.setValue(value);
        updateSequenceNumber();
    }

    public String getFormulaString() {
        return variable.getFormulaString();
    }

    protected Formula getFormula() {
        return variable.getFormula();
    }

    protected void setFormula(Formula formula) {
        this.variable.setFormula(formula);
        updateSequenceNumber();
    }

    protected void setDynamicVariant(DynamicVariant variable) {
        this.variable = new DynamicValue(variable);
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
        return variable.valueType();
    }

    @Override
    public boolean isValid() {
        return variable.isValid();
    }

    @Override
    public boolean isBlank() {
        return variable.isBlank();
    }

    @Override
    public ErrorCode errorValue() {
        return variable.errorValue();
    }

    @Override
    public int intValue() {
        return variable.intValue();
    }

    @Override
    public long longValue() {
        return variable.longValue();
    }

    @Override
    public float floatValue() {
        return variable.floatValue();
    }

    @Override
    public double doubleValue() {
        return variable.doubleValue();
    }

    @Override
    public BigDecimal decimalValue() {
        return variable.decimalValue();
    }

    @Override
    public boolean booleanValue() {
        return variable.booleanValue();
    }

    @Override
    public Date dateValue() {
        return variable.dateValue();
    }

    @Override
    public String strValue() {
        return variable.strValue();
    }

    @Override
    public List<Variant> listValue() {
        return variable.listValue();
    }

    @Override
    public int itemCount() {
        return variable.itemCount();
    }

    @Override
    public Variant item(int i) {
        return variable.item(i);
    }

    @Override
    public int columnCount() {
        return variable.columnCount();
    }

    @Override
    public int rowCount() {
        return variable.rowCount();
    }

    @Override
    public int compareTo(Variant o) {
        return variable.compareTo(o);
    }
}
