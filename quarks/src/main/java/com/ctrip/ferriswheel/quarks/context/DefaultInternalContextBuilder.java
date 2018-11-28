package com.ctrip.ferriswheel.quarks.context;

import com.ctrip.ferriswheel.quarks.AbstractSyntaxTree;
import com.ctrip.ferriswheel.quarks.EvaluationContext;
import com.ctrip.ferriswheel.quarks.InternalContext;
import com.ctrip.ferriswheel.quarks.InternalContextBuilder;

/**
 * Created by sqwen on 2017/9/18.
 */
public class DefaultInternalContextBuilder<V> implements InternalContextBuilder<V> {
    @Override
    public InternalContext<V> build(AbstractSyntaxTree<V> node, EvaluationContext<V> context) {
        return new DefaultInternalContext<>(node, context);
    }
}
