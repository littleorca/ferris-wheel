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

package com.ctrip.ferriswheel.common.query;

import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.util.ImmutableArrayDataSet;
import com.ctrip.ferriswheel.common.variant.ErrorCode;

public final class ImmutableQueryResult implements QueryResult {
    private final ErrorCode errorCode;
    private final String errorMessage;
    private final ImmutableCacheHint cacheHint;
    private final ImmutableArrayDataSet dataSet;

    public static ImmutableQueryResult from(QueryResult result) {
        if (result == null) {
            return null;
        } else if (result instanceof ImmutableQueryResult) {
            return (ImmutableQueryResult) result;
        } else {
            return new ImmutableQueryResult(result.getErrorCode(), result.getErrorMessage(),
                    result.getCacheHint(), result.getDataSet());
        }
    }

    public ImmutableQueryResult(ErrorCode errorCode,
                                String errorMessage,
                                CacheHint cacheHint,
                                DataSet dataSet) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.cacheHint = cacheHint == null ? null : ImmutableCacheHint.from(cacheHint);
        this.dataSet = dataSet == null ? null : ImmutableArrayDataSet.from(dataSet);
    }

    @Override
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public CacheHint getCacheHint() {
        return cacheHint;
    }

    @Override
    public DataSet getDataSet() {
        return dataSet;
    }
}
