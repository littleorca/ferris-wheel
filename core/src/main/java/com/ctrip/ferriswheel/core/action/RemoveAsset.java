package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;

public class RemoveAsset extends SheetAction implements Action {
    private String assetName;

    public RemoveAsset() {
    }

    public RemoveAsset(String sheetName, String assetName) {
        super(sheetName);
        this.assetName = assetName;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
}
