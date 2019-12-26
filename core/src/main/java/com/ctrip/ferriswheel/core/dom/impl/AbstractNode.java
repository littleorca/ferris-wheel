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

import com.ctrip.ferriswheel.core.dom.Node;

import java.io.Serializable;

public abstract class AbstractNode implements Node, Serializable {
    private transient AbstractDocument ownerDocument;
    private transient AbstractNode parentNode;

    void initialize(AbstractDocument document) {
        if (ownerDocument != null) {
            throw new IllegalStateException("This node is probably initialized already!");
        }
        setOwnerDocument(document);
    }

    @Override
    public AbstractDocument getOwnerDocument() {
        return ownerDocument;
    }

    private void setOwnerDocument(AbstractDocument document) {
        this.ownerDocument = document;
    }

    @Override
    public AbstractNode getRootNode() {
        AbstractNode rootNode = this;
        while (rootNode.getParentNode() != null) {
            rootNode = rootNode.getParentNode();
        }
        return rootNode;
    }

    @Override
    public AbstractNode getParentNode() {
        return parentNode;
    }

    protected void setParentNode(AbstractNode parentNode) {
        this.parentNode = parentNode;
    }

    @Override
    public boolean hasChildNodes() {
        return false;
    }

    @Override
    public boolean contains(Node otherNode) {
        return false;
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
    public Node replaceChild(Node newChild, Node oldChild) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public AbstractNode getChild(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractNode getChild(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractNode previousSibling() {
        return null;
    }

    @Override
    public AbstractNode nextSibling() {
        return null;
    }

    @Override
    public AbstractNode firstChild() {
        return null;
    }

    @Override
    public AbstractNode lastChild() {
        return null;
    }

}
