package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.chart.ChartBinder;
import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.view.Orientation;
import com.ctrip.ferriswheel.common.view.Placement;

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

    @Override
    protected EvaluationState doEvaluate(EvaluationContext context) {
        // leave the job to chart.
        return EvaluationState.DONE;
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
        if (this.orientation == null && orientation == null) {
            return;
        }
        if (this.orientation != null && this.orientation.equals(orientation)) {
            return;
        }

        this.orientation = orientation;
        markDirty();
    }

    @Override
    public Placement getCategoriesPlacement() {
        return categoriesPlacement;
    }

    void setCategoriesPlacement(Placement categoriesPlacement) {
        if (this.categoriesPlacement == null && categoriesPlacement == null) {
            return;
        }
        if (this.categoriesPlacement != null && this.categoriesPlacement.equals(categoriesPlacement)) {
            return;
        }

        this.categoriesPlacement = categoriesPlacement;
        markDirty();
    }

    @Override
    public Placement getSeriesNamePlacement() {
        return seriesNamePlacement;
    }

    void setSeriesNamePlacement(Placement seriesNamePlacement) {
        if (this.seriesNamePlacement == null && seriesNamePlacement == null) {
            return;
        }
        if (this.seriesNamePlacement != null && this.seriesNamePlacement.equals(seriesNamePlacement)) {
            return;
        }

        this.seriesNamePlacement = seriesNamePlacement;
        markDirty();
    }
}
