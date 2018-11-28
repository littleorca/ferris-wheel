package com.ctrip.ferriswheel.core.intf;

import com.ctrip.ferriswheel.core.bean.ChartData;

/**
 * Chart consultant generates suggested chart by analyzing the given sheet area.
 */
public interface ChartConsultant {
    ChartData getSuggestedChart(Table table, int left, int top, int right, int bottom);
}
