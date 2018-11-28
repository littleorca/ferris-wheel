package com.ctrip.ferriswheel.core.util;

import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class FlagStack {
    private Stack<Boolean> stack = new Stack<>();

    public FlagStack() {
    }

    public FlagStack(boolean initialFlag) {
        this.stack.push(initialFlag);
    }

    /**
     * Push flag.
     *
     * @param flag
     */
    public void push(boolean flag) {
        this.stack.push(flag);
    }

    /**
     * Pop flag.
     *
     * @return
     */
    public boolean pop() {
        return this.stack.pop();
    }

    /**
     * Get current flag.
     *
     * @return
     */
    public boolean get() {
        return this.stack.peek();
    }

    /**
     * Run with the given flag.
     *
     * @param flag     set this flag for running context.
     * @param callable Callable object.
     * @param <V>
     * @return
     * @throws Exception
     */
    public <V> V runWith(boolean flag, Callable<V> callable) throws Exception {
        push(flag);
        try {
            return callable.call();
        } finally {
            pop();
        }
    }

    /**
     * Run with the given flag.
     *
     * @param flag     set this flag for running context.
     * @param runnable Runnable object, with out return value.
     */
    public void runWith(boolean flag, Runnable runnable) {
        push(flag);
        try {
            runnable.run();
        } finally {
            pop();
        }
    }

    /**
     * Run with the given flag.
     *
     * @param flag     set this flag for running context.
     * @param object   Object for consumer to deal with.
     * @param consumer
     * @param <T>
     */
    public <T> void runWith(boolean flag, T object, Consumer<T> consumer) {
        push(flag);
        try {
            consumer.accept(object);
        } finally {
            pop();
        }
    }

}
