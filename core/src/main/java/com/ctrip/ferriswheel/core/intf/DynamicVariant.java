package com.ctrip.ferriswheel.core.intf;

/**
 * Dynamic variant extends {@link Variant} with adding a formula string.
 * If a dynamic variant has specified a formula, the final value will be
 * actually the calculated result of the formula.
 *
 * @see Variant
 */
public interface DynamicVariant extends Variant {

    /**
     * Determine if this dynamic variant is a formula.
     *
     * @return
     */
    boolean isFormula();

    /**
     * Get formula string.
     *
     * @return
     */
    String getFormulaString();

}
