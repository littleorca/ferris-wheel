package com.ctrip.ferriswheel.quarks;


public interface Evaluator<V> {

    /**
     * Determine if this evaluator is volatile. An evaluator is volatile if it
     * generates different results during calling with the same parameter(s).
     * <p>
     * For example, suppose function now() always give out the current time,
     * then it is a volatile function.
     * 
     * @return
     */
    boolean isVolatile();

    /**
     * Determine if the ceiling of the result is decidable.
     * 
     * @return
     */
    boolean isCeilDecidable();

    /**
     * Determine if the floor of the result is decidable.
     * 
     * @return
     */
    boolean isFloorDecidable();

    /**
     * Get the ceiling of the result according to the current state.
     * 
     * @return
     */
    V getCeiling();

    /**
     * Get the floor of the result according to the current state.
     * 
     * @return
     */
    V getFloor();;
}
