package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.api.action.Action;
import com.ctrip.ferriswheel.api.chart.Chart;

public final class AddChart extends SheetAction implements Action {
    private String chartName;
    private Chart chartData;

    public AddChart() {
    }

    public AddChart(String sheetName, String chartName, Chart chartData) {
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

    public Chart getChartData() {
        return chartData;
    }

    public void setChartData(Chart chartData) {
        this.chartData = chartData;
    }
}
