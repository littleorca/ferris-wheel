package com.ctrip.ferriswheel.core.dom;

import java.util.Collection;
import java.util.List;

public interface ElementSnapshotOrBuilder extends NodeSnapshotOrBuilder, ElementEssential {

    Collection<AttributeSnapshot> getAttributes();

    List<? extends NodeSnapshotOrBuilder> getChildren();

    @Override
    ElementSnapshot getPreviousSnapshot();

    @Override
    ElementSnapshot getOriginalSnapshot();

    @Override
    ElementSnapshot duplicate(boolean linked);

}
