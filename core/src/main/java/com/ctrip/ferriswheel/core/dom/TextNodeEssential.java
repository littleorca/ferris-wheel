package com.ctrip.ferriswheel.core.dom;

public interface TextNodeEssential extends NodeEssential {
    String TEXT_NODE_NAME = "#text";

    @Override
    default NodeType getNodeType() {
        return NodeType.TEXT_NODE;
    }

    @Override
    default String getNodeName() {
        return TEXT_NODE_NAME;
    }

    @Override
    default String getNodeValue() {
        return getData();
    }

    @Override
    default String getTextContent() {
        return getNodeValue();
    }

    String getData();
}
