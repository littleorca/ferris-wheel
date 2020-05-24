package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.NodeSnapshot;

public class TreeSnapshot {
    private NodeSnapshot root;
    private NodeRevisionTracer nodeTracer;

    public TreeSnapshot(NodeSnapshot root, NodeRevisionTracer nodeTracer) {
        this.root = root;
        this.nodeTracer = nodeTracer;
    }

    public NodeSnapshot getRoot() {
        return root;
    }

    public void setRoot(NodeSnapshot root) {
        this.root = root;
    }

    public NodeRevisionTracer getNodeTracer() {
        return nodeTracer;
    }

    public void setNodeTracer(NodeRevisionTracer nodeTracer) {
        this.nodeTracer = nodeTracer;
    }
}
