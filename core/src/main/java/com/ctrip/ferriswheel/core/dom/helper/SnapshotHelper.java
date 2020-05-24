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

import com.ctrip.ferriswheel.core.dom.*;

public class SnapshotHelper {

    public static NodeSnapshot snapshotNode(Node n, NodeRevisionTracer nodeTracer) {
        NodeSnapshot previousSnapshot = nodeTracer.getSnapshot(n);

        if (!n.isDirty() && previousSnapshot != null) {
            return previousSnapshot;
        }

        NodeSnapshot newSnapshot;

        switch (n.getNodeType()) {
            case ELEMENT_NODE:
                newSnapshot = snapshotElement((Element) n, nodeTracer);
                break;
            case TEXT_NODE:
                newSnapshot = snapshotText((TextNode) n, (TextNodeSnapshot) previousSnapshot);
                break;
            default:
                throw new IllegalArgumentException();
        }

        nodeTracer.setSnapshot(n, newSnapshot);
        return newSnapshot;
    }

    static ElementSnapshot snapshotElement(Element element,
                                           NodeRevisionTracer nodeTracer) {
        ElementSnapshotBuilder builder = new ElementSnapshotBuilder();
        builder.setPreviousSnapshot(nodeTracer.getSnapshot(element));
        builder.setTagName(element.getTagName());

        for (Attribute attr : element.getAttributes()) {
            builder.setAttribute(attr.getName(), attr.getValue());
        }
        for (int i = 0; i < element.getChildCount(); i++) {
            Node child = element.getChild(i);
            builder.addChild(i, snapshotNode(child, nodeTracer));
        }
        return builder.build();
    }

    static TextNodeSnapshot snapshotText(TextNode textNode, TextNodeSnapshot previousSnapshot) {
        TextNodeSnapshotBuilder builder = new TextNodeSnapshotBuilder();
        builder.setPreviousSnapshot(previousSnapshot);
        builder.setData(textNode.getData());
        return builder.build();
    }

}
