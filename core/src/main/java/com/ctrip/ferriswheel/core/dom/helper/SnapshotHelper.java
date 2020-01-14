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
import com.ctrip.ferriswheel.core.dom.impl.AttributeSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.TextNodeSnapshotImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class SnapshotHelper {
    public static NodeSnapshot snapshotTree(Node root, NodeSnapshotMapper mapper) {
        Stack<Node> pendingNodes = new Stack<>();
        pendingNodes.push(root);
        NodeSnapshot snapshot = null;
        while (!pendingNodes.isEmpty()) {
            Node n = pendingNodes.peek();
            boolean clean = true;
            if (n instanceof Element) {
                for (Attribute attr : ((Element) n).getAttributes()) {
                    if (attr.isDirty()) {
                        clean = false;
                        pendingNodes.push(attr);
                    }
                }
            }
            for (int i = 0; i < n.getChildCount(); i++) {
                Node c = n.getChild(i);
                if (c.isDirty()) {
                    clean = false;
                    pendingNodes.push(c);
                }
            }
            if (clean) {
                pendingNodes.pop();
                snapshot = snapshotSingleNode(n, mapper);
                n.setDirty(false);
            }
        }
        return snapshot;
    }

    private static NodeSnapshot snapshotSingleNode(Node n, NodeSnapshotMapper mapper) {
        NodeSnapshot previousSnapshot = mapper.map(n);
        NodeSnapshot newSnapshot;
        if (n instanceof Attribute) {
            newSnapshot = new AttributeSnapshotImpl((Attribute) n, (AttributeSnapshot) previousSnapshot);

        } else if (n instanceof TextNode) {
            newSnapshot = new TextNodeSnapshotImpl((TextNode) n, (TextNodeSnapshot) previousSnapshot);

        } else if (n instanceof Element) {
            Collection<? extends Attribute> attrs = ((Element) n).getAttributes();
            List<AttributeSnapshot> attrSnapshots = new ArrayList<>(attrs.size());
            for (Attribute attr : attrs) {
                attrSnapshots.add((AttributeSnapshot) mapper.map(attr));
            }

            List<NodeSnapshot> childSnapshots = new ArrayList<>(n.getChildCount());
            for (int i = 0; i < n.getChildCount(); i++) {
                childSnapshots.add(mapper.map(n.getChild(i)));
            }

            newSnapshot = new ElementSnapshotImpl(((Element) n).getTagName(),
                    attrSnapshots,
                    childSnapshots,
                    (ElementSnapshot) previousSnapshot);

        } else {
            throw new RuntimeException("Unsupported node: " + n + ", probably a bug.");
        }

        mapper.map(n, newSnapshot);
        return newSnapshot;
    }

}
