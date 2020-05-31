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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ElementSnapshotImpl extends AbstractNodeSnapshot implements ElementSnapshot {
    private final String tagName;
    private final Collection<AttributeSnapshot> attributes;
    private final List<? extends NodeSnapshot> children;

    public ElementSnapshotImpl(String tagName,
                               Collection<AttributeSnapshot> attributes,
                               List<? extends NodeSnapshot> children,
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
    public boolean hasAttribute(String name) {
        // TODO review
        for (AttributeSnapshot a : getAttributes()) {
            if (a.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getAttribute(String name) {
        // TODO review
        for (AttributeSnapshot a : getAttributes()) {
            if (a.getName().equals(name)) {
                return a.getValue();
            }
        }
        return null;
    }

    @Override
    public String getTextContent() {
        return null;//TODO
    }

    @Override
    public int getChildCount() {
        return getChildren().size();
    }

    @Override
    public List<? extends NodeSnapshot> getChildren() {
        return children == null ? Collections.emptyList() : children;
    }

    @Override
    public NodeSnapshot getChild(int index) {
        return getChildren().get(index);
    }

    @Override
    public NodeSnapshot getChild(String name) {
        for (NodeSnapshot child : getChildren()) {
            if (child.getNodeName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    @Override
    public NodeSnapshot firstChild() {
        return children == null ? null : children.get(0);
    }

    @Override
    public NodeSnapshot lastChild() {
        return children == null ? null : children.get(children.size() - 1);
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

        List<? extends NodeSnapshot> list = getChildren();
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
}
