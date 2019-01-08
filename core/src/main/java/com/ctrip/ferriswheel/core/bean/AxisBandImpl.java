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

import com.ctrip.ferriswheel.common.chart.AxisBand;

import java.io.Serializable;

public class AxisBandImpl implements AxisBand, Serializable {
    private IntervalImpl interval;
    private String label;
    private ColorImpl color;

    public AxisBandImpl() {
    }

    /**
     * Construct an AxisBand by copying another AxisBand
     *
     * @param another
     */
    public AxisBandImpl(AxisBand another) {
        this.interval = null;
        this.label = another.getLabel();
        this.color = null;

        if (another.getInterval() != null) {
            this.interval = new IntervalImpl(another.getInterval());
        }
        if (another.getColor() != null) {
            this.color = new ColorImpl(another.getColor());
        }
    }

    public AxisBandImpl(IntervalImpl interval, String label, ColorImpl color) {
        this.interval = interval;
        this.label = label;
        this.color = color;
    }

    public IntervalImpl getInterval() {
        return interval;
    }

    public void setInterval(IntervalImpl interval) {
        this.interval = interval;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ColorImpl getColor() {
        return color;
    }

    public void setColor(ColorImpl color) {
        this.color = color;
    }
}
