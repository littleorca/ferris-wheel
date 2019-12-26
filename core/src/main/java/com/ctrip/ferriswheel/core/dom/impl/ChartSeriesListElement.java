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

import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.core.dom.helper.TagNames;

import java.util.ArrayList;
import java.util.List;

public class ChartSeriesListElement extends AbstractElement {
    static final String TAG_NAME = TagNames.SERIES_LIST;

    @Override
    public String getTagName() {
        return TAG_NAME;
    }

    @Override
    public ChartSeriesElement getChild(String name) {
        return (ChartSeriesElement) super.getChild(name);
    }

    @Override
    public ChartSeriesElement getChild(int index) {
        return (ChartSeriesElement) super.getChild(index);
    }

    public int getSeriesCount() {
        return getChildCount();
    }

    public ChartSeriesElement getSeries(int i) {
        return getChild(i);
    }

    public List<DataSeries> getSeriesList() {
        List<DataSeries> seriesList = new ArrayList<>(getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            ChartSeriesElement child = getChild(i);
            seriesList.add(child);
        }
        return seriesList;
    }
}
