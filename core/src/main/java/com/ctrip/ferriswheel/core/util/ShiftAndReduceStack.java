/*
 * MIT License
 *
 * Copyright (c) 2018-2020 Ctrip.com
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

package com.ctrip.ferriswheel.core.util;

import java.util.Stack;

public abstract class ShiftAndReduceStack<T> {
    private Stack<T> stack = new Stack<>();

    /**
     * Feed new input and try to shift or reduce the input item.
     *
     * @param input
     */
    public void feed(T input) {
        while (tryReduce(input)) ;
        if (!isTerminator(input)) {
            shift(input);
        }
    }

    public void terminate() {
        feed(null);
    }

    protected boolean isTerminator(T input) {
        return input == null;
    }

    protected void shift(T inputElement) {
        stack.push(inputElement);
    }

    /**
     * Reduce items on top of the stack with out shift the input to the stack.
     * If no reduce can be done at the moment, false value will be returned,
     * otherwise the return value is true.
     *
     * @param input
     * @return
     */
    protected abstract boolean tryReduce(T input);

    public T push(T item) {
        return stack.push(item);
    }

    public T pop() {
        return stack.pop();
    }

    public T peek() {
        return stack.peek();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

}
