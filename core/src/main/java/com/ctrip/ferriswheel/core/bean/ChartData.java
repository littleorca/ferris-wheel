/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.ChartBinder;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.common.view.Orientation;
import com.ctrip.ferriswheel.common.view.Placement;
import com.ctrip.ferriswheel.core.view.LayoutImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChartData implements Chart, Serializable {
    private String name;
    private String type;
    private DynamicValue title;
    private DynamicValue categories;
    private List<DataSeries> seriesList = new ArrayList<>();
    private LayoutImpl layout;
    private BinderImpl binder;
    private AxisImpl xAxis;
    private AxisImpl yAxis;
    private AxisImpl zAxis;

    public ChartData() {
    }

    public ChartData(Chart chart) {
        this.name = chart.getName();
        this.type = chart.getType();
        this.title = new DynamicValue(chart.getTitle());
        this.categories = new DynamicValue(chart.getCategories());
        this.seriesList = new ArrayList<>(chart.getSeriesCount());
        for (int i = 0; i < chart.getSeriesCount(); i++) {
            this.seriesList.add(new SeriesImpl(chart.getSeries(i)));
        }
        this.layout = new LayoutImpl(chart.getLayout());
        if (chart.getBinder() != null) {
            this.binder = new BinderImpl(chart.getBinder());
        }
        if (chart.getxAxis() != null) {
            this.xAxis = new AxisImpl(chart.getxAxis());
        }
        if (chart.getyAxis() != null) {
            this.yAxis = new AxisImpl(chart.getyAxis());
        }
        if (chart.getzAxis() != null) {
            this.zAxis = new AxisImpl(chart.getzAxis());
        }
    }

    public ChartData(String name,
                     String type,
                     DynamicValue title,
                     DynamicValue categories,
                     List<DataSeries> seriesList) {
        this(name, type, title, categories, seriesList, null, null, null, null, null);
    }

    public ChartData(String name,
                     String type,
                     DynamicValue title,
                     DynamicValue categories,
                     List<DataSeries> seriesList,
                     LayoutImpl layout,
                     BinderImpl binder,
                     AxisImpl xAxis,
                     AxisImpl yAxis,
                     AxisImpl zAxis) {
        this.name = name;
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

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public DynamicValue getTitle() {
        return title;
    }

    @Override
    public void setTitle(DynamicVariant title) {
        this.title = DynamicValue.of(title);
    }

    @Override
    public DynamicValue getCategories() {
        return categories;
    }

    @Override
    public void setCategories(DynamicVariant categories) {
        this.categories = DynamicValue.of(categories);
    }

    @Override
    public int getSeriesCount() {
        return seriesList == null || seriesList.isEmpty() ? 0 : seriesList.size();
    }

    @Override
    public DataSeries getSeries(int i) {
        if (seriesList == null) {
            return null;
        }
        return seriesList.get(i);
    }

    @Override
    public List<DataSeries> getSeriesList() {
        return seriesList;
    }

    public void setSeriesList(List<DataSeries> seriesList) {
        this.seriesList = seriesList;
    }

    @Override
    public LayoutImpl getLayout() {
        return layout;
    }

    public void setLayout(LayoutImpl layout) {
        this.layout = layout;
    }

    @Override
    public BinderImpl getBinder() {
        return binder;
    }

    public void setBinder(BinderImpl binder) {
        this.binder = binder;
    }

    @Override
    public AxisImpl getxAxis() {
        return xAxis;
    }

    public void setxAxis(AxisImpl xAxis) {
        this.xAxis = xAxis;
    }

    @Override
    public AxisImpl getyAxis() {
        return yAxis;
    }

    public void setyAxis(AxisImpl yAxis) {
        this.yAxis = yAxis;
    }

    @Override
    public AxisImpl getzAxis() {
        return zAxis;
    }

    public void setzAxis(AxisImpl zAxis) {
        this.zAxis = zAxis;
    }

    public static class SeriesImpl implements DataSeries, Serializable {
        private DynamicValue name;
        private DynamicValue xValues;
        private DynamicValue yValues;

        public SeriesImpl() {
        }

        public SeriesImpl(DataSeries series) {
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

        public SeriesImpl(DynamicValue name, DynamicValue xValues, DynamicValue yValues) {
            this.name = name;
            this.xValues = xValues;
            this.yValues = yValues;
        }

        @Override
        public DynamicValue getName() {
            return name;
        }

        public void setName(DynamicVariant name) {
            this.name = name instanceof DynamicValue ? (DynamicValue) name : new DynamicValue(name);
        }

        @Override
        public DynamicValue getxValues() {
            return xValues;
        }

        public void setxValues(DynamicVariant xValues) {
            this.xValues = xValues instanceof DynamicValue ? (DynamicValue) xValues : new DynamicValue(xValues);
        }

        @Override
        public DynamicValue getyValues() {
            return yValues;
        }

        public void setyValues(DynamicVariant yValues) {
            this.yValues = yValues instanceof DynamicValue ? (DynamicValue) yValues : new DynamicValue(yValues);
        }
    }

    public static class BinderImpl implements ChartBinder, Serializable {
        private DynamicValue data;
        private Orientation orientation;
        private Placement categoriesPlacement;
        private Placement seriesNamePlacement;

        public BinderImpl() {
        }

        public BinderImpl(ChartBinder chartBinder) {
            this(new DynamicValue(chartBinder.getData()),
                    chartBinder.getOrientation(),
                    chartBinder.getCategoriesPlacement(),
                    chartBinder.getSeriesNamePlacement());
        }

        public BinderImpl(DynamicValue data,
                          Orientation orientation,
                          Placement categoriesPlacement,
                          Placement seriesNamePlacement) {
            this.data = data;
            this.orientation = orientation;
            this.categoriesPlacement = categoriesPlacement;
            this.seriesNamePlacement = seriesNamePlacement;
        }

        @Override
        public DynamicValue getData() {
            return data;
        }

        public void setData(DynamicValue data) {
            this.data = data;
        }

        @Override
        public Orientation getOrientation() {
            return orientation;
        }

        public void setOrientation(Orientation orientation) {
            this.orientation = orientation;
        }

        @Override
        public Placement getCategoriesPlacement() {
            return categoriesPlacement;
        }

        public void setCategoriesPlacement(Placement categoriesPlacement) {
            this.categoriesPlacement = categoriesPlacement;
        }

        @Override
        public Placement getSeriesNamePlacement() {
            return seriesNamePlacement;
        }

        public void setSeriesNamePlacement(Placement seriesNamePlacement) {
            this.seriesNamePlacement = seriesNamePlacement;
        }
    }

}
