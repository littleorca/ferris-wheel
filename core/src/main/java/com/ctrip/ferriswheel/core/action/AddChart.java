package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.intf.Action;

public final class AddChart extends SheetAction implements Action {
    private String chartName;
    private ChartData chartData;

    public AddChart() {
    }

    public AddChart(String sheetName, String chartName, ChartData chartData) {
        super(sheetName);
        this.chartName = chartName;
        this.chartData = chartData;
    }

    @Override
    public String toString() {
        return "AddChart{" +
                "sheetName='" + getSheetName() + '\'' +
                ", chartName='" + chartName + '\'' +
                ", chartData=" + chartData +
                '}';
    }

    public String getChartName() {
        return chartName;
    }

    public void setChartName(String chartName) {
        this.chartName = chartName;
    }

    public ChartData getChartData() {
        return chartData;
    }

    public void setChartData(ChartData chartData) {
        this.chartData = chartData;
    }
}
