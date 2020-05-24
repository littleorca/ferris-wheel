package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.Node;
import com.ctrip.ferriswheel.core.dom.NodeEssential;
import com.ctrip.ferriswheel.core.dom.NodeSnapshot;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public class WeakNodeSnapshotTracer implements NodeRevisionTracer {
    private Map<Node, WeakReference<NodeSnapshot>> nodeSnapshotMap = new WeakHashMap<>();

    public NodeSnapshot getSnapshot(Node node) {
        WeakReference<NodeSnapshot> ref = nodeSnapshotMap.get(node);
        return ref == null ? null : ref.get();
    }

    public void setSnapshot(Node node, NodeSnapshot snapshot) {
        WeakReference<NodeSnapshot> ref = new WeakReference<>(snapshot);
        nodeSnapshotMap.put(node, ref);
    }

    @Override
    public boolean isSameOrigin(NodeEssential nodeA, NodeEssential nodeB) {
        if (nodeA == nodeB) {
            return true;
        }

        NodeSnapshot snapshotA = null;
        NodeSnapshot snapshotB = null;

        if (nodeA instanceof NodeSnapshot) {
            snapshotA = (NodeSnapshot) nodeA;
        } else if (nodeA instanceof Node) {
            snapshotA = getSnapshot((Node) nodeA);
        }

        if (nodeB instanceof NodeSnapshot) {
            snapshotB = (NodeSnapshot) nodeB;
        } else if (nodeB instanceof Node) {
            snapshotB = getSnapshot((Node) nodeB);
        }

        if (snapshotA == null || snapshotB == null) {
            return false;
        }

        return snapshotA.getOriginalSnapshot() == snapshotB.getOriginalSnapshot();
    }
}
