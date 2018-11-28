package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.core.intf.Action;
import com.ctrip.ferriswheel.core.intf.ActionListener;
import com.ctrip.ferriswheel.core.intf.ActionNotifier;
import com.ctrip.ferriswheel.core.util.FlagStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActionListenerChain implements ActionNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(ActionListenerChain.class);
    private final CopyOnWriteArrayList<ActionListener> listeners = new CopyOnWriteArrayList<>();
    private final FlagStack silentMode = new FlagStack(false);

    ActionListenerChain() {
    }

    ActionListenerChain(List<ActionListener> listeners) {
        if (listeners == null) {
            throw new IllegalArgumentException("'listeners' cannot be null.");
        }
        for (ActionListener listener : listeners) {
            this.listeners.addIfAbsent(listener);
        }
    }

    public void addListener(ActionListener listener) {
        this.listeners.addIfAbsent(listener);
    }

    public boolean removeListener(ActionListener listener) {
        return this.listeners.remove(listener);
    }

    @Override
    public boolean beforeAction(Action action) {
        if (silentMode.get()) {
            return true;
        }
        boolean allowed = true;
        for (ActionListener listener : listeners) {
            try {
                allowed &= listener.beforeAction(action);
            } catch (Exception e) {
                allowed = false;
                LOG.warn("Action listener threw exception.", e);
            }
        }
        return allowed;
    }

    @Override
    public void afterActionDone(Action action, Object result) {
        if (silentMode.get()) {
            return;
        }
        for (ActionListener listener : listeners) {
            try {
                listener.afterActionDone(action, result);
            } catch (Exception e) {
                LOG.warn("Action listener threw exception.", e);
            }
        }
    }

    @Override
    public void afterActionFailed(Action action) {
        if (silentMode.get()) {
            return;
        }
        for (ActionListener listener : listeners) {
            try {
                listener.afterActionFailed(action);
            } catch (Exception e) {
                LOG.warn("Action listener threw exception.", e);
            }
        }
    }

    @Override
    public <V> V publicly(Action action, Callable<V> callable) {
        return doAction(action, callable);
    }

    @Override
    public void publicly(Action action, Runnable runnable) {
        doAction(action, runnable);
    }

    @Override
    public <V> V privately(Callable<V> callable) {
        try {
            return silentMode.runWith(true, callable);
        } catch (Error e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void privately(Runnable runnable) {
        silentMode.runWith(true, runnable);
    }

    <V> V doAction(Action action, Callable<V> callable) {
        if (!beforeAction(action)) {
            throw new RuntimeException(); // TODO handle error friendly
        }

        try {
            V ret = callable.call();
            afterActionDone(action, ret);
            return ret;

        } catch (Throwable e) {
            afterActionFailed(action);
            if (e instanceof Error) {
                throw (Error) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    void doAction(Action action, Runnable runnable) {
        if (!beforeAction(action)) {
            throw new RuntimeException(); // TODO handle error friendly
        }

        try {
            runnable.run();
            afterActionDone(action, null);

        } catch (Throwable e) {
            afterActionFailed(action);
            if (e instanceof Error) {
                throw (Error) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

}
