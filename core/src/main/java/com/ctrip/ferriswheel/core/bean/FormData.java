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

package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.common.form.Form;
import com.ctrip.ferriswheel.common.form.FormField;
import com.ctrip.ferriswheel.common.view.Layout;

import java.io.Serializable;
import java.util.*;

public class FormData implements Form, Serializable {
    private String name;
    private LinkedHashMap<String, FormField> fields;
    private Layout layout;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getFieldCount() {
        return fields == null ? 0 : fields.size();
    }

    @Override
    public FormField getField(int index) {
        return fields.values().toArray(new FormField[fields.size()])[index]; // TODO caution: bad performance!
    }

    @Override
    public FormField getField(String name) {
        return fields.get(name);
    }

    @Override
    public List<FormField> getFieldList() {
        if (fields == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(fields.values());
    }

    public void setFields(List<FormField> fields) {
        this.fields = new LinkedHashMap<>(fields.size());
        for (FormField field : fields) {
            this.fields.put(field.getName(), field);
        }
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public Iterator<FormField> iterator() {
        if (fields == null) {
            return Collections.emptyIterator();
        }
        return fields.values().iterator();
    }
}
