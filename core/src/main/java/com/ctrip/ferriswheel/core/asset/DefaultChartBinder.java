package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.chart.ChartBinder;
import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.common.view.Orientation;
import com.ctrip.ferriswheel.common.view.Placement;
import com.ctrip.ferriswheel.common.variant.impl.Value;

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
        return (DefaultChart) getParent();
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
