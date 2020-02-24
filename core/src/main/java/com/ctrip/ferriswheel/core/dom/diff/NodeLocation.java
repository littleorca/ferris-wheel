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

package com.ctrip.ferriswheel.core.dom.diff;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public final class NodeLocation {
    private final int[] indices;
    private transient String descriptor;

    public NodeLocation() {
        this.indices = new int[0];
    }

    public NodeLocation(int... indices) {
        this(indices, 0, indices.length);
    }

    public NodeLocation(int[] indices, int from, int to) {
        if (indices == null || indices.length == 0 || from < 0 || to <= from || to > indices.length) {
            throw new IllegalArgumentException();
        }
        this.indices = Arrays.copyOfRange(indices, from, to);
    }

    public NodeLocation(List<Integer> indices) {
        if (indices == null || indices.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.indices = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            this.indices[i] = indices.get(i);
        }
    }

    public NodeLocation(NodeLocation base, int index) {
        if (base == null) {
            throw new IllegalArgumentException();
        }
        this.indices = Arrays.copyOf(base.indices, base.indices.length + 1);
        this.indices[this.indices.length - 1] = index;
    }

    public int getDepth() {
        return indices.length;
    }

    public int getIndexOfLevel(int level) {
        if (level < 0 || level >= indices.length) {
            throw new IndexOutOfBoundsException();
        }
        return indices[level];
    }

    public boolean contains(NodeLocation another) {
        if (another.getDepth() <= this.getDepth()) {
            return false;
        }
        for (int i = 0; i < this.getDepth(); i++) {
            if (this.indices[i] != another.indices[i]) {
                return false;
            }
        }
        return true;
    }

    public NodeLocation getParent() {
        if (indices.length < 2) {
            return null;
        }
        return new NodeLocation(indices, 0, indices.length - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeLocation)) return false;
        NodeLocation that = (NodeLocation) o;
        return Arrays.equals(indices, that.indices);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(indices);
    }

    @Override
    public String toString() {
        if (indices.length == 0) {
            return "blank";
        }
        if (descriptor == null) {
            StringBuilder sb = new StringBuilder();
            for (int index : indices) {
                sb.append(":").append(index);
            }
            descriptor = sb.substring(1);
        }
        return descriptor;
    }

    public static class Builder {
        private Stack<Integer> stack = new Stack<>();

        public void push(int index) {
            stack.push(index);
        }

        public int pop() {
            return stack.pop();
        }

        public int size() {
            return stack.size();
        }

        public NodeLocation location() {
            return new NodeLocation(stack);
        }
    }
}
