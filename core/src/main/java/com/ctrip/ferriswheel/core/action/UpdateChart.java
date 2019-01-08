package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.chart.Chart;

public class UpdateChart extends ChartAction implements Action {
    private Chart chartData;

    public UpdateChart() {
    }

    public UpdateChart(String sheetName, String chartName, Chart chartData) {
        super(sheetName, chartName);
        this.chartData = chartData;
    }

    @Override
    public String toString() {
        return "UpdateChart{" +
                "sheetName='" + getSheetName() + '\'' +
                ", chartName='" + getChartName() + '\'' +
                ", chartData=" + chartData +
                '}';
    }

    public Chart getChartData() {
        return chartData;
    }

    public void setChartData(Chart chartData) {
        this.chartData = chartData;
    }
}
