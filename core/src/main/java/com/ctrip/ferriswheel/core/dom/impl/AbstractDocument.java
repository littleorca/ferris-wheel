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

import com.ctrip.ferriswheel.core.dom.*;

public abstract class AbstractDocument extends AbstractContainerNode implements Document {
    public static final String DOCUMENT_NODE_NAME = "#document";
    private Element documentElement;

    protected AbstractDocument() {
        super(null);
        this.documentElement = createDocumentElement();
        super.appendChild(documentElement);
    }

    protected AbstractDocument(ElementEssential documentElementEssential) {
        super(null);
        this.documentElement = createDocumentElement(documentElementEssential);
        super.appendChild(documentElement);
    }

    @Override
    public String getNodeName() {
        return DOCUMENT_NODE_NAME;
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
        return documentElement;
    }

    protected void setDocumentElement(Element documentElement) {
        if (!super.removeChild(this.documentElement)) {
            throw new RuntimeException();
        }
        this.documentElement = documentElement;
        super.appendChild(documentElement);
    }

    protected abstract Element createDocumentElement();

    protected Element createDocumentElement(ElementEssential elementEssential) {
        return createElement(elementEssential);
    }

    protected Node createNode(NodeEssential nodeEssential) {
        if (nodeEssential.getNodeType() == NodeType.ELEMENT_NODE) {
            return createElement((ElementEssential) nodeEssential);

        } else if (nodeEssential.getNodeType() == NodeType.TEXT_NODE) {
            return createTextNode((TextNodeEssential) nodeEssential);
        } else {
            throw new RuntimeException("Unsupported node type: " + nodeEssential.getNodeType());
        }
    }

    protected Element createElement(ElementEssential elementEssential) {
        Element element = createElement(elementEssential.getNodeName());
        for (AttributeEssential attr : elementEssential.getAttributes()) {
            element.setAttribute(attr.getName(), attr.getValue());
        }
        for (int i = 0; i < elementEssential.getChildCount(); i++) {
            element.appendChild(createNode(elementEssential.getChild(i)));
        }
        return element;
    }

    protected TextNode createTextNode(TextNodeEssential textNodeEssential) {
        return createTextNode(textNodeEssential.getNodeValue());
    }

    @Override
    public AttributeImpl createAttribute(String name) {
        AttributeImpl attrNode = new AttributeImpl(name);
        return attrNode;
    }

    @Override
    public TextNodeImpl createTextNode(String textContent) {
        TextNodeImpl textNode = new TextNodeImpl(this, textContent);
        return textNode;
    }

}
