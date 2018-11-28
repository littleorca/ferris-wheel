package com.ctrip.ferriswheel.quarks;


import javax.annotation.Nonnull;

/**
 * The Context when execute
 * 
 * 
 * @author sqwen
 *
 */
public interface EvaluationContext<V> {

    boolean contains(@Nonnull String token);

    /**
     * Get the value from context according the specify value name.
     * 
     * @param token
     * @return
     */
    V get(@Nonnull String token);

    /**
     * Put a value to context with specify name
     * 
     * @param token the value name.
     * @param value the value.
     */
    void put(@Nonnull String token, V value);

}
