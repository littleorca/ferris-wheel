package com.ctrip.ferriswheel.core.asset;

abstract class NamedAssetNode extends AssetNode implements NamedAsset {
    private String name;

    protected NamedAssetNode(String name, AssetManager assetManager) {
        super(assetManager);
        this.name = name;
    }

    NamedAssetNode(String name, long assetId) {
        super(assetId);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

}
