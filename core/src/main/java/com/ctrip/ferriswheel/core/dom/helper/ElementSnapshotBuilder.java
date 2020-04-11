/*
 * MIT License
 *
 * Copyright (c) 2018-2020 Ctrip.com
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

package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.AttributeSnapshot;
import com.ctrip.ferriswheel.core.dom.ElementSnapshot;
import com.ctrip.ferriswheel.core.dom.NodeSnapshot;
import com.ctrip.ferriswheel.core.dom.NodeType;
import com.ctrip.ferriswheel.core.dom.impl.AttributeSnapshotImpl;

import java.util.*;

public class ElementSnapshotBuilder extends AbstractNodeSnapshotBuilder
        implements ElementSnapshot {
    private String tagName;
    private Map<String, String> attributes;
    private List<NodeSnapshot> children;

    public ElementSnapshotBuilder() {
        this.attributes = new HashMap<>();
        this.children = new LinkedList<>();
    }

    /**
     * Create builder based on the specified origin node snapshot. The previous
     * node will be set to the specified origin node.
     *
     * @param origin
     */
    public ElementSnapshotBuilder(ElementSnapshot origin) {
        if (origin instanceof ElementSnapshotBuilder) {
            throw new IllegalArgumentException();
        }
        this.tagName = origin.getTagName();
        Collection<AttributeSnapshot> attrs = origin.getAttributes();
        if (attrs == null) {
            this.attributes = new HashMap<>();
        } else {
            this.attributes = new HashMap<>(attrs.size());
            attrs.forEach(a -> this.attributes.put(a.getName(), a.getValue()));
        }
        setChildren(origin.getChildren());
        setPreviousSnapshot(origin);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ELEMENT_NODE;
    }

    @Override
    public String getNodeName() {
        return getTagName();
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    public ElementSnapshotBuilder setTagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    @Override
    public Collection<AttributeSnapshot> getAttributes() {
        // TODO review this
        List<AttributeSnapshot> attrList = new ArrayList<>(attributes.size());
        attributes.forEach((n, v) -> attrList.add(new AttributeSnapshotImpl(n, v, null)));
        return attrList;
    }

    public ElementSnapshotBuilder setAttribute(String name, String value) {
        this.attributes.put(name, value);
        return this;
    }

    public boolean hasAttr(String name) {
        return this.attributes.containsKey(name);
    }

    public String removeAttr(String name) {
        return this.attributes.remove(name);
    }

    @Override
    public List<NodeSnapshot> getChildren() {
        return children;
    }

    public void setChildren(List<NodeSnapshot> children) {
        this.children = children == null ? new LinkedList<>() : new LinkedList<>(children);
    }

    public ElementSnapshotBuilder addChild(int index, NodeSnapshot child) {
        this.children.add(index, child);
        return this;
    }

    public NodeSnapshot removeChild(int index) {
        if (children == null) {
            throw new IllegalStateException();
        }
        return children.remove(index);
    }

    @Override
    public ElementSnapshot duplicate(boolean linked) {
        return null; // FIXME
    }

    @Override
    public ElementSnapshot getPreviousSnapshot() {
        return (ElementSnapshot) super.getPreviousSnapshot();
    }

    @Override
    public ElementSnapshotBuilder setPreviousSnapshot(NodeSnapshot previousSnapshot) {
        return (ElementSnapshotBuilder) super.setPreviousSnapshot(previousSnapshot);
    }

    @Override
    public ElementSnapshot getOriginalSnapshot() {
        return (ElementSnapshot) super.getOriginalSnapshot();
    }

}
