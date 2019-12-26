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
import com.ctrip.ferriswheel.common.chart.AxisBand;
import com.ctrip.ferriswheel.common.chart.Interval;
import com.ctrip.ferriswheel.common.chart.Stacking;
import com.ctrip.ferriswheel.common.view.Placement;
import com.ctrip.ferriswheel.core.dom.helper.AttributeSerializer;

import java.util.List;

public abstract class AbstractChartAxisElement extends AbstractElement implements Axis {
    private static final String ATTR_TITLE = "title";
    private static final String ATTR_LABEL = "label";
    private static final String ATTR_PLACEMENT = "placement";
    private static final String ATTR_REVERSED = "reversed";
    private static final String ATTR_INTERVAL = "interval";
    private static final String ATTR_STACKING = "stacking";
    private static final String ATTR_FORMAT = "format";

    @Override
    public String getTitle() {
        return getAttribute(ATTR_TITLE);
    }

    @Override
    public String getLabel() {
        return getAttribute(ATTR_LABEL);
    }

    @Override
    public Placement getPlacement() {
        String attrValue = getAttribute(ATTR_PLACEMENT);
        return attrValue == null ? null : Placement.valueOf(attrValue);
    }

    @Override
    public boolean isReversed() {
        String attrValue = getAttribute(ATTR_REVERSED);
        return attrValue == null ? false : Boolean.parseBoolean(attrValue);
    }

    @Override
    public Interval getInterval() {
        String attrValue = getAttribute(ATTR_INTERVAL);
        return attrValue == null ? null : new AttributeSerializer().deserializeInterval(attrValue);
    }

    @Override
    public List<AxisBand> getBands() {
        ChartAxisBandListElement bandListElement = firstChild(ChartAxisBandListElement.class);
        return bandListElement == null ? null : bandListElement.getBands();
    }

    @Override
    public Stacking getStacking() {
        String attrValue = getAttribute(ATTR_STACKING);
        return attrValue == null ? null : Stacking.valueOf(attrValue);
    }

    @Override
    public String getFormat() {
        return getAttribute(ATTR_FORMAT);
    }
}
