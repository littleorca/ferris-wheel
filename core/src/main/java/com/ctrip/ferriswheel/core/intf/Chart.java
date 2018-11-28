package com.ctrip.ferriswheel.core.intf;

import com.ctrip.ferriswheel.core.bean.Axis;

public interface Chart extends NamedAsset, Displayable {
    Sheet getSheet();

    String getType();

    VariantNode getTitle();

    VariantNode getCategories();

    int getSeriesCount();

    DataSeries getSeries(int i);

    ChartBinder getBinder();

    Axis getxAxis();

    Axis getyAxis();

    Axis getzAxis();
}
