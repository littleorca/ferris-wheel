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
import com.ctrip.ferriswheel.common.chart.ChartBinder;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.core.dom.CategoriesElement;
import com.ctrip.ferriswheel.core.dom.ChartElement;
import com.ctrip.ferriswheel.core.dom.DataSeriesListElement;
import com.ctrip.ferriswheel.core.dom.TitleElement;

import java.util.List;

public final class ChartElementImpl extends AbstractLayoutElement implements ChartElement {
    protected static final String ATTR_TYPE = "type";

    protected ChartElementImpl(AbstractDocument ownerDocument) {
        super(ownerDocument);
    }

    @Override
    public String getType() {
        return getAttribute(ATTR_TYPE);
    }

    @Override
    public void setType(String type) {
        setAttribute(ATTR_TYPE, type);
    }

    @Override
    public DynamicVariant getTitle() {
        return mapFirstChild(TitleElementImpl.class, TitleElementImpl::getVariant, null);
    }

    @Override
    public void setTitle(DynamicVariant title) {
        TitleElement titleElement = firstChild(TitleElementImpl.class);
        if (titleElement == null) {
            titleElement = getOwnerDocument().createElement(TitleElement.class);
            insertChild(titleElement, null);
        }
        titleElement.setVariant(title);
    }

    @Override
    public DynamicVariant getCategories() {
        return mapFirstChild(CategoriesElementImpl.class, CategoriesElementImpl::getVariant, null);
    }

    @Override
    public void setCategories(DynamicVariant categories) {
        CategoriesElement categoriesElement = firstChild(CategoriesElementImpl.class);
        if (categoriesElement == null) {
            categoriesElement = getOwnerDocument().createElement(CategoriesElement.class);
            appendChild(categoriesElement);
        }
        categoriesElement.setVariant(categories);
    }

    @Override
    public int getSeriesCount() {
        return mapFirstChild(DataSeriesListElementImpl.class, DataSeriesListElement::getSeriesCount, 0);
    }

    @Override
    public DataSeriesElementImpl getSeries(int i) {
        DataSeriesElementImpl series = mapFirstChild(DataSeriesListElementImpl.class,
                el -> el.getSeries(i), null);
        if (series == null) {
            throw new IllegalStateException();
        }
        return series;
    }

    @Override
    public List<? extends DataSeries> getSeriesList() {
        return mapFirstChild(DataSeriesListElementImpl.class, DataSeriesListElementImpl::getSeriesList, null);
    }

    @Override
    public ChartBinder getBinder() {
        return firstChild(ChartBinderElementImpl.class);
    }

    @Override
    public Axis getxAxis() {
        return firstChild(XAxisElementImpl.class);
    }

    @Override
    public Axis getyAxis() {
        return firstChild(YAxisElementImpl.class);
    }

    @Override
    public Axis getzAxis() {
        return firstChild(ZAxisElementImpl.class);
    }

    @Override
    public String getName() {
        return getAttribute(ATTR_NAME);
    }
}
