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

import com.ctrip.ferriswheel.common.chart.Axis;
import com.ctrip.ferriswheel.common.chart.AxisBand;
import com.ctrip.ferriswheel.common.chart.Stacking;
import com.ctrip.ferriswheel.common.view.Placement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AxisImpl implements Axis, Serializable {
    private String title;
    private String label;
    private Placement placement;
    private boolean reversed = false;
    private IntervalImpl interval;
    private List<AxisBand> bands;
    private Stacking stacking;
    private String format;

    public AxisImpl() {
    }

    /**
     * Construct an Axis by copying another Axis.
     *
     * @param another
     */
    public AxisImpl(Axis another) {
        this.title = another.getTitle();
        this.label = another.getLabel();
        this.placement = another.getPlacement();
        this.reversed = another.isReversed();
        this.interval = null;
        this.bands = null;
        this.stacking = another.getStacking();
        this.format = another.getFormat();

        if (another.getInterval() != null) {
            this.interval = new IntervalImpl(another.getInterval());
        }

        if (another.getBands() != null) {
            this.bands = new ArrayList<>(another.getBands().size());
            for (AxisBand b : another.getBands()) {
                this.bands.add(new AxisBandImpl(b));
            }
        }
    }

    public AxisImpl(String title,
                    String label,
                    Placement placement,
                    boolean reversed,
                    IntervalImpl interval,
                    List<AxisBand> bands,
                    Stacking stacking,
                    String format) {
        this.title = title;
        this.label = label;
        this.placement = placement;
        this.reversed = reversed;
        this.interval = interval;
        this.bands = bands;
        this.stacking = stacking;
        this.format = format;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Placement getPlacement() {
        return placement;
    }

    public void setPlacement(Placement placement) {
        this.placement = placement;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public IntervalImpl getInterval() {
        return interval;
    }

    public void setInterval(IntervalImpl interval) {
        this.interval = interval;
    }

    public List<AxisBand> getBands() {
        return bands;
    }

    public void setBands(List<AxisBand> bands) {
        this.bands = bands;
    }

    public Stacking getStacking() {
        return stacking;
    }

    public void setStacking(Stacking stacking) {
        this.stacking = stacking;
    }

    @Override
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
