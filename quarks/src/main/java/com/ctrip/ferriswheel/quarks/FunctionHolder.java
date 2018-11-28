package com.ctrip.ferriswheel.quarks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;


public interface FunctionHolder {

    /**
     * get function
     *
     * @param name
     * @return
     */
    @Nullable
    Function<?> getFunction(String name);

    /**
     * register function
     *
     * @param fun
     */
    void registerFunction(@Nonnull Function<?> fun);

    /**
     * register function
     *
     * @param fun
     */
    void registerFunction(@Nonnull String name, @Nonnull Function<?> fun);

    /**
     * @param name
     * @return
     */
    @Nullable
    Function<?> unregisterFunction(@Nonnull String name);


    /**
     * according function name to unregister
     *
     * @param fun
     * @return
     */
    @Nullable
    Function<?> unregisterFunction(@Nonnull Function<?> fun);

    /**
     * get all non-kernel function names from current FunctionHolder;
     *
     * @return
     */
    @Nonnull
    Set<String> getFunctionNames();

    /**
     * get all kernel function name from current FunctionHolder;
     *
     * @return
     */
    @Nonnull
    Set<String> getKernelFunctionNames();
}
