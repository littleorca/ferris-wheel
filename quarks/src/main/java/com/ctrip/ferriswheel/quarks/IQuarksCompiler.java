package com.ctrip.ferriswheel.quarks;

/**
 * Created by sqwen on 2016/4/12.
 */
public interface IQuarksCompiler<V> {

    InternalContextBuilder<V> getInternalContextBuilder();

}
