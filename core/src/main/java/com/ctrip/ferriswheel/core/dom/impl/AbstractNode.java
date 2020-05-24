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
import com.ctrip.ferriswheel.core.dom.NodeWrapper;

import java.io.Serializable;

public abstract class AbstractNode implements Node, Serializable {
    private AbstractDocument ownerDocument;
    private AbstractNode parentNode;
    private boolean dirty = false;

    protected AbstractNode() {
    }

    protected AbstractNode(AbstractDocument ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

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

    protected AbstractNode toAbstractNode(Node node) {
        if (node == null) {
            return null;
        }

        if (node instanceof NodeWrapper) {
            node = ((NodeWrapper) node).expose();
        }
        if (node instanceof AbstractNode) {
            return (AbstractNode) node;
        }

        throw new ClassCastException("Cannot cast Node to AbstractNode, node=" + node.getClass());
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        if (this.dirty == dirty) {
            return;
        }

        this.dirty = dirty;

        if (!dirty) { // mark all descendant nodes clean
            for (int i = 0; i < getChildCount(); i++) {
                getChild(i).setDirty(false);
            }

        } else if (getParentNode() != null) { // mark all ascendant nodes dirty
            getParentNode().setDirty(true);
        }
    }
}
