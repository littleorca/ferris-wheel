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

import com.ctrip.ferriswheel.api.chart.Interval;

import java.io.Serializable;

public class IntervalImpl implements Interval, Serializable {
    private double from;
    private double to;

    public IntervalImpl() {
    }

    /**
     * Construct an Interval by copying another Interval.
     *
     * @param another
     */
    public IntervalImpl(Interval another) {
        this(another.getFrom(), another.getTo());
    }

    public IntervalImpl(double from, double to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public double getFrom() {
        return from;
    }

    public void setFrom(double from) {
        this.from = from;
    }

    @Override
    public double getTo() {
        return to;
    }

    public void setTo(double to) {
        this.to = to;
    }
}
