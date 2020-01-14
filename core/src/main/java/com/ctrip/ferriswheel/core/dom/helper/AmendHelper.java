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
    private final boolean attrIncremental;
    private final boolean nodeIncremental;

    public static AmendmentCollector makeAmendmentByDiff(String pathname, NodeSnapshot newNode, NodeSnapshot oldNode) {
        return makeAmendmentByDiff(null, pathname, newNode, oldNode);
    }

    public static AmendmentCollector makeAmendmentByDiff(AmendmentCollector collector, String pathname,
                                                         NodeSnapshot newNode, NodeSnapshot oldNode) {
        return makeAmendmentByDiff(collector, pathname, newNode, oldNode, false, false);
    }

    public static AmendmentCollector makeAmendmentByDiff(String pathname, NodeSnapshot newNode, NodeSnapshot oldNode,
                                                         boolean attrIncremental, boolean nodeIncremental) {
        return makeAmendmentByDiff(null, pathname, newNode, oldNode,
                attrIncremental, nodeIncremental);
    }

    /**
     * Make amendment by difference between new node and old node. If old node is null, the amendment
     * contains actions to create refresh new node.
     *
     * @param collector
     * @param pathname        pathname of the starting node, it MUST contains the node's name,
     *                        and the name property of node object will be ignored.
     * @param newNode         node that will be create or update to.
     * @param oldNode         if old node is specified, the amendment means to update old node to new node,
     *                        otherwise the amendment means to create refresh new node.
     * @param attrIncremental
     * @param nodeIncremental
     * @return
     */
    public static AmendmentCollector makeAmendmentByDiff(AmendmentCollector collector,
                                                         String pathname,
                                                         NodeSnapshot newNode,
                                                         NodeSnapshot oldNode,
                                                         boolean attrIncremental,
                                                         boolean nodeIncremental) {
        if (collector == null) {
            collector = new AmendmentCollector();
        }
        return new AmendHelper(collector, pathname, newNode, oldNode,
                attrIncremental, nodeIncremental).getCollector();
    }

    private AmendHelper(AmendmentCollector collector,
                        String pathname,
                        final NodeSnapshot newNode,
                        final NodeSnapshot oldNode,
                        boolean attrIncremental,
                        boolean nodeIncremental) {
        this.collector = collector;
        this.pathname = pathname;
        this.newNode = newNode;
        this.oldNode = oldNode;
        this.attrIncremental = attrIncremental;
        this.nodeIncremental = nodeIncremental;
    }

    private AmendmentCollector getCollector() {
        if (oldNode != null) { // TODO disclaim rename operations. probably a ID map of old tree is needed
            diffRecursively(pathname, newNode, oldNode);
        } else {
            addNewNodeRecursively(pathname, newNode);
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
        }
/* TODO accomplish this
        Map<String, NodeSnapshot> newChildrenMap = getChildrenMap(newNode);
        Map<String, NodeSnapshot> oldChildrenMap = getChildrenMap(oldNode);

        if (!nodeIncremental) {
            Set<String> delNodes = new HashSet<>(oldChildrenMap.keySet());
            delNodes.removeAll(newChildrenMap.keySet());
            for (String name : delNodes) {
                collector.del(XPathHelper.join(pathname, name));
            }
        }

        if (newNode.getChildren() != null) {
            for (NodeSnapshot newChild : newNode.getChildren()) {
                String childPathname = XPathHelper.join(pathname, newChild.getName());
                NodeSnapshot oldChild = oldChildrenMap.get(newChild.getName());
                if (oldChild != null) { // update
                    diffRecursively(childPathname, newChild, oldChild);
                } else { // add
                    addNewNodeRecursively(childPathname, newChild);
                }
            }
        }
        */
    }

    private void diffAttributes(String pathname, Map<String, String> newAttrs,
                                Map<String, String> oldAttrs) {
        if (newAttrs == null) {
            newAttrs = Collections.emptyMap();
        }
        if (oldAttrs == null) {
            oldAttrs = Collections.emptyMap();
        }
        if (!attrIncremental) {
            Set<String> delAttrs = new HashSet<>(oldAttrs.keySet());
            delAttrs.removeAll(newAttrs.keySet());
            for (String attrName : delAttrs) {
                collector.delAttr(pathname, attrName);
            }
        }
        for (Map.Entry<String, String> newAttrEntry : newAttrs.entrySet()) {
            String oldAttr = oldAttrs.get(newAttrEntry.getKey());
            if (oldAttr == null || !oldAttr.equals(newAttrEntry.getValue())) {
                collector.putAttr(pathname, newAttrEntry.getKey(), newAttrEntry.getValue());
            }
        }
    }

    private void addNewNodeRecursively(String pathname, NodeSnapshot node) {
//        if (!XPathHelper.isRootPath(pathname)) {
//            collector.add(pathname);
//        }
//        if (node.getAttributes() != null) {
//            for (Attribute attr : node.getAttributes().values()) {
//                collector.putAttr(pathname, attr);
//            }
//        }
//        if (node.getChildren() != null) {
//            for (TreeNode child : node.getChildren()) {
//                String childPathname = PathHelper.join(pathname, child.getName());
//                addNewNodeRecursively(childPathname, child);
//            }
//        }
    }

//    private Map<String, TreeNode> getChildrenMap(TreeNode parent) {
//        Map<String, TreeNode> map = new HashMap<>(parent.getChildren() == null ? 0 : parent.getChildren().size());
//        if (parent.getChildren() == null) {
//            return map;
//        }
//        for (TreeNode child : parent.getChildren()) {
//            map.put(child.getName(), child);
//        }
//        return map;
//    }
//
//    public static List<Change> dumpChanges(ReviseNode rootNode) {
//        List<Change> changes = new ArrayList<>();
//        rootNode.setPathname(PathHelper.getRootPath());
//        dumpChangesRecursively(rootNode, changes);
//        return changes;
//    }
//
//    private static void dumpChangesRecursively(ReviseNode node, List<Change> changes) {
//        for (Map.Entry<String, Attribute> entry : node.getDelAttrs().entrySet()) {
//            changes.add(new ChangeImpl.DelAttrImpl(node.getPathname(), entry.getKey()));
//        }
//        for (Attribute attr : node.getAttrs()) {
//            changes.add(new ChangeImpl.PutAttrImpl(node.getPathname(), attr));
//        }
//        for (String name : node.getDelNodes().keySet()) {
//            if (node.getChild(name) == null) {
//                changes.add(new ChangeImpl.DelImpl(PathHelper.join(node.getPathname(), name)));
//            } // otherwise the node exists and just has been modified. TODO review
//        }
//        for (ReviseNode child : node.getChildren()) {
//            child.setPathname(PathHelper.join(node.getPathname(), child.getName()));
//            if (child.getShadow() == null) {
//                changes.add(new ChangeImpl.AddImpl(child.getPathname()));
//            }
//        }
//        for (ReviseNode child : node.getChildren()) {
//            dumpChangesRecursively(child, changes);
//        }
//    }
}
