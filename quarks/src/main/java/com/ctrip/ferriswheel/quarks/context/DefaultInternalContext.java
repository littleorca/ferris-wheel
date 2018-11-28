package com.ctrip.ferriswheel.quarks.context;

import com.ctrip.ferriswheel.quarks.AbstractSyntaxTree;
import com.ctrip.ferriswheel.quarks.EvaluationContext;
import com.ctrip.ferriswheel.quarks.InternalContext;

public class DefaultInternalContext<V> implements InternalContext<V> {

    private EvaluationContext<V> evaluationContext;

    private AbstractSyntaxTree<V> node;

    public DefaultInternalContext(AbstractSyntaxTree<V> node, EvaluationContext<V> evaluationContext) {
        this.evaluationContext = evaluationContext;
        this.node = node;
    }

    @Override
    public EvaluationContext<V> getEvaluationContext() {
        return evaluationContext;
    }

    @Override
    public AbstractSyntaxTree<V> getNode() {
        return node;
    }
}
