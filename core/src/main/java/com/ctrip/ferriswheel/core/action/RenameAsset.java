package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.api.action.Action;

public class RenameAsset extends SheetAction implements Action {
    private String oldAssetName;
    private String newAssetName;

    public RenameAsset() {
    }

    public RenameAsset(String sheetName, String oldAssetName, String newAssetName) {
        super(sheetName);
        this.oldAssetName = oldAssetName;
        this.newAssetName = newAssetName;
    }

    public String getOldAssetName() {
        return oldAssetName;
    }

    public void setOldAssetName(String oldAssetName) {
        this.oldAssetName = oldAssetName;
    }

    public String getNewAssetName() {
        return newAssetName;
    }

    public void setNewAssetName(String newAssetName) {
        this.newAssetName = newAssetName;
    }
}
