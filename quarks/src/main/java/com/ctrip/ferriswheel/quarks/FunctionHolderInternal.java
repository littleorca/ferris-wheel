package com.ctrip.ferriswheel.quarks;

public interface FunctionHolderInternal extends FunctionHolder {

    /**
     * register function
     * 
     * @param fun
     */
    void registerKernelFunction(Function<?> fun);
    
}
