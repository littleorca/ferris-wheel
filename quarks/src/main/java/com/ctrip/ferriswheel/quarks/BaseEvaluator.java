package com.ctrip.ferriswheel.quarks;

import com.ctrip.ferriswheel.quarks.exception.QuarksEvaluationException;

public interface BaseEvaluator<V> extends Evaluator<V> {

    /**
     * Evaluate.
     * 
     * @param internalContext
     * @return
     * @throws QuarksEvaluationException
     *             QuarksAssignException
     */
    V evaluate(InternalContext<V> internalContext) throws QuarksEvaluationException;

}
