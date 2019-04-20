package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.common.action.Action;
import com.ctrip.ferriswheel.common.chart.Chart;

public final class AddChart extends SheetAction implements Action {
    private Chart chartData;

    public AddChart() {
    }

    public AddChart(String sheetName, Chart chartData) {
        super(sheetName);
        this.chartData = chartData;
    }

    @Override
    public String toString() {
        return "AddChart{" +
                "sheetName='" + getSheetName() + '\'' +
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
