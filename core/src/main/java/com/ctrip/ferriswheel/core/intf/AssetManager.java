package com.ctrip.ferriswheel.core.intf;

public interface AssetManager {

    /**
     * Acquire for a new asset ID.
     *
     * @return
     */
    long nextAssetId();

    /**
     * Employ the specified asset.
     *
     * @param asset
     */
    void employ(Asset asset);

    /**
     * Get employed asset by ID.
     *
     * @param id
     * @return
     */
    Asset get(long id);

    /**
     * Determine whether the asset with the specified ID is employed or not.
     *
     * @param id
     * @return
     */
    boolean exists(long id);

    /**
     * Dismiss the specified asset.
     *
     * @param asset
     */
    void dismiss(Asset asset);

    /**
     * Subscribe asset change event.
     */
    void subscribe(AssetChangeCallback callback);

    /**
     * Asset change event callback.
     */
    interface AssetChangeCallback {
        /**
         * Asset has been employed.
         *
         * @param asset
         */
        void onAssetEmployed(Asset asset);

        /**
         * Asset update event callback.
         *
         * @param asset
         */
        void onAssetUpdate(Asset asset);

        /**
         * Asset has been dismissed.
         *
         * @param asset
         */
        void onAssetDismissed(Asset asset);
    }
}
