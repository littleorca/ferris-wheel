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
import com.ctrip.ferriswheel.common.action.ActionListener;
import com.ctrip.ferriswheel.core.dom.Element;
import com.ctrip.ferriswheel.core.dom.WorkbookDocument;
import com.ctrip.ferriswheel.core.dom.WorkbookElement;
import com.ctrip.ferriswheel.core.dom.helper.Tag;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorkbookDocumentImpl extends AbstractDocument implements WorkbookDocument {
    private static final Map<String, Class<? extends Element>> TAG_NAMES = new ConcurrentHashMap();
    private static final Map<Class<? extends Element>, Class<? extends Element>> IMPLEMENTATIONS = new ConcurrentHashMap();

    static {
        for (Tag tag : Tag.values()) {
            TAG_NAMES.put(tag.getTagName(), tag.getInterface());
            IMPLEMENTATIONS.put(tag.getInterface(), tag.getImplementation());
        }
    }

    @Override
    public WorkbookElement getDocumentElement() {
        return (WorkbookElement) super.getDocumentElement();
    }

    @Override
    public Element createElement(String tagName) {
        Class<? extends Element> elementInterface = TAG_NAMES.get(tagName);
        if (elementInterface == null) {
            throw new IllegalArgumentException("Invalid tag name: " + tagName);
        }
        return createElement(elementInterface);
    }

    @Override
    public <E extends Element> E createElement(Class<E> elementInterface) {
        Class<? extends Element> elementClass = IMPLEMENTATIONS.get(elementInterface);
        if (elementClass == null) {
            throw new IllegalArgumentException("Invalid element interface.");
        }
        if (!AbstractElement.class.isAssignableFrom(elementClass) ||
                !elementInterface.isAssignableFrom(elementClass)) {
            throw new RuntimeException("Invalid element implementation, probably a bug.");
        }

        AbstractElement element;
        try {
            element = (AbstractElement) elementClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        element.initialize(this);
        return elementInterface.cast(element);
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
