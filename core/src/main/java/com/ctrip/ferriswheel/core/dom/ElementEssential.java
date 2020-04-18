package com.ctrip.ferriswheel.core.dom;

import java.util.Collection;

public interface ElementEssential extends NodeEssential {

    @Override
    default NodeType getNodeType() {
        return NodeType.ELEMENT_NODE;
    }

    String getTagName();

    @Override
    default String getNodeName() {
        return getTagName();
    }

    @Override
    default String getNodeValue() {
        return null; // TODO review
    }

    boolean hasAttribute(String name);

    String getAttribute(String name);

    Collection<? extends AttributeEssential> getAttributes();

}
