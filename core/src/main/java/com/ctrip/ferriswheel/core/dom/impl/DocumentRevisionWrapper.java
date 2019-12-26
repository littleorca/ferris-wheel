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
import com.ctrip.ferriswheel.core.dom.Document;
import com.ctrip.ferriswheel.core.dom.Element;
import com.ctrip.ferriswheel.core.dom.TextNode;

public class DocumentRevisionWrapper extends NodeRevisionWrapper<Document> implements Document {
    public DocumentRevisionWrapper(Document document) {
        super(document);
    }

    @Override
    public Element getDocumentElement() {
        return getWrappedNode().getDocumentElement();
    }

    @Override
    public Element createElement(String tagName) {
        return getWrappedNode().createElement(tagName);
    }

    @Override
    public <E extends Element> E createElement(Class<E> elementClass) {
        return getWrappedNode().createElement(elementClass);
    }

    @Override
    public Attribute createAttribute(String name) {
        return getWrappedNode().createAttribute(name);
    }

    @Override
    public TextNode createTextNode(String textContent) {
        return getWrappedNode().createTextNode(textContent);
    }
}
