package com.ctrip.ferriswheel.core.intf;

/**
 * Variant node holds variant and optionally a formula.
 * It has asset ID for convenience to trace dependencies.
 */
public interface VariantNode extends DynamicVariant, Asset {

    /**
     * Get node value. If it is a formula node, the returned value represents
     * the evaluated result of the formula.
     *
     * @return
     */
    Variant getValue();

    /**
     * Determine whether this node is a formula node or not.
     *
     * @return
     */
    boolean isFormula();

//    /**
//     * Determine whether the evaluated result is out of date or not.
//     *
//     * @return
//     */
//    boolean isDirty();

}
