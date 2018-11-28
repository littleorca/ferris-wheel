package com.ctrip.ferriswheel.quarks;


import javax.annotation.Nonnull;

/**
 *
 * This class defined the super type of all Function. 
 *
 * @param <T>
 */
public interface Function<T> extends Evaluator<T> {

    /**
     * invoke function
     * 
     * @param arguments
     * @return
     */
    T invoke(Object... arguments);
    
    /**
     * get function name
     * 
     * @return
     */
    @Nonnull
    String getName();
    

    /**
     * Returns {@code true} if this function was declared to take
     * a variable number of arguments; returns {@code false}
     * otherwise.
     *
     * @return {@code true} if an only if this method was declared to
     * take a variable number of arguments.
     */
    boolean isVarArgs();
    
}
