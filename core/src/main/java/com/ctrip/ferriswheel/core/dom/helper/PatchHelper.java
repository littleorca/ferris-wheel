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
import com.ctrip.ferriswheel.core.dom.diff.*;
import com.ctrip.ferriswheel.core.util.ShiftAndReduceStack;

import java.util.*;

public class PatchHelper {

    public ElementSnapshot applyPatch(ElementSnapshot tree, Patch patch) {
        if (tree == null || patch == null) {
            throw new IllegalArgumentException();
        }

        if (patch.getDiffList().isEmpty()) {
            return tree;
        }

        // Deletions contains all renames plus the nodes that really deleted, indexed by negative location.
        TreeSet<NodeLocation> deletions = new TreeSet<>();
        // Renames contains all updated/moved nodes, indexed by negative location.
        TreeSet<NodeLocation> renames = new TreeSet<>();
        // Insertions contains all renames plus the case of pure new node, indexed by positive location.
        TreeMap<NodeLocation, Diff> insertions = new TreeMap<>();

        analysePatch(patch, deletions, renames, insertions);
        // tempNodes contains all useful nodes that has either child changes or self updates, indexed by negative location.
        NavigableMap<NodeLocation, NodeSnapshotOrBuilder> tempNodes = applyDeletions(tree, deletions, renames);
        ElementSnapshotOrBuilder tempTree = (ElementSnapshotOrBuilder) tempNodes.get(NodeLocation.root());
        if (tempTree == null) {
            tempTree = tree;
        }
        // tempNodes will be patched if needed, and the index will changed to positive location.
        tempNodes = applyStaticPatch(tempNodes, patch.getDiffList());
        tempTree = applyInsertions(tempTree, tempNodes, insertions);
        if (tempTree instanceof ElementSnapshot) {
            return (ElementSnapshot) tempTree;
        } else if (tempTree instanceof ElementSnapshotBuilder) {
            return ((ElementSnapshotBuilder) tempTree).build();
        } else {
            throw new RuntimeException();
        }
    }

    void analysePatch(Patch patch,
                      TreeSet<NodeLocation> deletions,
                      TreeSet<NodeLocation> renames,
                      TreeMap<NodeLocation, Diff> insertions) {
        for (Diff diff : patch.getDiffList()) {
            if (diff.getNegativeLocation() != null) {
                deletions.add(diff.getNegativeLocation());
            }
            if (diff.getPositiveLocation() != null) {
                Diff priorDiff = insertions.get(diff.getPositiveLocation());
                if (priorDiff == null || !priorDiff.hasContent()) {
                    insertions.put(diff.getPositiveLocation(), diff);
                }

                if (diff.getNegativeLocation() != null) {
                    renames.add(diff.getNegativeLocation());
                }
            }
        }
    }

    NavigableMap<NodeLocation, NodeSnapshotOrBuilder> applyDeletions(ElementSnapshot negativeTree,
                                                                     NavigableSet<NodeLocation> deletions,
                                                                     NavigableSet<NodeLocation> renames) {
        NavigableMap<NodeLocation, NodeSnapshotOrBuilder> dirtyNodes = new TreeMap<>();
        TreeDeleteStack treeDeleteStack = new TreeDeleteStack(negativeTree, deletions, renames, dirtyNodes);
        for (NodeLocation deleteLocation : deletions) {
            treeDeleteStack.feed(deleteLocation);
        }
        treeDeleteStack.terminate();
        return dirtyNodes;
    }

    /**
     * Patch nodes without dealing with there structure, and convert negative
     * temporary node map to positive temporary node map.
     *
     * @param negativeTempNodes
     * @param diffList
     * @return
     */
    NavigableMap<NodeLocation, NodeSnapshotOrBuilder> applyStaticPatch(
            NavigableMap<NodeLocation, NodeSnapshotOrBuilder> negativeTempNodes,
            List<Diff> diffList) {

        NavigableMap<NodeLocation, NodeSnapshotOrBuilder> newTempNodes = new TreeMap<>();

        for (Diff diff : diffList) {
            if (diff.getPositiveLocation() == null) {
                continue;
            }

            NodeSnapshotOrBuilder negativeNode = null;
            if (diff.getNegativeLocation() != null) {
                negativeNode = negativeTempNodes.get(diff.getNegativeLocation());
                if (negativeNode == null) {
                    throw new IllegalStateException();
                }
            }

            NodeSnapshotOrBuilder positiveNode = applyNodePatch(negativeNode, diff);
            if (positiveNode == null) {
                throw new IllegalStateException();
            }
            newTempNodes.put(diff.getPositiveLocation(), positiveNode);
        }

        return newTempNodes;
    }

    ElementSnapshotOrBuilder applyInsertions(ElementSnapshotOrBuilder negativeTree,
                                             NavigableMap<NodeLocation, NodeSnapshotOrBuilder> positiveTempNodes,
                                             NavigableMap<NodeLocation, Diff> insertions) {
        TreeInsertStack treeInsertStack = new TreeInsertStack(negativeTree,
                positiveTempNodes,
                insertions);
        for (NodeLocation insertLocation : insertions.keySet()) {
            treeInsertStack.feed(insertLocation);
        }
        treeInsertStack.terminate();

        ElementSnapshotOrBuilder newTree = (ElementSnapshotOrBuilder) positiveTempNodes.get(NodeLocation.root());
        if (newTree == null) {
            newTree = (ElementSnapshotOrBuilder) positiveTempNodes.get(NodeLocation.root());
        }
        if (newTree == null) {
            newTree = negativeTree;
        }
        return newTree;
    }

    /**
     * Apply patch to a node, and return new patched node.
     *
     * @param negativeNode
     * @param diff
     * @return A new patched node object, with previous node reference refer to
     * the specified <code>negativeNode</code>
     */
    AbstractNodeSnapshotBuilder applyNodePatch(NodeSnapshotOrBuilder negativeNode, Diff diff) {
        if (diff instanceof ElementDiff) {
            return applyElementPatch((ElementSnapshotOrBuilder) negativeNode, (ElementDiff) diff);
        } else if (diff instanceof TextNodeDiff) {
            return applyTextNodePatch(((TextNodeSnapshotOrBuilder) negativeNode), (TextNodeDiff) diff);
        } else {
            throw new RuntimeException();
        }
    }

    /**
     * Apply patch to element, return new patched element builder.
     *
     * @param negativeElement
     * @param diff
     * @return A new patched element builder object.
     */
    ElementSnapshotBuilder applyElementPatch(ElementSnapshotOrBuilder negativeElement, ElementDiff diff) {
        if (diff == null) {
            throw new IllegalArgumentException();
        }

        ElementSnapshotBuilder newElement;

        if (negativeElement == null) {
            newElement = new ElementSnapshotBuilder();
            newElement.setTagName(diff.getTagName());

        } else {
            newElement = negativeElement instanceof ElementSnapshotBuilder ?
                    (ElementSnapshotBuilder) negativeElement :
                    new ElementSnapshotBuilder((ElementSnapshot) negativeElement);
        }

        for (Map.Entry<String, String> negAttrEntry : diff.getNegativeAttributes().entrySet()) {
            if (!newElement.hasAttribute(negAttrEntry.getKey())) {
                throw new IllegalStateException();
            }
            String removed = newElement.removeAttr(negAttrEntry.getKey());
            // check removed.equals(negAttrEntry.getValue()); ?
        }

        for (Map.Entry<String, String> posAttr : diff.getPositiveAttributes().entrySet()) {
            if (newElement.hasAttribute(posAttr.getKey())) {
                throw new IllegalStateException();
            }
            newElement.setAttribute(posAttr.getKey(), posAttr.getValue());
        }

        return newElement;
    }

    /**
     * Apply patch to text node, return new patched text node builder.
     *
     * @param negativeText
     * @param diff
     * @return A new patched text node builder object.
     */
    TextNodeSnapshotBuilder applyTextNodePatch(TextNodeSnapshotOrBuilder negativeText, TextNodeDiff diff) {
        if (diff == null) {
            throw new IllegalArgumentException();
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

        String data = sb.toString();
        if (data.isEmpty() && negativeText != null && negativeText.getData() == null) {
            data = null; // TODO treat null as empty string?
        }

        TextNodeSnapshotBuilder newTextNode = negativeText instanceof TextNodeSnapshotBuilder ?
                (TextNodeSnapshotBuilder) negativeText : negativeText != null ?
                new TextNodeSnapshotBuilder((TextNodeSnapshot) negativeText) :
                new TextNodeSnapshotBuilder();
        newTextNode.setData(data);
        return newTextNode;
    }

    private NodeSnapshotOrBuilder getNodeByLocation(NodeSnapshotOrBuilder root, NodeLocation location) {
        return getNodeByLocation(root, NodeLocation.root(), location);
    }

    private NodeSnapshotOrBuilder getNodeByLocation(NodeSnapshotOrBuilder node,
                                                    NodeLocation nodeLocation,
                                                    NodeLocation targetLocation) {
        if (nodeLocation.equals(targetLocation)) {
            return node;
        }

        if (!nodeLocation.isAncestorOf(targetLocation)) {
            throw new IllegalArgumentException();
        }

        for (int i = nodeLocation.getDepth();
             i < targetLocation.getDepth() && node != null;
             i++) {
            int index = targetLocation.getIndexOfLevel(i);
            List<? extends NodeSnapshotOrBuilder> children = ((ElementSnapshotOrBuilder) node).getChildren();
            if (index >= children.size()) {
                return null;
            }
            node = children.get(index);
        }

        return node;
    }

    abstract class TreePatchStack extends ShiftAndReduceStack<NodeLocation> {
        @Override
        protected boolean tryReduce(NodeLocation inputLocation) {
            if (isEmpty()) {
                return false;
            }

            if (inputLocation != null) {
                if (inputLocation.isSibling(peek()) ||
                        inputLocation.isDescendantOf(peek())) {
                    return false;
                }
            }

            NodeLocation location = pop();
            TreeSet<Integer> collection = new TreeSet<>();
            collection.add(location.leafIndex());
            while (!isEmpty() && location.isSibling(peek())) {
                collection.add(pop().leafIndex());
            }

            NodeLocation parentLocation = location.getParent();
            if (parentLocation == null) {
                if (inputLocation != null) {
                    throw new RuntimeException("Unexpected state encountered, this is probably a bug.");
                }
                doReduce(null, collection);
                return false;
            }

            doReduce(parentLocation, collection);

            return true;
        }

        protected abstract void doReduce(NodeLocation parentLocation, NavigableSet<Integer> collection);
    }

    class TreeDeleteStack extends TreePatchStack {
        private ElementSnapshot negativeTree;
        private NavigableSet<NodeLocation> deletions;
        private NavigableSet<NodeLocation> renames;
        private Map<NodeLocation, NodeSnapshotOrBuilder> dirtyNodes;

        /**
         * Construct a stack for deleting tree nodes.
         *
         * @param negativeTree original tree.
         * @param deletions    sorted deletion list.
         * @param renames      renamed node locations, a subset of deletions.
         * @param dirtyNodes   used to hold dirty nodes after deleting.
         *                     a dirty node is always a new object with
         *                     previous node reference refer to the origin
         *                     node in the <code>negativeTree</code>
         */
        TreeDeleteStack(ElementSnapshot negativeTree,
                        NavigableSet<NodeLocation> deletions,
                        NavigableSet<NodeLocation> renames,
                        Map<NodeLocation, NodeSnapshotOrBuilder> dirtyNodes) {
            this.negativeTree = negativeTree;
            this.deletions = deletions;
            this.renames = renames;
            this.dirtyNodes = dirtyNodes;
        }

        @Override
        protected void doReduce(NodeLocation parentLocation, NavigableSet<Integer> deletedIndices) {
            if (parentLocation == null) {
                if (deletedIndices.size() != 1) {
                    throw new IllegalStateException();
                }
                if (dirtyNodes.isEmpty() && renames.contains(NodeLocation.root())) {
                    dirtyNodes.put(NodeLocation.root(), negativeTree);
                }
                return;
            }

            ElementSnapshot element = (ElementSnapshot) getNodeByLocation(negativeTree, parentLocation);
            // TODO review
            // probably there is not need to worry about element's class, it comes from negative tree,
            // should always be real snapshot instead of a builder.
            ElementSnapshotBuilder elementBuilder = element instanceof ElementSnapshotBuilder ?
                    (ElementSnapshotBuilder) element :
                    new ElementSnapshotBuilder(element);
            int originChildCount = element.getChildren().size();
            for (int i = originChildCount - 1; i >= 0; i--) {
                NodeLocation childLocation = parentLocation.append(i);
                if (deletions.contains(childLocation)) {
                    NodeSnapshotOrBuilder removedChild = elementBuilder.removeChild(i);
                    if (renames.contains(childLocation) && !dirtyNodes.containsKey(childLocation)) {
                        dirtyNodes.put(childLocation, removedChild);
                    }
                    continue;
                }

                NodeSnapshotOrBuilder child = dirtyNodes.get(childLocation);
                if (child != null) {
                    elementBuilder.removeChild(i);
                    elementBuilder.addChild(i, child);
                }
            }

            dirtyNodes.put(parentLocation, elementBuilder);

            if (isEmpty() || !parentLocation.equals(peek())) {
                feed(parentLocation);
            }
        }
    }

    class TreeInsertStack extends TreePatchStack {
        private ElementSnapshotOrBuilder negativeTree;
        private NavigableMap<NodeLocation, Diff> insertions;
        private NavigableMap<NodeLocation, NodeSnapshotOrBuilder> positiveTempNodes;

        /**
         * Construct a stack for tree nodes insertion.
         *
         * @param negativeTree      the origin tree before any update.
         * @param positiveTempNodes temp nodes indexed by positive locations.
         * @param insertions        sorted insertion map of location-diff pairs.
         */
        public TreeInsertStack(ElementSnapshotOrBuilder negativeTree,
                               NavigableMap<NodeLocation, NodeSnapshotOrBuilder> positiveTempNodes,
                               NavigableMap<NodeLocation, Diff> insertions) {
            this.negativeTree = negativeTree;
            this.positiveTempNodes = positiveTempNodes;
            this.insertions = insertions;
        }

        @Override
        protected void doReduce(NodeLocation parentLocation, NavigableSet<Integer> insertIndices) {
            if (parentLocation == null) {
                return; // last reduce
            }

            ElementSnapshotOrBuilder parentElem = (ElementSnapshotOrBuilder) positiveTempNodes.get(parentLocation);
            if (parentElem == null) {
                Map.Entry<NodeLocation, NodeSnapshotOrBuilder> ancestor = positiveTempNodes.floorEntry(parentLocation);
                if (ancestor != null) {
                    parentElem = (ElementSnapshotOrBuilder) getNodeByLocation(ancestor.getValue(),
                            ancestor.getKey(), parentLocation);

                } else {
                    parentElem = (ElementSnapshotOrBuilder) getNodeByLocation(negativeTree, parentLocation);
                }
            }

            if (parentElem == null) {
                throw new IllegalStateException();
            }

            ElementSnapshotBuilder newParent = parentElem instanceof ElementSnapshotBuilder ?
                    (ElementSnapshotBuilder) parentElem :
                    new ElementSnapshotBuilder((ElementSnapshot) parentElem);

            int endIndex = parentElem.getChildren().size() + insertIndices.size();
            for (int positiveIndex = 0; positiveIndex < endIndex; positiveIndex++) {
                NodeLocation childLocation = parentLocation.append(positiveIndex);
                NodeSnapshotOrBuilder child = positiveTempNodes.get(childLocation);
                if (child != null) {
                    newParent.addChild(positiveIndex, child);
                }
            }

            positiveTempNodes.put(parentLocation, newParent);

            if (isEmpty() || !parentLocation.equals(peek())) {
                feed(parentLocation);
            }
        }
    }

}
