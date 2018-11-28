package com.ctrip.ferriswheel.core.intf;

/**
 * Problems:
 * <p>
 * action相互触发时，listener怎样方便地整理发生的事件？如果调用链中间出错，listener收集了一半不完整的东西，如何回滚？
 */
public interface ActionListener {

    boolean beforeAction(Action action);

    default void afterActionDone(Action action) {
        afterActionDone(action, null);
    }

    void afterActionDone(Action action, Object result);

    void afterActionFailed(Action action);
}
