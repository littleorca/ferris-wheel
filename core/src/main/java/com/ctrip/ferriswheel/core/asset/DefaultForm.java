/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.form.Form;
import com.ctrip.ferriswheel.common.form.FormField;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.core.action.ExecuteQuery;
import com.ctrip.ferriswheel.core.action.UpdateForm;
import com.ctrip.ferriswheel.core.formula.CellReferenceElement;
import com.ctrip.ferriswheel.core.formula.Formula;
import com.ctrip.ferriswheel.core.formula.FormulaElement;
import com.ctrip.ferriswheel.core.formula.NameReferenceElement;
import com.ctrip.ferriswheel.core.ref.CellReference;
import com.ctrip.ferriswheel.core.ref.NameReference;
import com.ctrip.ferriswheel.core.view.FormLayout;

import java.util.*;

public class DefaultForm extends SheetAssetNode implements Form {
    private final FormLayout layout = new FormLayout();
    private final NamedAssetList<DefaultFormField> fields;

    DefaultForm(String name, DefaultSheet sheet) {
        super(name, sheet);
        this.fields = new NamedAssetList<>(this);
    }

    @Override
    public FormLayout getLayout() {
        return layout;
    }

    @Override
    public int getFieldCount() {
        return fields.size();
    }

    @Override
    public DefaultFormField getField(int index) {
        return fields.get(index);
    }

    @Override
    public DefaultFormField getField(String name) {
        return fields.get(name);
    }

    @Override
    public List<FormField> getFieldList() {
        return Collections.unmodifiableList(new ArrayList<>(fields.values()));
    }

    void addField(FormField fieldData) {
        if (fields.get(fieldData.getName()) != null) {
            throw new IllegalArgumentException("Duplicated field name: " + fieldData.getName());
        }
        DefaultFormField field = new DefaultFormField(fieldData.getName(), this);
        field.fillFieldData(fieldData);
        getSheet().publicly(new UpdateForm(getSheet().getName(), getName(), this), () -> {
            fields.add(field);
            getSheet().getWorkbook().onAssetUpdate(this);
        });
    }

    public void updateField(FormField fieldData) {
        DefaultFormField field = fields.get(fieldData.getName());
        if (field == null) {
            throw new IllegalArgumentException("Field name not exists: " + field.getName());
        }
        getSheet().publicly(new UpdateForm(getSheet().getName(), getName(), this), () -> {
            field.fillFieldData(fieldData);
            getSheet().getWorkbook().onAssetUpdate(this);
        });
    }

    public void renameField(String oldName, String newName) {
        DefaultFormField field = fields.get(oldName);
        if (field == null) {
            throw new IllegalArgumentException("Field name not exists: " + oldName);
        }
        if (fields.get(newName) != null) {
            throw new IllegalArgumentException("Duplicated field name: " + newName);
        }
        getSheet().publicly(new UpdateForm(getSheet().getName(), getName(), this), () -> {
            field.setName(newName);
            getSheet().getWorkbook().onAssetUpdate(this);
        });
    }

    public void moveField(String name, int index) {
        if (fields.get(name) == null) {
            throw new IllegalArgumentException("Field name not exists: " + name);
        }
        getSheet().publicly(new UpdateForm(getSheet().getName(), getName(), this), () -> {
            DefaultFormField field = fields.remove(name);
            fields.add(index, field);
            getSheet().getWorkbook().onAssetUpdate(this);
        });
    }

    public void removeField(String name) {
        if (fields.get(name) == null) {
            return;
        }
        getSheet().publicly(new UpdateForm(getSheet().getName(), getName(), this), () -> {
            fields.remove(name);
            getSheet().getWorkbook().onAssetUpdate(this);
        });
    }

    void clearFields() {
        fields.clear();
    }

    @Override
    public Iterator<FormField> iterator() {
        return Collections.<FormField>unmodifiableCollection(fields.values()).iterator();
    }

    public void submit(Map<String, Variant> params) {
        DefaultWorkbook wb = getSheet().getWorkbook();
        wb.withoutRefresh(() -> doSubmit(params));
        wb.refreshIfNeeded();
    }

    void doSubmit(Map<String, Variant> params) {
        Map<Long, ExecuteQuery> pendingQuerys = new HashMap<>();
        for (Map.Entry<String, Variant> pair : params.entrySet()) {
            DefaultFormField field = fields.get(pair.getKey());
            if (field == null) {
                throw new IllegalArgumentException("There is no field named: " + pair.getKey() + ", form=" + getName());
            }
            field.setValue(pair.getValue());
            for (int i = 0; i < field.getBindingCount(); i++) {
                DefaultFormFieldBinding binding = field.getBinding(i);
                updateOrCollectQuery(field, binding, pendingQuerys);
            }
        }

        for (Map.Entry<Long, ExecuteQuery> entry : pendingQuerys.entrySet()) {
            DefaultQueryAutomaton queryAutomaton = (DefaultQueryAutomaton) getAssetManager().get(entry.getKey());
            queryAutomaton.handleAction(entry.getValue());
        }
    }

    private void updateOrCollectQuery(DefaultFormField field,
                                      DefaultFormFieldBinding binding,
                                      Map<Long, ExecuteQuery> pendingQuerys) {
        Formula f = binding.getTargetRefHolder().getFormula();
        if (f.getElements().length != 1) { // should be checked during update procedure.
            throw new RuntimeException();
        }
        FormulaElement elem = f.getElements()[0];
        if (elem instanceof CellReferenceElement) {
            CellReference ref = ((CellReferenceElement) elem).getCellReference();
            if (ref.isValid()) {
                DefaultCell cell = (DefaultCell) getAssetManager().get(ref.getCellId());
                cell.getRow().getTable().setCellValue(cell.getRowIndex(), cell.getColumnIndex(), field.getValue());
            }

        } else if (elem instanceof NameReferenceElement) {
            NameReference ref = ((NameReferenceElement) elem).getNameReference();
            if (ref.isValid()) {
                AssetNode target = (AssetNode) getAssetManager().get(ref.getTargetId());
                DefaultQueryAutomaton queryAutomaton = target.parent(DefaultQueryAutomaton.class);
                if (queryAutomaton == null) {
                    throw new RuntimeException("Unsupported binding target: " + target.getClass());
                }
                DefaultTable table = queryAutomaton.getTable();
                ExecuteQuery eq = pendingQuerys.get(queryAutomaton.getAssetId());
                if (eq == null) {
                    eq = new ExecuteQuery(table.getSheet().getName(), table.getName(), new HashMap<>());
                    pendingQuerys.put(queryAutomaton.getAssetId(), eq);
                }
                eq.getParams().put(ref.getTargetName(), field.getValue());
            }

        } else {
            throw new RuntimeException("Unsupported reference type: " + elem.getClass());
        }
    }
}
