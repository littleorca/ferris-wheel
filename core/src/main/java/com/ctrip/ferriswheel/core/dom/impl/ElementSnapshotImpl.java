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

package com.ctrip.ferriswheel.core.dom.impl;

import com.ctrip.ferriswheel.core.dom.AttributeSnapshot;
import com.ctrip.ferriswheel.core.dom.ElementSnapshot;
import com.ctrip.ferriswheel.core.dom.NodeSnapshot;

import java.util.*;

public class ElementSnapshotImpl extends AbstractNodeSnapshot implements ElementSnapshot {
    private final String tagName;
    private final Collection<AttributeSnapshot> attributes;
    private final List<NodeSnapshot> children;

    public ElementSnapshotImpl(String tagName,
                               Collection<AttributeSnapshot> attributes,
                               List<NodeSnapshot> children,
                               ElementSnapshot previousSnapshot) {
        super(previousSnapshot);
        this.tagName = tagName;
        this.attributes = attributes;
        this.children = children;
    }

    @Override
    public String getTagName() {
        return tagName;
    }

    @Override
    public Collection<AttributeSnapshot> getAttributes() {
        return attributes == null ? Collections.emptyList() : attributes;
    }

    @Override
    public List<NodeSnapshot> getChildren() {
        return children == null ? Collections.emptyList() : children;
    }

    @Override
    public ElementSnapshot getPreviousSnapshot() {
        return (ElementSnapshot) super.getPreviousSnapshot();
    }

    @Override
    public ElementSnapshot getOriginalSnapshot() {
        return (ElementSnapshot) super.getOriginalSnapshot();
    }

    @Override
    public ElementSnapshotImpl duplicate(boolean linked) {
        return new ElementSnapshotImpl(tagName,
                new ArrayList<>(getAttributes()),
                new ArrayList<>(getChildren()),
                linked ? this : null);
    }

    @Override
    protected void buildStringDescriptor(StringBuilder sb, String linePrefix) {
        super.buildStringDescriptor(sb, linePrefix);

        if (linePrefix.length() >= 2) {
            String basePrefix = linePrefix.substring(0, linePrefix.length() - 2);
            if (linePrefix.endsWith("|-")) {
                linePrefix = basePrefix + "| ";
            } else if (linePrefix.endsWith("`-")) {
                linePrefix = basePrefix + "  ";
            }
        }

        List<NodeSnapshot> list = getChildren();
        for (int i = 0; i < list.size(); i++) {
            String childLinePrefix = (i == list.size() - 1) ?
                    linePrefix + " `-" : linePrefix + " |-";
            AbstractNodeSnapshot child = (AbstractNodeSnapshot) list.get(i);
            child.buildStringDescriptor(sb, childLinePrefix);
        }
    }

    @Override
    protected String toSingleLineString() {
        StringBuilder sb = new StringBuilder("<").append(getTagName());
        Collection<AttributeSnapshot> attrs = getAttributes();
        for (AttributeSnapshot attr : attrs) {
            sb.append(" ").append(((AttributeSnapshotImpl) attr).toSingleLineString());
        }
        return sb.append(">").toString();
    }

    public static class Builder extends AbstractNodeSnapshot.Builder {
        private String tagName;
        private Map<String, String> attributes = new HashMap<>();
        private List<AbstractNodeSnapshot.Builder> children = new LinkedList<>();

        public Builder setTagName(String tagName) {
            if (this.tagName != tagName) {
                this.tagName = tagName;
                markDirty();
            }
            return this;
        }

        public String getTagName() {
            return tagName;
        }

        public Builder setAttribute(String name, String value) {
            attributes.put(name, value);
            markDirty();
            return this;
        }

        public String getAttribute(String name) {
            return attributes.get(name);
        }

//        public Map<String, String> getAttributes() {
//            return attributes;
//        }

        public Builder removeAttribute(String name) {
            if (attributes.remove(name) != null) {
                markDirty();
            }
            return this;
        }

        public Builder clearAttributes() {
            if (!attributes.isEmpty()) {
                attributes.clear();
                markDirty();
            }
            return this;
        }
//
//        public Builder addChild(NodeSnapshot child) {
//            children.add(child);
//            markDirty();
//            return this;
//        }
//
//        public Builder addChild(int index, NodeSnapshot child) {
//            children.add(index, child);
//            markDirty();
//            return this;
//        }

        public Builder addChild(AbstractNodeSnapshot.Builder child) {
            children.add(child);
            child.setParent(this);
            markDirty();
            return this;
        }

        public Builder addChild(int index, AbstractNodeSnapshot.Builder child) {
            children.add(index, child);
            child.setParent(this);
            markDirty();
            return this;
        }

        public int getChildCount() {
            return children.size();
        }

        public AbstractNodeSnapshot.Builder getChild(int index) {
            return children.get(index);
        }

//        public List<Object> getChildren() {
//            return children;
//        }

        public Builder removeChild(int index) {
            AbstractNodeSnapshot.Builder child = children.remove(index);
            child.setParent(null);
            markDirty();
            return this;
        }

        public Builder removeChild(AbstractNodeSnapshot.Builder child) {
            if (children.remove(child)) {
                child.setParent(null);
                markDirty();
            }
            return this;
        }

        public Builder clearChildren() {
            if (!children.isEmpty()) {
                for (AbstractNodeSnapshot.Builder child : children) {
                    child.setParent(null);
                }
                children.clear();
                markDirty();
            }
            return this;
        }

        public Builder setPreviousNode(ElementSnapshot previousNode) {
            return (Builder) super.setPreviousNode(previousNode);
        }

        public ElementSnapshot getPreviousNode() {
            return (ElementSnapshot) super.getPreviousNode();
        }

        @Override
        public ElementSnapshotImpl build() {
            if (!isDirty() && getLatestBuild() != null) {
                return (ElementSnapshotImpl) getLatestBuild();
            }
            List<AttributeSnapshot> attrList = new ArrayList<>(attributes.size());
            for (Map.Entry<String, String> attr : attributes.entrySet()) {
                attrList.add(new AttributeSnapshotImpl(attr.getKey(), attr.getValue(), null));
            }
            List<NodeSnapshot> childList = new ArrayList<>(children.size());
            for (AbstractNodeSnapshot.Builder child : children) {
                childList.add(child.build());
            }
            ElementSnapshotImpl node = new ElementSnapshotImpl(tagName,
                    Collections.unmodifiableList(attrList),
                    Collections.unmodifiableList(childList),
                    getPreviousNode());
            setPreviousNode(null);
            setLatestBuild(node);
            setDirty(false);
            return node;
        }
    }
}
