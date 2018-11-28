package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.intf.*;
import com.ctrip.ferriswheel.core.view.Layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChartData implements Serializable {
    private String type;
    private DynamicValue title;
    private DynamicValue categories;
    private List<Series> seriesList = new ArrayList<>();
    private Layout layout;
    private Binder binder;
    private Axis xAxis;
    private Axis yAxis;
    private Axis zAxis;

    public ChartData() {
    }

    public ChartData(Chart chart) {
        this.type = chart.getType();
        this.title = new DynamicValue(chart.getTitle());
        this.categories = new DynamicValue(chart.getCategories());
        this.seriesList = new ArrayList<>(chart.getSeriesCount());
        for (int i = 0; i < chart.getSeriesCount(); i++) {
            this.seriesList.add(new ChartData.Series(chart.getSeries(i)));
        }
        this.layout = new Layout(chart.getLayout());
        if (chart.getBinder() != null) {
            this.binder = new Binder(chart.getBinder());
        }
        if (chart.getxAxis() != null) {
            this.xAxis = new Axis(chart.getxAxis());
        }
        if (chart.getyAxis() != null) {
            this.yAxis = new Axis(chart.getyAxis());
        }
        if (chart.getzAxis() != null) {
            this.zAxis = new Axis(chart.getzAxis());
        }
    }

    public ChartData(String type,
                     DynamicValue title,
                     DynamicValue categories,
                     List<Series> seriesList) {
        this(type, title, categories, seriesList, null, null, null, null, null);
    }

    public ChartData(String type,
                     DynamicValue title,
                     DynamicValue categories,
                     List<Series> seriesList,
                     Layout layout,
                     Binder binder,
                     Axis xAxis,
                     Axis yAxis,
                     Axis zAxis) {
        this.type = type;
        this.title = title;
        this.categories = categories;
        this.seriesList = seriesList;
        this.layout = layout;
        this.binder = binder;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public DynamicValue getTitle() {
        return title;
    }

    public void setTitle(DynamicValue title) {
        this.title = title;
    }

    public DynamicValue getCategories() {
        return categories;
    }

    public void setCategories(DynamicValue categories) {
        this.categories = categories;
    }

    public List<Series> getSeriesList() {
        return seriesList;
    }

    public void setSeriesList(List<Series> seriesList) {
        this.seriesList = seriesList;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public Binder getBinder() {
        return binder;
    }

    public void setBinder(Binder binder) {
        this.binder = binder;
    }

    public Axis getxAxis() {
        return xAxis;
    }

    public void setxAxis(Axis xAxis) {
        this.xAxis = xAxis;
    }

    public Axis getyAxis() {
        return yAxis;
    }

    public void setyAxis(Axis yAxis) {
        this.yAxis = yAxis;
    }

    public Axis getzAxis() {
        return zAxis;
    }

    public void setzAxis(Axis zAxis) {
        this.zAxis = zAxis;
    }

    public static class Series implements Serializable {
        private DynamicValue name;
        private DynamicValue xValues;
        private DynamicValue yValues;

        public Series() {
        }

        public Series(DataSeries series) {
            if (series.getName() != null) {
                this.name = new DynamicValue(series.getName());
            }
            if (series.getxValues() != null) {
                this.xValues = new DynamicValue(series.getxValues());
            }
            if (series.getyValues() != null) {
                this.yValues = new DynamicValue(series.getyValues());
            }
        }

        public Series(DynamicValue name, DynamicValue xValues, DynamicValue yValues) {
            this.name = name;
            this.xValues = xValues;
            this.yValues = yValues;
        }

        public DynamicValue getName() {
            return name;
        }

        public void setName(DynamicValue name) {
            this.name = name;
        }

        public DynamicValue getxValues() {
            return xValues;
        }

        public void setxValues(DynamicValue xValues) {
            this.xValues = xValues;
        }

        public DynamicValue getyValues() {
            return yValues;
        }

        public void setyValues(DynamicValue yValues) {
            this.yValues = yValues;
        }
    }

    public static class Binder implements Serializable {
        private DynamicValue data;
        private Orientation orientation;
        private Placement categoriesPlacement;
        private Placement seriesNamePlacement;

        public Binder() {
        }

        public Binder(ChartBinder chartBinder) {
            this(new DynamicValue(chartBinder.getData()),
                    chartBinder.getOrientation(),
                    chartBinder.getCategoriesPlacement(),
                    chartBinder.getSeriesNamePlacement());
        }

        public Binder(DynamicValue data,
                      Orientation orientation,
                      Placement categoriesPlacement,
                      Placement seriesNamePlacement) {
            this.data = data;
            this.orientation = orientation;
            this.categoriesPlacement = categoriesPlacement;
            this.seriesNamePlacement = seriesNamePlacement;
        }

        public DynamicValue getData() {
            return data;
        }

        public void setData(DynamicValue data) {
            this.data = data;
        }

        public Orientation getOrientation() {
            return orientation;
        }

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
        }

        public Placement getCategoriesPlacement() {
            return categoriesPlacement;
        }

        public void setCategoriesPlacement(Placement categoriesPlacement) {
            this.categoriesPlacement = categoriesPlacement;
        }

        public Placement getSeriesNamePlacement() {
            return seriesNamePlacement;
        }

        public void setSeriesNamePlacement(Placement seriesNamePlacement) {
            this.seriesNamePlacement = seriesNamePlacement;
        }
    }

}
