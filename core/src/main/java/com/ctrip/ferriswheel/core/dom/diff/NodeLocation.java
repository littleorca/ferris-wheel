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

import java.util.*;

public final class NodeLocation implements Comparable<NodeLocation>, Iterable<Integer> {
    private static final NodeLocation SHARED_EMPTY_LOCATION = new NodeLocation();

    private final int[] indices;
    private transient int hash;
    private transient String descriptor;

    public static NodeLocation empty() {
        return SHARED_EMPTY_LOCATION;
    }

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

    public Integer leafIndex() {
        if (indices.length < 1) {
            throw new IllegalStateException("Location is empty.");
        }
        return indices[indices.length - 1];
    }

    public boolean contains(NodeLocation another) {
        return isAncestorOf(another);
    }

    public NodeLocation getParent() {
        if (indices.length < 2) {
            return null;
        }
        return new NodeLocation(indices, 0, indices.length - 1);
    }

    public boolean isAncestorOf(NodeLocation another) {
        return another.getDepth() > getDepth() &&
                partialCompareTo(another, 0, getDepth()) == 0;
    }

    public boolean isParentOf(NodeLocation another) {
        return another.getDepth() == getDepth() + 1 &&
                partialCompareTo(another, 0, getDepth()) == 0;
    }

    public boolean isSibling(NodeLocation another) {
        return partialCompareTo(another, 0, getDepth() - 1) == 0;
    }

    public boolean isDescendantOf(NodeLocation another) {
        return another.isAncestorOf(this);
    }

    private int partialCompareTo(NodeLocation another, int from, int to) {
        if (from > to) {
            throw new IllegalArgumentException();
        }
        if (from >= getDepth() || from >= another.getDepth() || to < 0) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = from; i < to; i++) {
            if (getIndexOfLevel(i) > another.getIndexOfLevel(i)) {
                return 1;
            } else if (getIndexOfLevel(i) < another.getIndexOfLevel(i)) {
                return -1;
            }
        }
        return 0;
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
        if (hash == 0 && indices.length > 0) {
            hash = Arrays.hashCode(indices);
        }
        return hash;
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

    @Override
    public int compareTo(NodeLocation o) {
        int pos = 0;
        for (int index : this) {
            if (pos >= o.indices.length) {
                return 1;
            }
            int anotherIndex = o.indices[pos++];
            if (index != anotherIndex) {
                return index > anotherIndex ? 1 : -1;
            }
        }
        if (pos < o.indices.length) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return new ReadOnlyIterator();
    }


    final class ReadOnlyIterator implements Iterator<Integer> {
        private int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < indices.length;
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return indices[pos++];
        }
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
