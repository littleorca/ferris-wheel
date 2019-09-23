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

import com.ctrip.ferriswheel.common.query.DataProvider;
import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.util.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

public abstract class DataProviderSupport implements DataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(DataProviderSupport.class);
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofSeconds(60);
    private CacheService cacheService;

    @Override
    public DataSet execute(DataQuery query) throws IOException {
        if (cacheService != null) {
            DataSet result = cacheService.getCache(query);
            if (result != null) {
                LOG.info("Serve cached result of query: " + query.getScheme());
                return result;
            }
        }

        DataSet result;
        long start = System.currentTimeMillis();
        try {
            result = doExecute(query);
        } finally {
            long cost = System.currentTimeMillis() - start;
            LOG.info("Executed query({}) in {}ms.", query.getScheme(), cost);
        }

        if (result != null && cacheService != null && isCacheable(query, result)) {
            Duration duration = getCacheTtl(query, result);
            cacheService.setCache(query, result, duration);
        }
        return result;
    }

    protected abstract DataSet doExecute(DataQuery query) throws IOException;

    /**
     * Overridable method, determine if the specified query-result pair can be
     * cached.
     *
     * @param query
     * @param result
     * @return
     */
    protected boolean isCacheable(DataQuery query, DataSet result) {
        return true;
    }

    /**
     * Overridable method, get TTL hint for the specified query-result pair.
     *
     * @param query
     * @param result
     * @return
     */
    protected Duration getCacheTtl(DataQuery query, DataSet result) {
        return DEFAULT_CACHE_TTL;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
}
