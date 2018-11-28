package com.ctrip.ferriswheel.core.intf;

public interface ChartBinder {
    DynamicVariant getData();

    Orientation getOrientation();

    Placement getCategoriesPlacement();

    Placement getSeriesNamePlacement();
}
