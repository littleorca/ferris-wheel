package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.Node;
import com.ctrip.ferriswheel.core.dom.NodeEssential;
import com.ctrip.ferriswheel.core.dom.NodeSnapshot;

public interface NodeRevisionTracer {

    /**
     * Get the bound snapshot node.
     *
     * @param node
     * @return
     */
    NodeSnapshot getSnapshot(Node node);

    /**
     * Set the bound snapshot of the specified node.
     *
     * @param node
     * @param snapshot
     */
    void setSnapshot(Node node, NodeSnapshot snapshot);

    /**
     * Determine if the specified <code>nodeA</code> and <code>nodeB</code> are
     * same origin. Two nodes are same origin if they match any of the
     * following conditions:
     * <ol>
     *    <li>
     *        <code>nodeA</code> and <code>nodeB</code> are the same node;
     *    </li>
     *    <li>
     *        <code>nodeA</code> is the newer revision f <code>nodeB</code>;
     *    </li>
     *    <li>
     *        <code>nodeA</code> is the older revision f <code>nodeB</code>.
     *    </li>
     * </ol>
     * Implementations are obviously rely on certain revise tracing mechanism,
     * if any of the specified nodes is beyond the tracing, false value should
     * be returned.
     *
     * @param nodeA
     * @param nodeB
     * @return true if <code>nodeA</code> and <code>nodeB</code> are same
     * origin, false otherwise.
     */
    boolean isSameOrigin(NodeEssential nodeA, NodeEssential nodeB);
}
