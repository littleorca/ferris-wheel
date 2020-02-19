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

import java.util.*;

public class AmendHelper {
    private AmendmentCollector collector;
    private final String pathname;
    private final NodeSnapshot newNode;
    private final NodeSnapshot oldNode;

    /**
     * Make amendment by difference between new node and old node. If old node is null, the amendment
     * contains actions to create refresh new node.
     *
     * @param collector
     * @param pathname  pathname of the starting node, it MUST contains the node's name,
     *                  and the name property of node object will be ignored.
     * @param newNode   node that will be create or update to.
     * @param oldNode   if old node is specified, the amendment means to update old node to new node,
     *                  otherwise the amendment means to create refresh new node.
     * @return
     */
    public static AmendmentCollector diff(AmendmentCollector collector,
                                          String pathname,
                                          NodeSnapshot newNode,
                                          NodeSnapshot oldNode) {
        if (collector == null) {
            collector = new AmendmentCollector();
        }
        return new AmendHelper(collector, pathname, newNode, oldNode).getCollector();
    }

    private AmendHelper(AmendmentCollector collector,
                        String pathname,
                        final NodeSnapshot newNode,
                        final NodeSnapshot oldNode) {
        this.collector = collector;
        this.pathname = pathname;
        this.newNode = newNode;
        this.oldNode = oldNode;
    }

    private AmendmentCollector getCollector() {
        if (oldNode != null) { // TODO disclaim rename operations. probably a ID map of old tree is needed
            diffRecursively(pathname, newNode, oldNode);
        } else {
//            addNewNodeRecursively(pathname, newNode);
        }
        return collector;
    }

    private void diffRecursively(String pathname, NodeSnapshot newNode, NodeSnapshot oldNode) {
        if (newNode instanceof ElementSnapshot) {
            if (!(oldNode instanceof ElementSnapshot)) {
                throw new RuntimeException("Node type not match, probably a bug.");
            }
            Collection<? extends AttributeSnapshot> newAttrs = ((ElementSnapshot) newNode).getAttributes();
            Collection<? extends AttributeSnapshot> oldAttrs = ((ElementSnapshot) oldNode).getAttributes();
            Map<String, String> newAttrMap = new HashMap<>(newAttrs.size());
            Map<String, String> oldAttrMap = new HashMap<>(oldAttrs.size());
            newAttrs.forEach(a -> newAttrMap.put(a.getName(), a.getValue()));
            oldAttrs.forEach(a -> oldAttrMap.put(a.getName(), a.getValue()));

            diffAttributes(pathname, newAttrMap, oldAttrMap);
            diffChildList((ElementSnapshot) oldNode, (ElementSnapshot) newNode);
        }
    }

    private void diffAttributes(String pathname, Map<String, String> newAttrs,
                                Map<String, String> oldAttrs) {
        if (newAttrs == null) {
            newAttrs = Collections.emptyMap();
        }
        if (oldAttrs == null) {
            oldAttrs = Collections.emptyMap();
        }
//        if (!attrIncremental) {
        Set<String> delAttrs = new HashSet<>(oldAttrs.keySet());
        delAttrs.removeAll(newAttrs.keySet());
        for (String attrName : delAttrs) {
            collector.delAttr(pathname, attrName);
        }
//        }
        for (Map.Entry<String, String> newAttrEntry : newAttrs.entrySet()) {
            String oldAttr = oldAttrs.get(newAttrEntry.getKey());
            if (oldAttr == null || !oldAttr.equals(newAttrEntry.getValue())) {
                collector.putAttr(pathname, newAttrEntry.getKey(), newAttrEntry.getValue());
            }
        }
    }

    static List<LDAction> diffChildList(ElementSnapshot nodeA, ElementSnapshot nodeB) {
        List<? extends NodeSnapshot> childListA = nodeA.getChildren();
        List<? extends NodeSnapshot> childListB = nodeB.getChildren();

        int[][] matrix = new int[childListA.size() + 1][childListB.size() + 1];

        for (int i = 0; i <= childListA.size(); i++) {
            for (int j = 0; j <= childListB.size(); j++) {
                if (i == 0) {
                    matrix[i][j] = j;
                } else if (j == 0) {
                    matrix[i][j] = i;
                } else {
                    int min = Math.min(matrix[i - 1][j], matrix[i][j - 1]);
                    min = Math.min(min, matrix[i - 1][j - 1]);
                    matrix[i][j] = (isSameNode(childListA.get(i - 1), childListB.get(j - 1))) ? min : min + 1;
                }
            }
        }

        int i = childListA.size();
        int j = childListB.size();
        int current = matrix[i][j];
        LinkedList<LDAction> ldActions = new LinkedList<>();

        while (i > 0 && j > 0) {
            int corner = matrix[i - 1][j - 1];
            int upper = matrix[i - 1][j];
            int left = matrix[i][j - 1];

            if (corner <= upper && corner <= left) {
                if (current != corner) {
                    ldActions.add(new LDAction(j - 1, true, childListB.get(j - 1)));
                    ldActions.add(new LDAction(i - 1, false, childListA.get(i - 1)));
                    current = corner;
                }
                i--;
                j--;

            } else if (upper <= left) {
                i--;
                if (current != upper) {
                    ldActions.add(new LDAction(i, false, childListA.get(i)));
                    current = upper;
                }
            } else {
                j--;
                if (current != left) {
                    ldActions.add(new LDAction(j, true, childListB.get(j)));
                    current = left;
                }
            }
        }
        while (i > 0) {
            i--;
            ldActions.add(new LDAction(i, false, childListA.get(i)));
        }
        while (j > 0) {
            j--;
            ldActions.add(new LDAction(j, true, childListB.get(j)));
        }

        Collections.reverse(ldActions);
        return ldActions;
    }

    static boolean isSameNode(NodeSnapshot nodeA, NodeSnapshot nodeB) {
        if (nodeA == nodeB) {
            return true;
        }

        NodeSnapshot temp = nodeA.getPreviousSnapshot();
        while (temp != null) {
            if (temp == nodeB) {
                return true;
            }
            temp = temp.getPreviousSnapshot();
        }

        temp = nodeB.getPreviousSnapshot();
        while (temp != null) {
            if (temp == nodeA) {
                return true;
            }
            temp = temp.getPreviousSnapshot();
        }

        return false;
    }

    static class LDAction {
        int index;
        boolean positive;
        NodeSnapshot node;

        public LDAction(int index, boolean positive, NodeSnapshot node) {
            this.index = index;
            this.positive = positive;
            this.node = node;
        }

        @Override
        public String toString() {
            String nodeDesc = node instanceof ElementSnapshot ?
                    ((ElementSnapshot) node).getTagName() :
                    node.toString();
            return (positive ? "+" : "-") + "@" + index + "\t" + nodeDesc;
        }
    }
}
