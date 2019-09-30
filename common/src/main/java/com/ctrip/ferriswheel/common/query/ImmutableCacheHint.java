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

import java.util.Date;

public final class ImmutableCacheHint implements CacheHint {
    private final Date date;
    private final long maxAge;
    private final boolean fromCache;

    public static ImmutableCacheHint from(CacheHint cacheHint) {
        if (cacheHint == null) {
            return null;
        } else if (cacheHint instanceof ImmutableCacheHint) {
            return (ImmutableCacheHint) cacheHint;
        } else {
            return new ImmutableCacheHint(cacheHint);
        }
    }

    public ImmutableCacheHint(CacheHint another) {
        this(another.getDate(), another.getMaxAge(), another.isFromCache());
    }


    private ImmutableCacheHint(Date date, long maxAge, boolean fromCache) {
        this.date = date;
        this.maxAge = maxAge;
        this.fromCache = fromCache;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public long getMaxAge() {
        return maxAge;
    }

    @Override
    public boolean isFromCache() {
        return fromCache;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Date date;
        private long maxAge = 0;
        private boolean fromCache = false;

        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        public Builder maxAge(long maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Builder maxAge(boolean fromCache) {
            this.fromCache = fromCache;
            return this;
        }

        public ImmutableCacheHint build() {
            Date dt = (date == null) ? new Date() : date;
            return new ImmutableCacheHint(dt, maxAge, fromCache);
        }
    }
}
