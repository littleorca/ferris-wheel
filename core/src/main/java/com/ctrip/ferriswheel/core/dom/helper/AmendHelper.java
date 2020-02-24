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
import com.ctrip.ferriswheel.core.dom.TextNodeSnapshot;
import com.ctrip.ferriswheel.core.dom.diff.AttributeDiff;
import com.ctrip.ferriswheel.core.dom.diff.ElementDiff;
import com.ctrip.ferriswheel.core.dom.diff.NodeLocation;

import java.util.*;

public class AmendHelper {

    AmendHelper() {
    }

    public ChangeCollector diff(NodeSnapshot negativeNode,
                                NodeSnapshot positiveNode) {
        return diff(null, negativeNode, positiveNode);
    }

    public ChangeCollector diff(ChangeCollector changeCollector, NodeSnapshot negativeNode, NodeSnapshot positiveNode) {
        if (changeCollector == null) {
            changeCollector = new ChangeCollectorImpl();
        }

        DiffContext context = new DiffContext();
        context.collector = changeCollector;
        context.negativeIndexer = new TreeIndexer(negativeNode);
        context.positiveLocation = new NodeLocation(0);

        diffNode(context, negativeNode, positiveNode);
        return changeCollector;
    }

    void diffNode(DiffContext context, NodeSnapshot negativeNode, NodeSnapshot positiveNode) {
        if (negativeNode == null && positiveNode == null) {
            throw new IllegalArgumentException("Negative node and positive node cannot be both null, " +
                    "this is probably a bug.");

        } else if ((negativeNode == null || negativeNode instanceof ElementSnapshot) &&
                (positiveNode == null || positiveNode instanceof ElementSnapshot)) {
            diffElement(context, (ElementSnapshot) negativeNode, (ElementSnapshot) positiveNode);

        } else if ((negativeNode == null || negativeNode instanceof TextNodeSnapshot) &&
                (positiveNode == null || positiveNode instanceof TextNodeSnapshot)) {
            diffTextNode(context, (TextNodeSnapshot) negativeNode, (TextNodeSnapshot) positiveNode);

        } else {
            throw new IllegalArgumentException("Negative node and positive node are not comparable, " +
                    "this is probably a bug.");
        }
    }

    void diffElement(DiffContext context, ElementSnapshot negativeElement, ElementSnapshot positiveElement) {
        if (!(positiveElement instanceof ElementSnapshot)) {
            throw new RuntimeException("Node type not match, probably a bug.");
        }

        ElementDiff elementDiff = new ElementDiff();
        diffAttributes(elementDiff, negativeElement, positiveElement);

        if (negativeElement != null) {
            elementDiff.setNegativeLocation(context.negativeIndexer.getTreeLocation(negativeElement));
        }
        if (positiveElement != null) {
            elementDiff.setPositiveLocation(context.positiveLocation);
        }

        diffChildList(context, negativeElement, positiveElement);

        diffChildItems(context, positiveElement);
    }

    void diffTextNode(DiffContext context, TextNodeSnapshot negativeTextNode, TextNodeSnapshot positiveTextNode) {
// TODO line diff
    }

    void diffAttributes(ElementDiff elementDiff, ElementSnapshot negativeElement, ElementSnapshot positiveElement) {
        Collection<? extends AttributeSnapshot> negativeAttrs = negativeElement == null ?
                Collections.emptyList() : negativeElement.getAttributes();
        Collection<? extends AttributeSnapshot> positiveAttrs = positiveElement == null ?
                Collections.emptyList() : positiveElement.getAttributes();
        Map<String, String> negativeAttrMap = new HashMap<>(negativeAttrs.size());
        Map<String, String> positiveAttrMap = new HashMap<>(positiveAttrs.size());
        positiveAttrs.forEach(a -> positiveAttrMap.put(a.getName(), a.getValue()));
        negativeAttrs.forEach(a -> negativeAttrMap.put(a.getName(), a.getValue()));

        ArrayList<AttributeDiff> attrDiffList = new ArrayList<>();
        for (Map.Entry<String, String> negativeEntry : negativeAttrMap.entrySet()) {
            String positiveValue = positiveAttrMap.get(negativeEntry.getKey());
            if (!Objects.equals(negativeEntry.getValue(), positiveValue)) {
                attrDiffList.add(new AttributeDiff(false, negativeEntry.getKey(), negativeEntry.getValue()));
            }
        }
        for (Map.Entry<String, String> positiveEntry : positiveAttrMap.entrySet()) {
            String negativeValue = negativeAttrMap.get(positiveEntry.getKey());
            if (!Objects.equals(positiveEntry.getValue(), negativeValue)) {
                attrDiffList.add(new AttributeDiff(true, positiveEntry.getKey(), positiveEntry.getValue()));
            }
        }
        elementDiff.setAttributes(attrDiffList);
    }

    void diffChildList(DiffContext context, ElementSnapshot negativeElement, ElementSnapshot positiveElement) {
        ChangeCollector changeCollector = context.collector;
        TreeIndexer negativeIndexer = context.negativeIndexer;
        NodeLocation positiveLocation = context.positiveLocation;

        List<? extends NodeSnapshot> negativeChildren = negativeElement.getChildren();
        List<? extends NodeSnapshot> positiveChildren = positiveElement.getChildren();

        int[][] matrix = new int[negativeChildren.size() + 1][positiveChildren.size() + 1];

        for (int i = 0; i <= negativeChildren.size(); i++) {
            for (int j = 0; j <= positiveChildren.size(); j++) {
                if (i == 0) {
                    matrix[i][j] = j;
                } else if (j == 0) {
                    matrix[i][j] = i;
                } else {
                    int min = Math.min(matrix[i - 1][j], matrix[i][j - 1]);
                    min = Math.min(min, matrix[i - 1][j - 1]);
                    NodeSnapshot negativeChild = negativeChildren.get(i - 1);
                    NodeSnapshot positiveChild = positiveChildren.get(j - 1);
                    matrix[i][j] = isSameNode(negativeChild, positiveChild) ? min : min + 1;
                }
            }
        }

        int i = negativeChildren.size();
        int j = positiveChildren.size();
        int current = matrix[i][j];

        while (i > 0 && j > 0) {
            int corner = matrix[i - 1][j - 1];
            int upper = matrix[i - 1][j];
            int left = matrix[i][j - 1];

            if (corner <= upper && corner <= left) {
                i--;
                j--;

                if (current != corner) {
                    changeCollector.add(new ElementDiff(null, new NodeLocation(positiveLocation, j)));

                    NodeLocation negativeLocation = negativeIndexer.getTreeLocation(negativeChildren.get(i));
                    changeCollector.add(new ElementDiff(negativeLocation, null));
                    current = corner;
                }

            } else if (upper <= left) {
                i--;
                if (current != upper) {
                    NodeLocation negativeLocation = negativeIndexer.getTreeLocation(negativeChildren.get(i));
                    changeCollector.add(new ElementDiff(negativeLocation, null));
                    current = upper;
                }
            } else {
                j--;
                if (current != left) {
                    changeCollector.add(new ElementDiff(null, new NodeLocation(positiveLocation, j)));
                    current = left;
                }
            }
        }
        while (i > 0) {
            i--;
            NodeLocation negativeLocation = negativeIndexer.getTreeLocation(negativeChildren.get(i));
            changeCollector.add(new ElementDiff(negativeLocation, null));
        }
        while (j > 0) {
            j--;
            changeCollector.add(new ElementDiff(null, new NodeLocation(positiveLocation, j)));
        }
    }

    boolean isSameNode(NodeSnapshot nodeA, NodeSnapshot nodeB) {
        if (nodeA == nodeB) {
            return true;
        }
        return nodeA.getOriginalSnapshot() == nodeB.getOriginalSnapshot();
    }

    private void diffChildItems(DiffContext context, ElementSnapshot element) {
        List<? extends NodeSnapshot> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            NodeSnapshot child = children.get(i);
            NodeSnapshot negativeChild = context.negativeIndexer.getTreeNode(child);
            diffNode(context, negativeChild, child);
        }
    }

    static class DiffContext {
        ChangeCollector collector;
        TreeIndexer negativeIndexer;
        NodeLocation positiveLocation;
    }

    static class TreeIndexer {
        private NodeSnapshot rootNode;
        private NodeLocation rootLocation;
        private Map<NodeSnapshot, NodeSnapshot> originalToTreeNode = new HashMap<>();
        private Map<NodeSnapshot, NodeLocation> originalToTreeLocation = new HashMap<>();

        TreeIndexer(NodeSnapshot startNode) {
            this(startNode, new NodeLocation(new int[]{0}));
        }

        TreeIndexer(NodeSnapshot startNode, NodeLocation startLocation) {
            this.rootNode = startNode;
            this.rootLocation = startLocation;

            NodeSnapshot originalOfStartNode = startNode.getOriginalSnapshot();
            originalToTreeNode.put(originalOfStartNode, startNode);
            originalToTreeLocation.put(originalOfStartNode, startLocation);

            if (!(startNode instanceof ElementSnapshot)) {
                return;
            }

            Stack<ElementSnapshot> stack = new Stack<>();
            stack.push((ElementSnapshot) startNode);

            while (!stack.isEmpty()) {
                ElementSnapshot parentElem = stack.pop();
                ElementSnapshot parentOriginal = parentElem.getOriginalSnapshot();
                NodeLocation parentLocation = originalToTreeLocation.get(parentOriginal);

                List<? extends NodeSnapshot> childList = parentElem.getChildren();
                for (int i = 0; i < childList.size(); i++) {
                    NodeSnapshot child = childList.get(i);
                    NodeSnapshot childOriginal = child.getOriginalSnapshot();
                    originalToTreeNode.put(childOriginal, child);
                    originalToTreeLocation.put(childOriginal, new NodeLocation(parentLocation, i));
                    if (child instanceof ElementSnapshot) {
                        stack.push((ElementSnapshot) child);
                    }
                }
            }
        }

        NodeSnapshot getTreeNodeByOriginal(NodeSnapshot original) {
            return originalToTreeNode.get(original);
        }

        NodeSnapshot getTreeNode(NodeSnapshot node) {
            return originalToTreeNode.get(node.getOriginalSnapshot());
        }

        NodeLocation getTreeLocationByOriginal(NodeSnapshot original) {
            return originalToTreeLocation.get(original);
        }

        NodeLocation getTreeLocation(NodeSnapshot node) {
            return originalToTreeLocation.get(node.getOriginalSnapshot());
        }
    }
}
