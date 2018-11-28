package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.core.intf.Placement;
import com.ctrip.ferriswheel.core.intf.Stacking;
import com.ctrip.ferriswheel.core.intf.Placement;
import com.ctrip.ferriswheel.core.intf.Stacking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Axis implements Serializable {
    private String title;
    private String label;
    private Placement placement;
    private boolean reversed = false;
    private Interval interval;
    private List<AxisBand> bands;
    private Stacking stacking;

    public Axis() {
    }

    /**
     * Construct an Axis by copying another Axis.
     *
     * @param another
     */
    public Axis(Axis another) {
        this.title = another.getTitle();
        this.label = another.getLabel();
        this.placement = another.getPlacement();
        this.reversed = another.isReversed();
        this.interval = null;
        this.bands = null;
        this.stacking = another.stacking;

        if (another.getInterval() != null) {
            this.interval = new Interval(another.getInterval());
        }

        if (another.getBands() != null) {
            this.bands = new ArrayList<>(another.getBands().size());
            for (AxisBand b : another.getBands()) {
                this.bands.add(new AxisBand(b));
            }
        }
    }

    public Axis(String title,
                String label,
                Placement placement,
                boolean reversed,
                Interval interval,
                List<AxisBand> bands,
                Stacking stacking) {
        this.title = title;
        this.label = label;
        this.placement = placement;
        this.reversed = reversed;
        this.interval = interval;
        this.bands = bands;
        this.stacking = stacking;
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

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
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
}
