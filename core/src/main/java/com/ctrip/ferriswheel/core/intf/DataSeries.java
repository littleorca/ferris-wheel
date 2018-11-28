package com.ctrip.ferriswheel.core.intf;

/**
 * Data series for charts
 */
public interface DataSeries {
    /**
     * Get chart which this series belongs to.
     *
     * @return
     */
    Chart getChart();

    /**
     * Get series name.
     *
     * @return
     */
    VariantNode getName();

    /**
     * Get X-values.
     *
     * @return
     */
    VariantNode getxValues();

    /**
     * Get Y-values.
     *
     * @return
     */
    VariantNode getyValues();
}
