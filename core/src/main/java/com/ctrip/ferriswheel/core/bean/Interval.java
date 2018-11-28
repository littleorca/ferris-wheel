package com.ctrip.ferriswheel.core.bean;

import java.io.Serializable;

public class Interval implements Serializable {
    private double from;
    private double to;

    public Interval() {
    }

    /**
     * Construct an Interval by copying another Interval.
     *
     * @param another
     */
    public Interval(Interval another) {
        this(another.from, another.to);
    }

    public Interval(double from, double to) {
        this.from = from;
        this.to = to;
    }

    public double getFrom() {
        return from;
    }

    public void setFrom(double from) {
        this.from = from;
    }

    public double getTo() {
        return to;
    }

    public void setTo(double to) {
        this.to = to;
    }
}
