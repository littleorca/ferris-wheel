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

import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.Version;
import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.action.ActionListener;
import com.ctrip.ferriswheel.core.dom.Element;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class WorkbookDocument extends AbstractDocument<WorkbookElement> implements Workbook {
    private static final ConcurrentHashMap<String, Class<? extends AbstractElement>> TAG_TO_CLASS_MAP = new ConcurrentHashMap();

    static {
        TAG_TO_CLASS_MAP.put(SheetElement.TAG_NAME, SheetElement.class);
        TAG_TO_CLASS_MAP.put(TableElement.TAG_NAME, TableElement.class);
        TAG_TO_CLASS_MAP.put(ChartElement.TAG_NAME, ChartElement.class);
        TAG_TO_CLASS_MAP.put(FormElement.TAG_NAME, FormElement.class);
        TAG_TO_CLASS_MAP.put(TextElement.TAG_NAME, TextElement.class);
    }

    @Override
    public Element createElement(String tagName) {
        Class<? extends AbstractElement> elementClass = TAG_TO_CLASS_MAP.get(tagName);
        if (elementClass == null) {
            throw new IllegalArgumentException("Invalid tag name: " + tagName);
        }
        return createElement(elementClass);
    }

    @Override
    public <T extends Element> T createElement(Class<T> elementClass) {
        if (!AbstractElement.class.isAssignableFrom(elementClass)) {
            throw new IllegalArgumentException();
        }

        T element;
        try {
            element = elementClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        ((AbstractElement) element).initialize(this);
        return element;
    }

    @Override
    public Version getVersion() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public int getSheetCount() {
        return 0;
    }

    @Override
    public Sheet getSheet(int index) {
        return null;
    }

    @Override
    public Sheet getSheet(String name) {
        return null;
    }

    @Override
    public Sheet addSheet(String name) {
        return null;
    }

    @Override
    public Sheet addSheet(int index, String name) {
        return null;
    }

    @Override
    public void renameSheet(String oldName, String newName) {

    }

    @Override
    public void moveSheet(String name, int index) {

    }

    @Override
    public Sheet removeSheet(int index) {
        return null;
    }

    @Override
    public Sheet removeSheet(String name) {
        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void refresh(boolean force) {

    }

    @Override
    public void addListener(ActionListener listener) {

    }

    @Override
    public boolean removeListener(ActionListener listener) {
        return false;
    }

    @Override
    public Iterator<Sheet> iterator() {
        return null;
    }

    @Override
    protected WorkbookElement createDocumentElement() {
        return createElement(WorkbookElement.class);
    }

}
