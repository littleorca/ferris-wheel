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
import com.ctrip.ferriswheel.api.action.ActionContextManager;

import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * @author liuhaifeng
 */
public class DefaultActionContextManager implements ActionContextManager {
    private final Stack<ActionContext> states = new Stack<>();

    public DefaultActionContextManager() {
        this(new DefaultActionContext());
    }

    public DefaultActionContextManager(ActionContext initialState) {
        states.push(initialState);
    }

    @Override
    public boolean isSkipWelding() {
        return states.peek().isSkipWelding();
    }

    @Override
    public boolean isSkipRefresh() {
        return states.peek().isSkipRefresh();
    }

    @Override
    public boolean isForceRefresh() {
        return states.peek().isForceRefresh();
    }

    @Override
    public void withContext(ActionContext context, Runnable runnable) {
        states.push(context);
        try {
            runnable.run();
        } finally {
            states.pop();
        }
    }

    @Override
    public <V> V withContext(ActionContext context, Callable<V> callable) throws Exception {
        states.push(context);
        try {
            return callable.call();
        } finally {
            states.pop();
        }
    }

    @Override
    public <T> void withContext(ActionContext context, T input, Consumer<T> consumer) {
        states.push(context);
        try {
            consumer.accept(input);
        } finally {
            states.pop();
        }
    }
}
