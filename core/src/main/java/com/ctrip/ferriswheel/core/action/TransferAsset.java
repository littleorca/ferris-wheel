package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.intf.Action;

public class TransferAsset extends BaseAction implements Action {
    public String fromSheetName;
    public String fromAssetName;
    public String toSheetName;
    public String toAssetName;

    public TransferAsset() {
    }

    public TransferAsset(String fromSheetName, String fromAssetName, String toSheetName, String toAssetName) {
        this.fromSheetName = fromSheetName;
        this.fromAssetName = fromAssetName;
        this.toSheetName = toSheetName;
        this.toAssetName = toAssetName;
    }

    public String getFromSheetName() {
        return fromSheetName;
    }

    public void setFromSheetName(String fromSheetName) {
        this.fromSheetName = fromSheetName;
    }

    public String getFromAssetName() {
        return fromAssetName;
    }

    public void setFromAssetName(String fromAssetName) {
        this.fromAssetName = fromAssetName;
    }

    public String getToSheetName() {
        return toSheetName;
    }

    public void setToSheetName(String toSheetName) {
        this.toSheetName = toSheetName;
    }

    public String getToAssetName() {
        return toAssetName;
    }

    public void setToAssetName(String toAssetName) {
        this.toAssetName = toAssetName;
    }
}
