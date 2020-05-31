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

import java.util.Objects;

public final class AttributeImpl extends AbstractNode implements Attribute {
    private final String name;
    private String value;

    public AttributeImpl(String name) {
        super(null);
        this.name = name;
    }

    public AttributeImpl(String name, String value) {
        super(null);
        this.name = name;
        this.value = value;
    }

    @Override
    public String getNodeName() {
        return name;
    }

    @Override
    public String getTextContent() {
        return getValue();
    }

    @Override
    public void setTextContent(String textContent) {
        setValue(textContent);
    }

    @Override
    public String getNodeValue() {
        return getValue();
    }

    @Override
    public void setNodeValue(String nodeValue) {
        setValue(nodeValue);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        if (!Objects.equals(this.value, value)) {
            setDirty(true);
            this.value = value;
        }
    }

//    // FIXME @Override
//    AttributeSnapshotImpl snapshot() {
//        return new AttributeSnapshotImpl(getName(), getValue());
//    }
}
