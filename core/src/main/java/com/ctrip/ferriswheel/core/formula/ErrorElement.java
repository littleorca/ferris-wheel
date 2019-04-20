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

package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.common.variant.ErrorCode;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.formula.eval.FormulaEvaluationContext;
import com.ctrip.ferriswheel.quarks.Token;

public class ErrorElement extends FormulaElement {
    private Value.ErrorValue errorValue;

    public ErrorElement(Token token, String tokenString) {
        super(token, tokenString);
        this.errorValue = Value.err(parseErrorCode(tokenString));
    }

    private ErrorCode parseErrorCode(String tokenString) {
        if ("#NULL!".equalsIgnoreCase(tokenString)) {
            return ErrorCodes.NULL;
        } else if ("#DIV/0!".equalsIgnoreCase(tokenString)) {
            return ErrorCodes.DIV;
        } else if ("#VALUE!".equalsIgnoreCase(tokenString)) {
            return ErrorCodes.VALUE;
        } else if ("#REF!".equalsIgnoreCase(tokenString)) {
            return ErrorCodes.REF;
        } else if ("#NAME?".equalsIgnoreCase(tokenString)) {
            return ErrorCodes.NAME;
        } else if ("#NUM!".equalsIgnoreCase(tokenString)) {
            return ErrorCodes.NUM;
        } else if ("#N/A".equalsIgnoreCase(tokenString)) {
            return ErrorCodes.NA;
        } else if ("#GETTING_DATA".equalsIgnoreCase(tokenString)) {
            return ErrorCodes.GETTING_DATA;
        } else {
            return null;
        }
    }


    @Override
    public void evaluate(FormulaEvaluationContext context) {
        context.pushOperand(errorValue);
    }
}
