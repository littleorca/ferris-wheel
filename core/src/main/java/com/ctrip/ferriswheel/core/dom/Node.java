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

package com.ctrip.ferriswheel.core.dom;

public interface Node extends NodeEssential {

    Document getOwnerDocument();

    Node getRootNode();

    Node getParentNode();

    Node previousSibling();

    Node nextSibling();

    void setTextContent(String textContent);

    void setNodeValue(String nodeValue);

    @Override
    default Node getChild(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Node getChild(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    default Node firstChild() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Node lastChild() {
        throw new UnsupportedOperationException();
    }

    default void insertChild(Node child, Node ref) {
        throw new UnsupportedOperationException();
    }

    default void appendChild(Node child) {
        throw new UnsupportedOperationException();
    }

    default boolean removeChild(Node child) {
        throw new UnsupportedOperationException();
    }

    default Node replaceChild(Node newChild, Node oldChild) {
        throw new UnsupportedOperationException();
    }

    boolean isDirty();

    void setDirty(boolean dirty);
}
