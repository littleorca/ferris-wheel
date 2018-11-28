package com.ctrip.ferriswheel.core.intf;

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
