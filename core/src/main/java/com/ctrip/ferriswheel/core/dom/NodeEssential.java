package com.ctrip.ferriswheel.core.dom;

public interface NodeEssential {

    NodeType getNodeType();

    String getNodeName();

    String getTextContent();

    String getNodeValue();

    default boolean hasChildNodes() {
        return false;
    }

    default boolean contains(Node otherNode) {
        return false;
    }

    default int getChildCount() {
        return 0;
    }

    default NodeEssential getChild(int index) {
        throw new UnsupportedOperationException();
    }

    default NodeEssential getChild(String name) {
        throw new UnsupportedOperationException();
    }

    default NodeEssential firstChild() {
        return null;
    }

    default NodeEssential lastChild() {
        return null;
    }

}
