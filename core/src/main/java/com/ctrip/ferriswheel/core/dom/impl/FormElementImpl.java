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

package com.ctrip.ferriswheel.core.dom.impl;

import com.ctrip.ferriswheel.common.form.FormField;
import com.ctrip.ferriswheel.common.view.Layout;
import com.ctrip.ferriswheel.core.dom.FormElement;

import java.util.Iterator;
import java.util.List;

public final class FormElementImpl extends AbstractElement implements FormElement {

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public FormField getField(int index) {
        return null;
    }

    @Override
    public FormField getField(String name) {
        return null;
    }

    @Override
    public List<FormField> getFieldList() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Layout getLayout() {
        return null;
    }

    @Override
    public Iterator<FormField> iterator() {
        return null;
    }
}