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
import com.ctrip.ferriswheel.core.dom.impl.AttributeSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.TextNodeSnapshotImpl;

import java.util.*;
import java.util.function.BiFunction;

public class PatchHelper {
    public NodeSnapshot applyPatch(ElementSnapshot tree, Patch patch) {
        if (tree == null || patch == null) {
            throw new IllegalArgumentException();
        }

        TreeSet<NodeLocation> deletions = new TreeSet<>();
        TreeMap<NodeLocation, Diff> insertions = new TreeMap<>();
        Map<NodeLocation, NodeSnapshot> negativeDirtyNodes = new HashMap<>();
        analysePatch(tree, patch, deletions, insertions);
        NodeSnapshot middleTree = applyDeletions(tree, deletions, negativeDirtyNodes);
        return applyInsertions(tree, middleTree, insertions, negativeDirtyNodes);
    }

    private void analysePatch(ElementSnapshot tree,
                              Patch patch,
                              TreeSet<NodeLocation> deletions,
                              TreeMap<NodeLocation, Diff> insertions) {
        for (Diff diff : patch.getDiffList()) {
            if (diff.getNegativeLocation() != null) {
                deletions.add(diff.getNegativeLocation());
            }
            if (diff.getPositiveLocation() != null) {
                Diff priorDiff = insertions.get(diff.getPositiveLocation());
                if (priorDiff != null && priorDiff.hasContent()) {
                    insertions.put(diff.getPositiveLocation(), priorDiff);
                } else {
                    insertions.put(diff.getPositiveLocation(), diff);
                }
            }
        }
    }

    private NodeSnapshot applyDeletions(NodeSnapshot tree,
                                        SortedSet<NodeLocation> deletions,
                                        Map<NodeLocation, NodeSnapshot> dirtyNodes) {
        return shiftAndReduce(deletions, (location, deletedIndices) -> {
            ElementSnapshot element = (ElementSnapshot) getNodeByLocation(tree, location);
            List<NodeSnapshot> priorChildren = element.getChildren();
            List<NodeSnapshot> nextChildren = new ArrayList<>(priorChildren.size() - deletedIndices.size());
            for (int i = 0; i < priorChildren.size(); i++) {
                if (deletedIndices.contains(i)) {
                    continue;
                }
                NodeSnapshot child = dirtyNodes.get(new NodeLocation(location, i));
                if (child == null) {
                    child = priorChildren.get(i);
                }
                nextChildren.add(child);
            }
            ElementSnapshotImpl dirtyElement = new ElementSnapshotImpl(
                    element.getTagName(),
                    element.getAttributes(),
                    nextChildren,
                    element);
            dirtyNodes.put(location, dirtyElement);
            return dirtyElement;
        });
    }

    private NodeSnapshot applyInsertions(NodeSnapshot negativeTree,
                                         NodeSnapshot middleTree,
                                         TreeMap<NodeLocation, Diff> insertions,
                                         Map<NodeLocation, NodeSnapshot> negativeDirtyNodes) {
        final Map<NodeLocation, NodeSnapshot> dirtyNodes = new HashMap<>();

        return shiftAndReduce(insertions.keySet(), (location, insertedIndices) -> {
            ElementSnapshot element = (ElementSnapshot) getNodeByLocation(middleTree, location);
            List<NodeSnapshot> priorChildren = element.getChildren();
            List<NodeSnapshot> nextChildren = new ArrayList<>(priorChildren.size() + insertedIndices.size());

            int pos = 0;
            for (int insertedIndex : insertedIndices) {
                for (int i = nextChildren.size(); i < insertedIndex; i++, pos++) {
                    NodeSnapshot child = dirtyNodes.get(new NodeLocation(location, i));
                    if (child == null) {
                        child = priorChildren.get(pos);
                    }
                    nextChildren.add(child);
                }

                NodeLocation insertLocation = new NodeLocation(location, insertedIndex);
                NodeSnapshot child = dirtyNodes.get(insertLocation);
                if (child == null) {
                    Diff diff = insertions.get(insertLocation);
                    if (diff.getNegativeLocation() != null) {
                        child = negativeDirtyNodes.get(diff.getNegativeLocation());
                        if (child == null) {
                            child = getNodeByLocation(negativeTree, diff.getNegativeLocation());
                        }
                    }
                    child = applyNodePatch(child, diff);
                }
                nextChildren.add(child);
            }

            for (; pos < priorChildren.size(); pos++) {
                NodeSnapshot child = dirtyNodes.get(new NodeLocation(location, nextChildren.size()));
                if (child == null) {
                    child = priorChildren.get(pos);
                }
                nextChildren.add(child);
            }


            for (int i = 0; i < priorChildren.size(); i++) {
                if (insertedIndices.contains(i)) {
                    continue;
                }
                NodeSnapshot child = dirtyNodes.remove(new NodeLocation(location, i));
                if (child == null) {
                    child = priorChildren.get(i);
                }
                nextChildren.add(child);
            }
            ElementSnapshotImpl dirtyElement = new ElementSnapshotImpl(
                    element.getTagName(),
                    element.getAttributes(),
                    nextChildren,
                    element);
            dirtyNodes.put(location, dirtyElement);
            return dirtyElement;
        });
    }

    private <T> T shiftAndReduce(Collection<NodeLocation> locations,
                                 BiFunction<NodeLocation, Collection<Integer>, T> callback) {
        Stack<NodeLocation> stack = new Stack<>();
        T lastResult = null;

        for (NodeLocation location : locations) {
            if (stack.isEmpty() || location.isSibling(stack.peek())) {
                stack.push(location);
            } else {
                lastResult = reduce(stack, location, callback);
            }
        }

        if (!stack.isEmpty()) {
            lastResult = reduce(stack, null, callback);
        }

        if (!stack.isEmpty()) {
            throw new RuntimeException("Unexpected state, this is probably a bug.");
        }

        return lastResult;
    }

    private <T> T reduce(Stack<NodeLocation> stack,
                         NodeLocation pendingLocation,
                         BiFunction<NodeLocation, Collection<Integer>, T> callback) {
        T lastResult;
        NodeLocation location = stack.pop();
        LinkedList<Integer> collection = new LinkedList<>();
        collection.add(location.leafIndex());
        while (location.isSibling(stack.peek())) {
            collection.add(stack.pop().leafIndex());
        }
        NodeLocation parentLocation = location.getParent();
        if (parentLocation == null) {
            if (!collection.isEmpty()) {
                throw new RuntimeException("Unexpected state encountered, this is probably a bug.");
            }
            return null; // FIXME
        }

        lastResult = callback.apply(parentLocation, collection);

        if (stack.isEmpty() || !stack.peek().isParentOf(location)) {
            stack.push(parentLocation);
        }
        if (pendingLocation == null || !pendingLocation.isSibling(stack.peek())) {
            lastResult = reduce(stack, pendingLocation, callback);
        } else if (pendingLocation != null) {
            stack.push(pendingLocation);
        }

        return lastResult;
    }

    NodeSnapshot applyNodePatch(NodeSnapshot negativeNode, Diff diff) {
        if (diff instanceof ElementDiff) {
            return applyElementPatch((ElementSnapshot) negativeNode, (ElementDiff) diff);
        } else if (diff instanceof TextNodeDiff) {
            return applyTextNodePatch(((TextNodeSnapshot) negativeNode), (TextNodeDiff) diff);
        } else {
            throw new RuntimeException();
        }
    }

    ElementSnapshot applyElementPatch(ElementSnapshot negativeElement, ElementDiff diff) {
        if (!diff.hasAttributeChanges()) {
            return negativeElement;
        }

        List<AttributeSnapshot> attrs = new ArrayList<>();
        for (AttributeSnapshot nAttr : negativeElement.getAttributes()) {
            if (!diff.getNegativeAttributes().containsKey(nAttr.getName())) {
                attrs.add(nAttr);
            }
        }
        for (Map.Entry<String, String> pAttr : diff.getPositiveAttributes().entrySet()) {
            attrs.add(new AttributeSnapshotImpl(pAttr.getKey(), pAttr.getValue(), null));
        }

        return new ElementSnapshotImpl(negativeElement.getTagName(),
                attrs,
                new ArrayList<>(negativeElement.getChildren()),
                negativeElement);
    }

    TextNodeSnapshot applyTextNodePatch(TextNodeSnapshot negativeText, TextNodeDiff diff) {
        if (!diff.hasTextChanges()) {
            return negativeText;
        }

        LineArrayWrapper nSeq = new LineArrayWrapper(negativeText == null ?
                null : negativeText.getData());
        StringBuilder sb = new StringBuilder();
        int ni = 0, pi = 0;

        // here assume LineDiff objects is well ordered, as produced by Myers difference algorithm.
        for (LineDiff lineDiff : diff.getLines()) {
            int op = lineDiff.getOp();
            if (op < 0) {
                int nPos = -op - 1;
                for (; ni < nPos; ni++, pi++) {
                    LineArrayWrapper.LineWrapper ln = nSeq.get(ni);
                    sb.append(ln.getOriginText(), ln.getStart(), ln.getEnd());
                }
                ni++;

            } else {
                int pPos = op - 1;
                for (; pi < pPos; ni++, pi++) {
                    LineArrayWrapper.LineWrapper ln = nSeq.get(ni);
                    sb.append(ln.getOriginText(), ln.getStart(), ln.getEnd());
                }
                sb.append(lineDiff.getContent());
                pi++;
            }
        }
        for (; ni < nSeq.size(); ni++) {
            LineArrayWrapper.LineWrapper ln = nSeq.get(ni);
            sb.append(ln.getOriginText(), ln.getStart(), ln.getEnd());
        }

        return new TextNodeSnapshotImpl(sb.toString(), negativeText);
    }

    private NodeSnapshot getNodeByLocation(NodeSnapshot root, NodeLocation location) {
        Iterator<Integer> it = location.iterator();
        if (it.next() != 0) {
            throw new RuntimeException();
        }
        NodeSnapshot n = root;
        while (it.hasNext()) {
            n = ((ElementSnapshot) n).getChildren().get(it.next());
        }
        return n;
    }

    class PendingAddNode {
        NodeSnapshot origin;
        NodeSnapshot revised;

        public PendingAddNode(NodeSnapshot origin, NodeSnapshot revised) {
            this.origin = origin;
            this.revised = revised;
        }
    }
}
