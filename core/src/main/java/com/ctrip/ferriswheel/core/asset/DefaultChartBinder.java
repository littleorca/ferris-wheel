package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.intf.*;

public class DefaultChartBinder extends AssetNode implements ChartBinder {
    private final ValueNode data;
    private Orientation orientation;
    private Placement categoriesPlacement;
    private Placement seriesNamePlacement;

    DefaultChartBinder(AssetManager assetManager) {
        super(assetManager);
        this.data = new ValueNode(assetManager, Value.BLANK, null);
        bindChild(this.data);
    }

    DefaultChart getChart() {
        return (DefaultChart) getParentAsset();
    }

    @Override
    public ValueNode getData() {
        return data;
    }

    void setData(DynamicVariant data) {
        this.data.setDynamicVariant(data);
    }

    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    @Override
    public Placement getCategoriesPlacement() {
        return categoriesPlacement;
    }

    void setCategoriesPlacement(Placement categoriesPlacement) {
        this.categoriesPlacement = categoriesPlacement;
    }

    @Override
    public Placement getSeriesNamePlacement() {
        return seriesNamePlacement;
    }

    void setSeriesNamePlacement(Placement seriesNamePlacement) {
        this.seriesNamePlacement = seriesNamePlacement;
    }
}
