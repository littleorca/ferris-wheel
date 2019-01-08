package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;

public abstract class ChartAction extends SheetAction implements Action {
    private String chartName;

    ChartAction() {
    }

    ChartAction(String sheetName, String chartName) {
        super(sheetName);
        this.chartName = chartName;
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }
}
