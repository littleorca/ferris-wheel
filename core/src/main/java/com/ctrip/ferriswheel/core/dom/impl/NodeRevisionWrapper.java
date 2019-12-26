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
import com.ctrip.ferriswheel.core.dom.Node;
import com.ctrip.ferriswheel.core.dom.VersionControllable;

public class NodeRevisionWrapper<T extends Node> implements Node, VersionControllable {
    private T node;
    private long revision;

    public NodeRevisionWrapper(T node) {
        this.node = node;
    }

    protected T getWrappedNode() {
        return node;
    }

    @Override
    public Document getOwnerDocument() {
        return node.getOwnerDocument();
    }

    @Override
    public Node getRootNode() {
        return node.getRootNode();
    }

    @Override
    public Node getParentNode() {
        return node.getParentNode();
    }

    @Override
    public Node previousSibling() {
        return node.previousSibling();
    }

    @Override
    public Node nextSibling() {
        return node.nextSibling();
    }

    @Override
    public String getNodeName() {
        return node.getNodeName();
    }

    @Override
    public String getTextContent() {
        return node.getTextContent();
    }

    @Override
    public void setTextContent(String textContent) {
        node.setTextContent(textContent);
    }

    @Override
    public String getNodeValue() {
        return node.getNodeValue();
    }

    @Override
    public void setNodeValue(String nodeValue) {
        node.setNodeValue(nodeValue);
    }

    @Override
    public boolean hasChildNodes() {
        return node.hasChildNodes();
    }

    @Override
    public boolean contains(Node otherNode) {
        return node.contains(otherNode);
    }

    @Override
    public int getChildCount() {
        return node.getChildCount();
    }

    @Override
    public Node getChild(int index) {
        return node.getChild(index);
    }

    @Override
    public Node getChild(String name) {
        return node.getChild(name);
    }

    @Override
    public Node firstChild() {
        return node.firstChild();
    }

    @Override
    public Node lastChild() {
        return node.lastChild();
    }

    @Override
    public void insertChild(Node child, Node ref) {
        node.insertChild(child, ref);
    }

    @Override
    public void appendChild(Node child) {
        node.appendChild(child);
    }

    @Override
    public boolean removeChild(Node child) {
        return node.removeChild(child);
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) {
        return node.replaceChild(newChild, oldChild);
    }

    @Override
    public long getRevision() {
        return revision;
    }

    protected void setRevision(long revision) {
        this.revision = revision;
    }
}
