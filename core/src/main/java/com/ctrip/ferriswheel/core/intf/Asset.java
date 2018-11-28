package com.ctrip.ferriswheel.core.intf;

import java.io.Serializable;

public interface Asset extends Serializable {
    long UNSPECIFIED_ASSET_ID = -1;

    long getAssetId();

    boolean isValid();
}
