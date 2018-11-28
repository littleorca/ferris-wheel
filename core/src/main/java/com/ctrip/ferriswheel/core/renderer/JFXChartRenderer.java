package com.ctrip.ferriswheel.core.renderer;

import com.ctrip.ferriswheel.core.intf.Chart;
import com.ctrip.ferriswheel.core.intf.ChartRenderer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;

import java.awt.image.RenderedImage;

public class JFXChartRenderer implements ChartRenderer {

    @Override
    public boolean accepts(Chart chart) {
        return false;
    }

    @Override
    public RenderedImage render(Chart chart) {
        return null;
    }

    private void test() {
        Axis xAxis = new CategoryAxis();
        Axis yAxis = new NumberAxis();
        LineChart lineChart = new LineChart(xAxis, yAxis);
        ObservableList<XYChart.Series> data = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> series = FXCollections.observableArrayList();
        series.add(new XYChart.Data(null, 10));
        series.add(new XYChart.Data(null, 20));
        series.add(new XYChart.Data(null, 15));
        data.add(new XYChart.Series(series));
        lineChart.setData(data);
    }
}
