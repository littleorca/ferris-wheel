package com.ctrip.ferriswheel.core.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

public class InvokeChain<T> implements InvocationHandler {
    private List<? extends T> items;

    private InvokeChain(List<? extends T> items) {
        this.items = items;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object ret = null;
        for (Object item : items) {
            ret = method.invoke(item, args);
        }
        return ret;
    }


    public static class Builder<T> {
        private Class<T>[] classes;
        private List<T> items = new LinkedList<>();

        public Builder() {
        }

        public Builder(Class<T>... classes) {
            this.classes = classes;
        }

        public Builder<T> add(T item) {
            items.add(item);
            return this;
        }

        @SuppressWarnings("unchecked")
        public T build() {
            if (items.isEmpty()) {
                throw new IllegalStateException("Please add at least on item.");
            }
            return (T) Proxy.newProxyInstance(
                    InvokeChain.class.getClassLoader(),
                    classes,
                    new InvokeChain<>(items));
        }
    }

}
