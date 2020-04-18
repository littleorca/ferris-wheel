package com.ctrip.ferriswheel.core.dom;

public interface AttributeEssential extends NodeEssential {
    @Override
    default NodeType getNodeType() {
        return NodeType.ATTRIBUTE_NODE;
    }

    String getName();

    String getValue();

    @Override
    default String getNodeValue() {
        return getValue();
    }

    @Override
    default String getTextContent() {
        return getValue();
    }
}
