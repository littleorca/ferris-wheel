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

class DefaultTransaction implements Transaction {
    private final DefaultWorkbook workbook;
    private final DefaultAssetEvaluator evaluator;
    private final long id;
    private volatile TransactionPhase phase = TransactionPhase.Polluting;

    DefaultTransaction(DefaultWorkbook workbook, DefaultAssetEvaluator evaluator, long id) {
        this.workbook = workbook;
        this.evaluator = evaluator;
        this.id = id;
    }

    @Override
    public long getTransactionId() {
        return id;
    }

    @Override
    public TransactionPhase getCurrentPhase() {
        return phase;
    }

    @Override
    public void evaluate(EvaluationMode mode) {
        if (TransactionPhase.Polluting != getCurrentPhase()) {
            throw new IllegalStateException("Transaction is not ready for evaluating, current phase is "
                    + getCurrentPhase());
        }
        this.phase = TransactionPhase.Evaluating;
        evaluator.evaluate(workbook, mode);
    }

    @Override
    public void commit() {
        if (phase != TransactionPhase.Evaluating) {
            throw new IllegalStateException("Cannot commit at phase: " + phase);
        }
        // TODO
        phase = TransactionPhase.Done;
    }

    @Override
    public void rollback() {
        if (phase == TransactionPhase.Done) {
            throw new IllegalStateException("Cannot rollback after transaction has been done.");
        }
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
