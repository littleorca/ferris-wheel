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

import com.ctrip.ferriswheel.common.chart.Interval;
import com.ctrip.ferriswheel.common.view.Color;
import com.ctrip.ferriswheel.core.dom.AxisBandElement;
import com.ctrip.ferriswheel.core.dom.helper.AttributeSerializer;

public final class AxisBandElementImpl extends AbstractElement implements AxisBandElement {
    private static final String ATTR_INTERVAL = "interval";
    private static final String ATTR_LABEL = "label";
    private static final String ATTR_COLOR = "color";

    protected AxisBandElementImpl(AbstractDocument ownerDocument) {
        super(ownerDocument);
    }

    @Override
    public Interval getInterval() {
        String attrValue = getAttribute(ATTR_INTERVAL);
        return attrValue == null ? null : new AttributeSerializer().deserializeInterval(attrValue);
    }

    @Override
    public String getLabel() {
        return getAttribute(ATTR_LABEL);
    }

    @Override
    public Color getColor() {
        String attrValue = getAttribute(ATTR_COLOR);
        return attrValue == null ? null : new AttributeSerializer().deserializeColor(attrValue);
    }
}
