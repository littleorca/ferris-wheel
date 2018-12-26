package com.ctrip.ferriswheel.core.intf;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface Asset extends Serializable {
    long UNSPECIFIED_ASSET_ID = -1;

    long getAssetId();

    boolean isValid();

    /**
     * Determine if an asset is ephemeral. An ephemeral asset can be destroyed
     * during the refresh procedure.
     *
     * @return
     */
    boolean isEphemeral();

    Asset getParent();

    List<? extends Asset> getChildren();

    /**
     * Get nodes that the represented node is depending on.
     *
     * @return
     */
    Set<? extends Asset> getDependencies();

    /**
     * Get nodes that depending on the represented node.
     *
     * @return
     */
    Set<? extends Asset> getDependents();

}
