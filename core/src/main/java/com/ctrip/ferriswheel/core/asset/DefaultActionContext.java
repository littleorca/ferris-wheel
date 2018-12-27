/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
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
 *
 */

package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.api.action.ActionContext;

/**
 * @author liuhaifeng
 */
public class DefaultActionContext implements ActionContext {
    private final boolean skipWelding;
    private final boolean skipRefresh;
    private final boolean forceRefresh;

    public DefaultActionContext() {
        this(false, false, false);
    }

    public DefaultActionContext(boolean skipWelding, boolean skipRefresh, boolean forceRefresh) {
        this.skipWelding = skipWelding;
        this.skipRefresh = skipRefresh;
        this.forceRefresh = forceRefresh;
    }

    public DefaultActionContext duplicate() {
        return new DefaultActionContext(skipWelding, skipRefresh, forceRefresh);
    }

    public boolean isSkipWelding() {
        return skipWelding;
    }

    public boolean isSkipRefresh() {
        return skipRefresh;
    }

    public boolean isForceRefresh() {
        return forceRefresh;
    }
}
