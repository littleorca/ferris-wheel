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

package com.ctrip.ferriswheel.provider;

import com.ctrip.ferriswheel.common.query.CacheHint;
import com.ctrip.ferriswheel.common.query.DataProvider;
import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class DataProviderSupport implements DataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DataProviderSupport.class);
    private CacheService cacheService;

    @Override
    public QueryResult execute(DataQuery query, boolean forceRefresh) throws IOException {
        QueryResult result;

        if (!forceRefresh) {
            result = getCachedResult(query);
            if (result != null) {
                LOG.info("Serve cached result of query: " + query.getScheme());
                return result;
            }
        }

        long start = System.currentTimeMillis();
        try {
            result = doExecute(query);
        } finally {
            long cost = System.currentTimeMillis() - start;
            LOG.info("Executed query({}) in {}ms.", query.getScheme(), cost);
        }

        cacheIfPossible(query, result);
        return result;
    }

    protected QueryResult getCachedResult(DataQuery query) {
        if (cacheService == null) {
            return null;
        }
        return cacheService.getCache(query);
    }

    protected abstract QueryResult doExecute(DataQuery query) throws IOException;

    protected void cacheIfPossible(DataQuery query, QueryResult result) {
        if (cacheService == null || result == null || result.getCacheHint() == null) {
            return;
        }
        CacheHint cacheHint = result.getCacheHint();
        if (cacheHint.getMaxAge() <= 0) {
            return;
        }
        if (cacheHint.getDate() != null) {
            long ts = cacheHint.getDate().getTime();
            if (ts + TimeUnit.SECONDS.toMillis(cacheHint.getMaxAge()) <= System.currentTimeMillis()) {
                return;
            }
        }
        cacheService.cacheIfPossible(query, result);
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
}
