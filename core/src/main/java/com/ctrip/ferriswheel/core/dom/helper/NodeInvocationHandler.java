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

package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.Node;
import com.ctrip.ferriswheel.core.dom.NodeWrapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class NodeInvocationHandler<T extends Node> implements InvocationHandler, NodeWrapper {
    private T node;
    private long revision;

    public NodeInvocationHandler(T node) {
        this.node = node;
    }

    public T expose() {
        return node;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        if (NodeWrapper.class.equals(declaringClass)) {
            return method.invoke(this, args);
        }

        WithTransaction withTransaction = method.getAnnotation(WithTransaction.class);
        if (withTransaction == null) {
            withTransaction = node.getClass().getMethod(method.getName(), method.getParameterTypes())
                    .getAnnotation(WithTransaction.class);
        }

        if (withTransaction == null) {
            return method.invoke(node, args);
        }

        checkTransaction();
        return method.invoke(node, args);
    }

    private void checkTransaction() {
        System.out.println("### Start tx if needed!");
    }
}
