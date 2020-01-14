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

import com.ctrip.ferriswheel.core.dom.Attribute;
import com.ctrip.ferriswheel.core.dom.Element;
import com.ctrip.ferriswheel.core.dom.helper.AttributeMap;

import java.util.Collection;

public abstract class AbstractElement extends AbstractContainerNode implements Element {
    protected static final String ATTR_NAME = "name";

    private AttributeMap<AttributeImpl> attributes = new AttributeMap<>();

    @Override
    public String getNodeName() {
        return getTagName();
    }

    @Override
    public String getTextContent() {
        if (getChildCount() == 0) {
            return TextNodeImpl.DEFAULT_TEXT_CONTENT;

        } else if (getChildCount() == 1) {
            return firstChild().getTextContent();

        } else {
            StringBuilder sb = new StringBuilder();
            forEachChild(child -> sb.append(child.getTextContent()));
            return sb.toString();
        }
    }

    @Override
    public void setTextContent(String textContent) {
        AbstractNode child;
        while ((child = firstChild()) != null) {
            applyRemoveChild(child);
        }
        if (textContent != null) {
            appendChild(getOwnerDocument().createTextNode(textContent));
        }
    }

    @Override
    public String getNodeValue() {
        return null;
    }

    @Override
    public void setNodeValue(String nodeValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributes.contains(name);
    }

    @Override
    public String getAttribute(String name) {
        AttributeImpl attr = attributes.get(name);
        return attr == null ? null : attr.getValue();
    }

    @Override
    public String setAttribute(String name, String value) {
        // TODO consider create attribute node first and pass it to beforeSetAttribute
        beforeSetAttribute(name, value);
        AttributeImpl attr = getOwnerDocument().createAttribute(name);
        attr.setValue(value);
//        return withTransaction(() -> {
        // TODO check if new value equals to old value to skip unnecessary update.
        setDirty(true);
        Attribute oldAttr = attributes.put(attr);
        String oldValue = oldAttr == null ? null : oldAttr.getValue();
        afterSetAttribute(name, value, oldValue);
        return oldValue;
//        });
    }

    @Override
    public String removeAttribute(String name) {
        if (!hasAttribute(name)) {
            return null;
        }
//        return withTransaction(() -> {
        beforeRemoveAttribute(name);
        setDirty(true);
        AttributeImpl attr = attributes.remove(name);
        String removedValue = attr == null ? null : attr.getValue();
        afterRemoveAttribute(name, removedValue);
        return removedValue;
//        });
    }

    @Override
    public Collection<AttributeImpl> getAttributes() {
        return attributes.all();
    }

    protected void beforeSetAttribute(String name, String value) {
        // override this method to hook setAttribute
    }

    protected void afterSetAttribute(String name, String value, String oldValue) {
        // override this method to hook setAttribute
    }

    protected void beforeRemoveAttribute(String name) {
        // override this method to hook removeAttribute
    }

    protected void afterRemoveAttribute(String name, String value) {
        // override this method to hook removeAttribute
    }

//    // FIXME @Override
//    ElementSnapshot snapshot() {
//        Collection<AttributeSnapshotImpl> attrs = new ArrayList<>(attributes.size());
//        for (AttributeImpl attr : attributes) {
//            attrs.add(attr.snapshot());
//        }
//        List<NodeSnapshot> children = new ArrayList<>(getChildCount());
//        forEachChild(c -> children.add(c.snapshot()));
//        return new ElementSnapshotImpl(getTagName(), attrs, children);
//    }
}
