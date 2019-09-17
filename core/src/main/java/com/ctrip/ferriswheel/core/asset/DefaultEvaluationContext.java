/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluator;

import java.util.concurrent.CompletionService;

class DefaultEvaluationContext implements EvaluationContext {
    private final EvaluationMode evaluationMode;
    private final FormulaEvaluator formulaEvaluator;
    private final CompletionService<Long> completionService;

    DefaultEvaluationContext(EvaluationMode evaluationMode,
                             FormulaEvaluator formulaEvaluator,
                             CompletionService<Long> completionService) {
        this.evaluationMode = evaluationMode;
        this.formulaEvaluator = formulaEvaluator;
        this.completionService = completionService;
    }

    @Override
    public FormulaEvaluator getFormulaEvaluator() {
        return formulaEvaluator;
    }

    @Override
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    @Override
    public CompletionService<Long> getCompletionService() {
        return completionService;
    }
}
