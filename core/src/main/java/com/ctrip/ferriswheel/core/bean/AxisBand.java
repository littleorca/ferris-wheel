package com.ctrip.ferriswheel.core.bean;

import java.io.Serializable;

public class AxisBand implements Serializable {
    private Interval interval;
    private String label;
    private Color color;

    public AxisBand() {
    }

    /**
     * Construct an AxisBand by copying another AxisBand
     *
     * @param another
     */
    public AxisBand(AxisBand another) {
        this.interval = null;
        this.label = another.getLabel();
        this.color = null;

        if (another.getInterval() != null) {
            this.interval = new Interval(another.getInterval());
        }
        if (another.getColor() != null) {
            this.color = new Color(another.getColor());
        }
    }

    public AxisBand(Interval interval, String label, Color color) {
        this.interval = interval;
        this.label = label;
        this.color = color;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
