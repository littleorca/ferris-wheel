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

package com.ctrip.ferriswheel.common.action;

import java.util.concurrent.Callable;

/**
 * Problems:
 * <p>
 * action相互触发时，listener怎样方便地整理发生的事件？如果调用链中间出错，listener收集了一半不完整的东西，如何回滚？
 */
public interface ActionNotifier extends ActionListener {

    /**
     * Run in public, listeners will be notified unless this method call itself
     * residents inside in-private running context.
     *
     * @param action
     * @param callable
     * @param <V>
     * @return
     * @see #publicly(Action, Runnable)
     * @see #privately(Callable)
     * @see #privately(Runnable)
     */
    <V> V publicly(Action action, Callable<V> callable);

    /**
     * Run in public, listeners will be notified unless this method call itself
     * residents inside in-private running context.
     *
     * @param action
     * @param runnable
     * @see #publicly(Action, Callable)
     * @see #privately(Callable)
     * @see #privately(Runnable)
     */
    void publicly(Action action, Runnable runnable);

    /**
     * Run in private, actions inside this context won't be broadcast to
     * any listener, including further action running publicly.
     *
     * @param callable
     * @param <V>
     * @return
     * @see #publicly(Action, Callable)
     * @see #publicly(Action, Runnable)
     * @see #privately(Runnable)
     */
    <V> V privately(Callable<V> callable);

    /**
     * Run in private, actions inside this context won't be broadcast to
     * any listener, including further action running publicly.
     *
     * @param runnable
     * @see #publicly(Action, Callable)
     * @see #publicly(Action, Runnable)
     * @see #privately(Callable)
     */
    void privately(Runnable runnable);
}
