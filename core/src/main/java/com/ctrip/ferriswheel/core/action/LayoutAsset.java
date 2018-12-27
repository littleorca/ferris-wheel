package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.api.view.Layout;

public class LayoutAsset extends SheetAction {
    private String assetName;
    private Layout layout;

    public LayoutAsset() {
    }

    public LayoutAsset(String sheetName, String assetName, Layout layout) {
        super(sheetName);
        this.assetName = assetName;
        this.layout = layout;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }
}
