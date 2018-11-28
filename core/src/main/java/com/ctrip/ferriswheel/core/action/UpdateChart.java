package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.intf.Action;

public class UpdateChart extends ChartAction implements Action {
    private ChartData chartData;

    public UpdateChart() {
    }

    public UpdateChart(String sheetName, String chartName, ChartData chartData) {
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

    public ChartData getChartData() {
        return chartData;
    }

    public void setChartData(ChartData chartData) {
        this.chartData = chartData;
    }
}
