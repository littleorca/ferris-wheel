package com.ctrip.ferriswheel.quarks;

/**
 * Created by sqwen on 2016/4/12.
 */
public interface InternalContext<V> {

    /**
     *
     * @return
     */
    EvaluationContext<V> getEvaluationContext();

    /**
     * Get the node
     * @return
     */
    AbstractSyntaxTree<V> getNode();

}
