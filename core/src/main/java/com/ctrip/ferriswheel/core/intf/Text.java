package com.ctrip.ferriswheel.core.intf;

public interface Text extends NamedAsset, Displayable {
    VariantNode getContent();
}
