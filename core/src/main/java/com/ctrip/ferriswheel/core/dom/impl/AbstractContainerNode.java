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

import com.ctrip.ferriswheel.core.dom.ContainerNode;
import com.ctrip.ferriswheel.core.dom.Node;
import com.ctrip.ferriswheel.core.dom.helper.NodeList;
import com.ctrip.ferriswheel.core.dom.helper.WithTransaction;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractContainerNode extends AbstractNode implements ContainerNode {
    private NodeList<AbstractNode> children = new NodeList<>();

    @WithTransaction
    @Override
    public void insertChild(Node child, Node ref) {
        if (child.getOwnerDocument() != getOwnerDocument()) {
            throw new IllegalArgumentException("Given child node is not create by the same document.");
        }
        if (ref != null && ref.getOwnerDocument() != getOwnerDocument()) {
            throw new IllegalArgumentException("Invalid referrer node.");
        }
        applyInsertChild(toAbstractNode(child), toAbstractNode(ref));
    }

    protected void applyInsertChild(AbstractNode child, AbstractNode ref) {
        beforeInsertChild(child, ref);
        doInsertChild(child, ref);
        afterInsertChild(child, ref);
    }

    protected void beforeInsertChild(AbstractNode child, AbstractNode ref) {
        // overridable
    }

    protected void doInsertChild(AbstractNode child, AbstractNode ref) {
        children.insertBefore(child, ref);
        adoptChild(child);
    }

    protected void afterInsertChild(AbstractNode child, AbstractNode ref) {
        // overridable
    }

    @WithTransaction
    @Override
    public void appendChild(Node child) {
        if (child.getOwnerDocument() != getOwnerDocument()) {
            throw new IllegalArgumentException("Given child node is not create by the same document.");
        }
        applyAppendChild(toAbstractNode(child));
    }

    protected void applyAppendChild(AbstractNode child) {
        beforeAppendChild(child);
        doAppendChild(child);
        afterAppendChild(child);
    }

    protected void beforeAppendChild(AbstractNode child) {
        // overridable
    }

    private void doAppendChild(AbstractNode child) {
        children.append(child);
        adoptChild(child);
    }

    protected void afterAppendChild(AbstractNode child) {
        // overridable
    }

    private void adoptChild(AbstractNode child) {
        child.setParentNode(this);
    }

    @WithTransaction
    @Override
    public boolean removeChild(Node child) {
        if (child.getOwnerDocument() != getOwnerDocument()) {
            throw new IllegalArgumentException("Given child node is not create by the same document.");
        }
        AbstractNode castedChild = toAbstractNode(child);
        if (!children.contains(castedChild)) {
            return false;
        }
        applyRemoveChild(castedChild);
        return true;
    }

    protected void applyRemoveChild(AbstractNode child) {
        beforeRemoveChild(child);
        boolean removed = doRemoveChild(child);
        if (!removed) {
            throw new RuntimeException("Failed to remove child.");
        }
        afterRemoveChild(child);
    }

    protected void beforeRemoveChild(AbstractNode child) {
        // purposed to be override
    }

    private boolean doRemoveChild(AbstractNode child) {
        boolean removed = children.remove(child);
        if (removed) {
            disposeChild(child);
        }
        return removed;
    }

    protected void afterRemoveChild(AbstractNode child) {
        // purposed to be override
    }

    private void disposeChild(AbstractNode child) {
        child.setParentNode(null);
    }

    @WithTransaction
    @Override
    public Node replaceChild(Node newChild, Node oldChild) {
        if (newChild.getOwnerDocument() != getOwnerDocument() ||
                oldChild.getOwnerDocument() != getOwnerDocument()) {
            throw new IllegalArgumentException(); // TODO add detailed message
        }
        Node ref = oldChild.nextSibling();
        boolean removed = removeChild(oldChild);
        if (!removed) {
            throw new IllegalArgumentException("Target child not found.");
        }
        insertChild(newChild, ref);
        return oldChild;
    }

    @Override
    public boolean hasChildNodes() {
        return children.size() > 0;
    }

    @Override
    public boolean contains(Node otherNode) {
        if (otherNode.getOwnerDocument() != getOwnerDocument()) {
            return false;
        }
        return children.contains(toAbstractNode(otherNode));
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public AbstractNode getChild(int index) {
        return children.get(index);
    }

    @Override
    public AbstractNode getChild(String name) {
        return children.get(name);
    }

    @Override
    public AbstractNode previousSibling() {
        return null; // TODO
    }

    @Override
    public AbstractNode nextSibling() {
        return null; // TODO
    }

    @Override
    public AbstractNode firstChild() {
        return children.first();
    }

    protected <R> R mapFirstChild(Function<? super AbstractNode, R> mapper) {
        AbstractNode child = firstChild();
        return child == null ? null : mapper.apply(child);
    }

    protected <N> N firstChild(Class<N> clazz) {
        return children.first(clazz);
    }

    protected <N, R> R mapFirstChild(Class<N> clazz,
                                     Function<? super N, R> mapper,
                                     R defaultValue) {
        N node = firstChild(clazz);
        return node == null ? defaultValue : mapper.apply(node);
    }

    protected AbstractNode firstChild(Predicate<? super AbstractNode> predicate) {
        return children.first(predicate);
    }

    protected <R> R mapFirstChild(Predicate<? super AbstractNode> predicate,
                                  Function<? super AbstractNode, R> mapper,
                                  R defaultValue) {
        AbstractNode child = firstChild(predicate);
        return child == null ? defaultValue : mapper.apply(child);
    }

    @Override
    public AbstractNode lastChild() {
        return children.last();
    }

    protected <N> N lastChild(Class<N> clazz) {
        return children.last(clazz);
    }

    protected AbstractNode lastChild(Predicate<? super AbstractNode> predicate) {
        return children.last(predicate);
    }

    protected <N> List<N> filterChildren(Class<N> clazz) {
        return children.filter(clazz);
    }

    protected List<AbstractNode> filterChildren(Predicate<? super AbstractNode> predicate) {
        return children.filter(predicate);
    }

    protected void forEachChild(Consumer<? super AbstractNode> action) {
        children.forEach(action);
    }

//    @Override
//    public Node cloneNode() {
//        return null; // TODO FIXME
//    }
}
