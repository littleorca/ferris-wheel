package com.ctrip.ferriswheel.quarks.util;

import com.ctrip.ferriswheel.quarks.AbstractSyntaxTree;
import com.ctrip.ferriswheel.quarks.Evaluator;
import com.ctrip.ferriswheel.quarks.EvaluatorHolder;
import com.ctrip.ferriswheel.quarks.annotation.Production;
import com.ctrip.ferriswheel.quarks.annotation.Scope;
import com.ctrip.ferriswheel.quarks.exception.QuarksEvaluationNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sqwen on 2016/11/28.
 */
public class ProductionEvaluatorRegistry<V> implements EvaluatorHolder<V> {

    /**
     * save unique evaluator object
     */
    private final Map<String, Evaluator<V>> _eval_object_map;

    /**
     * save evaluator class.
     */
    protected final Map<String, EvaluatorMeta> _eval_meta_map;

    public ProductionEvaluatorRegistry() {
        _eval_object_map = new HashMap<>();
        _eval_meta_map = new HashMap<>();
    }

    /**
     * Register
     * @param type
     */
    public void registerEvaluatorClass(Class<? extends Evaluator<V>> type) {
        Production production = type.getAnnotation(Production.class);
        if(production == null) {
            throw new RuntimeException("Annotation 'Production' has not found.");
        }
        for(String prod : production.value()) {
            if (_eval_meta_map.containsKey(prod)) {
                throw new RuntimeException("Production name '" + prod + "' conflict between classes "
                        + _eval_meta_map.get(prod).type.getName()
                        + " and "
                        + type.getName());
            }
            EvaluatorMeta meta = new EvaluatorMeta();
            meta.type = type;
            meta.scope = production.scope();
            _eval_meta_map.put(prod, meta);
        }
    }

    /**
     * get Evaluator from registry according expression
     *
     * @param expression
     * @return
     */
    private Evaluator<V> getCoreEvaluator(String expression) {
        EvaluatorMeta meta = _eval_meta_map.get(expression);
        if(meta == null || meta.type == null) {
            return null;
        }
        Class<? extends Evaluator> type = meta.type;
        Evaluator<V> eval;
        if(Scope.singleton.equals(meta.scope)) {
            eval = _eval_object_map.get(expression);
            if (eval == null) {
                synchronized (_eval_object_map) {
                    eval = _eval_object_map.get(expression);
                    if(eval == null) {
                        try {
                            _eval_object_map.put(expression, eval = type.newInstance());
                        } catch (Exception e) {
                            throw new RuntimeException("Instance Evaluator(" + type.getName() + ") error.");
                        }
                    }
                }
            }
        } else {
            try {
                eval =  type.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Instance " + type.getName() + " error.");
            }
        }
        return eval;
    }

    @Override
    public Evaluator<V> getEvaluator(AbstractSyntaxTree<V> ast) throws QuarksEvaluationNotFoundException {
        try {
            return getCoreEvaluator(ast.getHandle().getSymbol());
        } catch (Exception e) {
            throw new QuarksEvaluationNotFoundException(
                    "Evaluation not found for at expression : " + ASTUtil.dumpExp(ast), e);
        }
    }

    class EvaluatorMeta<E extends Evaluator<V>> {

        Class<E> type;

        Scope scope;

    }
}
