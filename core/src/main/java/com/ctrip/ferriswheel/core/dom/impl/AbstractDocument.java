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

import com.ctrip.ferriswheel.core.dom.Document;
import com.ctrip.ferriswheel.core.dom.Element;
import com.ctrip.ferriswheel.core.dom.Node;

public abstract class AbstractDocument extends AbstractContainerNode implements Document {
    public static final String NODE_NAME = "#document";
    private transient Element documentElement;

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractDocument getOwnerDocument() {
        return this;
    }

    @Override
    public String getTextContent() {
        return null;
    }

    @Override
    public void setTextContent(String textContent) {
        throw new UnsupportedOperationException();
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
    public void insertChild(Node child, Node ref) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendChild(Node child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeChild(Node child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Element getDocumentElement() {
        if (documentElement == null) {
            documentElement = lastChild(Element.class);
        }
        if (documentElement == null) {
            documentElement = createDocumentElement();
            super.appendChild(documentElement);
        }
        return documentElement;
    }

    protected abstract Element createDocumentElement();

    @Override
    public AttributeImpl createAttribute(String name) {
        AttributeImpl attrNode = new AttributeImpl(name);
        attrNode.initialize(this);
        return attrNode;
    }

    @Override
    public TextNodeImpl createTextNode(String textContent) {
        TextNodeImpl textNode = new TextNodeImpl(textContent);
        textNode.initialize(this);
        return textNode;
    }
}
