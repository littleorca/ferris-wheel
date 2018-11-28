package com.ctrip.ferriswheel.quarks;

/**
 * Created by sqwen on 2016/4/12.
 */
public interface InternalContextBuilder<V> {

    InternalContext<V> build(AbstractSyntaxTree<V> node, EvaluationContext<V> context);

}
