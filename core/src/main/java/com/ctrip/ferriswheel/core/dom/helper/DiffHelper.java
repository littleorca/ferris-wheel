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
import com.ctrip.ferriswheel.core.dom.diff.*;
import com.ctrip.ferriswheel.core.util.ListSequenceWrapper;
import com.ctrip.ferriswheel.core.util.MyersDifferenceAnalyzer;
import com.ctrip.ferriswheel.core.util.Sequence;
import com.ctrip.ferriswheel.core.util.SequenceDifferenceAnalyzer;

import java.util.*;

public class DiffHelper {
    private SequenceDifferenceAnalyzer analyzer = new MyersDifferenceAnalyzer();

    /**
     * Analyse the differences between negative root node and positive root node.
     *
     * @param negativeRoot
     * @param positiveRoot
     * @return
     * @see #diff(NodeSnapshot, NodeSnapshot, DiffCollector)
     * @see #diff(NodeSnapshot, NodeLocation, NodeSnapshot, NodeLocation)
     * @see #diff(NodeSnapshot, NodeLocation, NodeSnapshot, NodeLocation, DiffCollector)
     */
    public Patch diff(NodeSnapshot negativeRoot,
                      NodeSnapshot positiveRoot) {
        DefaultDiffCollector collector = new DefaultDiffCollector();
        diff(negativeRoot, positiveRoot, collector);
        return collector.toPatch();
    }

    /**
     * Analyse the differences between negative root node and positive root node.
     *
     * @param negativeRoot
     * @param positiveRoot
     * @param collector
     * @see #diff(NodeSnapshot, NodeSnapshot)
     * @see #diff(NodeSnapshot, NodeLocation, NodeSnapshot, NodeLocation)
     * @see #diff(NodeSnapshot, NodeLocation, NodeSnapshot, NodeLocation, DiffCollector)
     */
    public void diff(NodeSnapshot negativeRoot,
                     NodeSnapshot positiveRoot,
                     DiffCollector collector) {
        diff(negativeRoot, null, positiveRoot, null, collector);
    }

    /**
     * Analyse the differences between negative root node and positive root node.
     *
     * @param negativeRoot
     * @param negativeBaseLocation
     * @param positiveRoot
     * @param positiveBaseLocation
     * @return
     * @see #diff(NodeSnapshot, NodeSnapshot)
     * @see #diff(NodeSnapshot, NodeSnapshot, DiffCollector)
     * @see #diff(NodeSnapshot, NodeLocation, NodeSnapshot, NodeLocation, DiffCollector)
     */
    public Patch diff(NodeSnapshot negativeRoot,
                      NodeLocation negativeBaseLocation,
                      NodeSnapshot positiveRoot,
                      NodeLocation positiveBaseLocation) {
        DefaultDiffCollector collector = new DefaultDiffCollector();
        diff(negativeRoot, negativeBaseLocation, positiveRoot, positiveBaseLocation, collector);
        return collector.toPatch();
    }

    /**
     * Analyse the differences between negative root node and positive root node.
     *
     * @param negativeRoot
     * @param negativeBaseLocation
     * @param positiveRoot
     * @param positiveBaseLocation
     * @param collector
     * @see #diff(NodeSnapshot, NodeSnapshot)
     * @see #diff(NodeSnapshot, NodeSnapshot, DiffCollector)
     * @see #diff(NodeSnapshot, NodeLocation, NodeSnapshot, NodeLocation)
     */
    public void diff(NodeSnapshot negativeRoot,
                     NodeLocation negativeBaseLocation,
                     NodeSnapshot positiveRoot,
                     NodeLocation positiveBaseLocation,
                     DiffCollector collector) {
        if (negativeRoot == null && positiveRoot == null) {
            throw new IllegalArgumentException("Negative node and positive node cannot be both null.");
        }
        if (collector == null) {
            throw new IllegalArgumentException("Collector cannot be both null.");
        }
        if (negativeBaseLocation == null) {
            negativeBaseLocation = new NodeLocation(0);
        }
        if (positiveBaseLocation == null) {
            positiveBaseLocation = new NodeLocation(0);
        }

        DiffContext context = new DiffContext(negativeRoot, negativeBaseLocation,
                positiveRoot, positiveBaseLocation, collector);
        diffNode(context, negativeRoot, positiveRoot);
    }

    void diffNode(DiffContext context, NodeSnapshot negativeNode, NodeSnapshot positiveNode) {
        if (negativeNode == null && positiveNode == null) {
            throw new IllegalArgumentException("Negative node and positive node cannot be both null, " +
                    "this is probably a bug.");
        }

        if (isInstanceOrNull(ElementSnapshot.class, negativeNode, positiveNode)) {
            diffElement(context, (ElementSnapshot) negativeNode, (ElementSnapshot) positiveNode);

        } else if (isInstanceOrNull(TextNodeSnapshot.class, negativeNode, positiveNode)) {
            diffTextNode(context, (TextNodeSnapshot) negativeNode, (TextNodeSnapshot) positiveNode);

        } else {
            throw new IllegalArgumentException("Negative node and positive node are not comparable, " +
                    "this is probably a bug.");
        }
    }

    private boolean isInstanceOrNull(Class<?> clazz, Object... objects) {
        if (objects == null) {
            return true;
        }
        for (Object obj : objects) {
            if (obj != null && !clazz.isInstance(obj)) {
                return false;
            }
        }
        return true;
    }

    void diffElement(DiffContext context, ElementSnapshot negativeElement, ElementSnapshot positiveElement) {
        Collection<AttributeSnapshot> negativeAttrs = negativeElement == null ?
                null : negativeElement.getAttributes();
        Collection<AttributeSnapshot> positiveAttrs = positiveElement == null ?
                null : positiveElement.getAttributes();
        LinkedHashMap<String, String> delMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> addMap = new LinkedHashMap<>();

        diffAttributes(negativeAttrs, positiveAttrs, delMap, addMap);

        if (!delMap.isEmpty() || !addMap.isEmpty()) {
            NodeLocation negativeLocation = null;
            NodeLocation positiveLocation = null;

            if (negativeElement != null) {
                negativeLocation = context.getNegativeLocation(negativeElement);
            }
            if (positiveElement != null) {
                positiveLocation = context.getCurrentLocation();
            }

            ElementDiff elementDiff = createElementDiff(negativeLocation, positiveLocation);
            elementDiff.setNegativeAttributes(delMap);
            elementDiff.setPositiveAttributes(addMap);
            context.collect(elementDiff);
        }

        diffChildList(context, negativeElement, positiveElement);

        diffChildItems(context, positiveElement);
    }

    void diffTextNode(DiffContext context,
                      TextNodeSnapshot negativeTextNode,
                      TextNodeSnapshot positiveTextNode) {
        List<LineDiff> lineDiffs = diffTextByLine(
                negativeTextNode == null ? null : negativeTextNode.getData(),
                positiveTextNode == null ? null : positiveTextNode.getData());

        if (lineDiffs != null && !lineDiffs.isEmpty()) {
            NodeLocation negativeLocation = context.getNegativeLocation(negativeTextNode);
            TextNodeDiff textNodeDiff = new TextNodeDiff(negativeLocation, context.getCurrentLocation());
            textNodeDiff.setLines(lineDiffs);
            context.collect(textNodeDiff);
        }
    }

    List<LineDiff> diffTextByLine(String negative,
                                  String positive) {
        LineArrayWrapper negativeSequence = new LineArrayWrapper(negative);
        LineArrayWrapper positiveSequence = new LineArrayWrapper(positive);
        List<LineDiff> diffList = new LinkedList<>();
        analyzer.analyze(negativeSequence, positiveSequence, null,
                op -> diffList.add(new LineDiff(
                        op,
                        (op > 0 ?
                                positiveSequence.get(op - 1).toString() :
                                negativeSequence.get(-op - 1).toString()
                        )
                )));
        return diffList;
    }

    void diffAttributes(Collection<AttributeSnapshot> negativeAttrs,
                        Collection<AttributeSnapshot> positiveAttrs,
                        Map<String, String> delMap,
                        Map<String, String> addMap) {
        if (negativeAttrs == null) {
            negativeAttrs = Collections.emptyList();
        }
        if (positiveAttrs == null) {
            positiveAttrs = Collections.emptyList();
        }
        negativeAttrs.forEach(a -> delMap.put(a.getName(), a.getValue()));
        positiveAttrs.forEach(a -> {
            if (!delMap.containsKey(a.getName()) ||
                    !Objects.equals(delMap.get(a.getName()), a.getValue())) {
                addMap.put(a.getName(), a.getValue());
            } else {
                delMap.remove(a.getName());
            }
        });
    }

    void diffChildList(DiffContext context, ElementSnapshot negativeElement, ElementSnapshot positiveElement) {
        Sequence<NodeSnapshot> negativeSeq = createChildSequence(negativeElement);
        Sequence<NodeSnapshot> positiveSeq = createChildSequence(positiveElement);
        List<Integer> ops = new LinkedList<>();

        analyzer.analyze(negativeSeq, positiveSeq,
                (a, b) -> (isSameNode(a, b) ? 0 : 1),
                op -> ops.add(op));

        for (int op : ops) {
            NodeSnapshot nChild;
            NodeLocation nLocation;
            NodeSnapshot pChild = null;
            NodeLocation pLocation = null;

            if (op > 0) {
                int childIndex = op - 1;
                pChild = positiveSeq.get(childIndex);
                nChild = context.getNegativeNode(pChild);
                nLocation = context.getNegativeLocation(pChild);
                context.pushLocation(childIndex);
                pLocation = context.getCurrentLocation();
                context.popLocation();

            } else {
                int childIndex = -op - 1;
                nChild = negativeSeq.get(childIndex);
                nLocation = context.getNegativeLocation(nChild);
            }
            Diff diff = createDiff(nChild, nLocation,
                    pChild, pLocation);
            context.collect(diff);
        }
    }

    private Sequence<NodeSnapshot> createChildSequence(ElementSnapshot element) {
        List<NodeSnapshot> list = element == null ?
                Collections.emptyList() : element.getChildren();
        return new ListSequenceWrapper<>(list);
    }

    private Diff createDiff(NodeSnapshot negativeNode, NodeLocation negativeLocation,
                            NodeSnapshot positiveNode, NodeLocation positiveLocation) {
        NodeSnapshot nonNullNode = positiveNode == null ? negativeNode : positiveNode;

        if (nonNullNode instanceof ElementSnapshot) {
            return createElementDiff(negativeLocation, positiveLocation);

        } else if (nonNullNode instanceof TextNodeSnapshot) {
            return createTextNodeDiff(negativeLocation, positiveLocation);

        } else {
            throw new IllegalArgumentException("Unsupported node type: " + nonNullNode);
        }
    }

    private ElementDiff createElementDiff(NodeLocation negativeLocation, NodeLocation positiveLocation) {
        return new ElementDiff(negativeLocation, positiveLocation);
    }

    private TextNodeDiff createTextNodeDiff(NodeLocation negativeLocation, NodeLocation positiveLocation) {
        return new TextNodeDiff(negativeLocation, positiveLocation);
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
            NodeSnapshot negativeChild = context.getNegativeNode(child);
            context.pushLocation(i);
            diffNode(context, negativeChild, child);
            context.popLocation();
        }
    }

    static class DiffContext {
        DiffCollector collector;
        TreeIndexer negativeIndexer;
        NodeLocationStack positiveLocation;

        DiffContext(NodeSnapshot negativeNode, NodeLocation negativeLocation,
                    NodeSnapshot positiveNode, NodeLocation positiveLocation, DiffCollector collector) {
            this.collector = collector;
            this.negativeIndexer = new TreeIndexer(negativeNode, negativeLocation);
            this.positiveLocation = new NodeLocationStack();
            if (positiveLocation != null) {
                for (int index : positiveLocation) {
                    this.positiveLocation.push(index);
                }
            }
        }

        NodeLocation getNegativeLocation(NodeSnapshot node) {
            return negativeIndexer.getTreeLocation(node);
        }

        public NodeLocation getCurrentLocation() {
            return positiveLocation.toLocation();
        }

        public void collect(Diff diff) {
            collector.add(diff);
        }

        public NodeSnapshot getNegativeNode(NodeSnapshot node) {
            return negativeIndexer.getTreeNode(node);
        }

        public void pushLocation(int index) {
            positiveLocation.push(index);
        }

        public int popLocation() {
            return positiveLocation.pop();
        }

        public Patch createPatch() {
            return collector.toPatch();
        }
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
            if (node == null) {
                return null;
            }
            return originalToTreeLocation.get(node.getOriginalSnapshot());
        }
    }

    static class NodeLocationStack {
        private Stack<Integer> delegate = new Stack<>();

        void push(int element) {
            delegate.push(element);
        }

        int pop() {
            return delegate.pop();
        }

        public NodeLocation toLocation() {
            return new NodeLocation(delegate);
        }
    }
}
