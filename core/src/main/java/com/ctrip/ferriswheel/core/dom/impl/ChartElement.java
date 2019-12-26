/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
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
 */

package com.ctrip.ferriswheel.core.dom.impl;

import com.ctrip.ferriswheel.common.chart.Axis;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.ChartBinder;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.core.dom.helper.TagNames;

import java.util.List;

public class ChartElement extends AbstractLayoutElement implements Chart {
    static final String TAG_NAME = TagNames.CHART;

    protected static final String ATTR_TYPE = "type";

    @Override
    public String getTagName() {
        return TAG_NAME;
    }

    @Override
    public String getType() {
        return getAttribute(ATTR_TYPE);
    }

    public void setType(String type) {
        setAttribute(ATTR_TYPE, type);
    }

    @Override
    public DynamicVariant getTitle() {
        return mapFirstChild(TitleElement.class, TitleElement::getVariant, null);
    }

    public void setTitle(DynamicValue title) {
        TitleElement titleElement = firstChild(TitleElement.class);
        if (titleElement == null) {
            titleElement = getOwnerDocument().createElement(TitleElement.class);
            insertChild(titleElement, null);
        }
        titleElement.setVariant(title);
    }

    @Override
    public DynamicVariant getCategories() {
        return mapFirstChild(ChartCategoriesElement.class, ChartCategoriesElement::getVariant, null);
    }

    public void setCategories(DynamicValue categories) {
        ChartCategoriesElement categoriesElement = firstChild(ChartCategoriesElement.class);
        if (categoriesElement == null) {
            categoriesElement = getOwnerDocument().createElement(ChartCategoriesElement.class);
            appendChild(categoriesElement);
        }
        categoriesElement.setVariant(categories);
    }

    @Override
    public int getSeriesCount() {
        return mapFirstChild(ChartSeriesListElement.class, ChartSeriesListElement::getSeriesCount, 0);
    }

    @Override
    public ChartSeriesElement getSeries(int i) {
        ChartSeriesElement series = mapFirstChild(ChartSeriesListElement.class,
                el -> el.getSeries(i), null);
        if (series == null) {
            throw new IllegalStateException();
        }
        return series;
    }

    @Override
    public List<DataSeries> getSeriesList() {
        return mapFirstChild(ChartSeriesListElement.class, ChartSeriesListElement::getSeriesList, null);
    }

    @Override
    public ChartBinder getBinder() {
        return firstChild(ChartBinderElement.class);
    }

    @Override
    public Axis getxAxis() {
        return firstChild(ChartXAxisElement.class);
    }

    @Override
    public Axis getyAxis() {
        return firstChild(ChartYAxisElement.class);
    }

    @Override
    public Axis getzAxis() {
        return firstChild(ChartZAxisElement.class);
    }

    @Override
    public String getName() {
        return getAttribute(ATTR_NAME);
    }
}
