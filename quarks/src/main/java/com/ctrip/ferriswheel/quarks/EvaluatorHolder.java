package com.ctrip.ferriswheel.quarks;

import com.ctrip.ferriswheel.quarks.exception.QuarksEvaluationNotFoundException;

public interface EvaluatorHolder<V> {

	/**
	 * get Evaluator from registry according AbstractSyntaxTree
	 * 
	 * @param ast
	 * @return
	 * @throws QuarksEvaluationNotFoundException
	 */
	Evaluator<V> getEvaluator(AbstractSyntaxTree<V> ast) throws QuarksEvaluationNotFoundException;

}
