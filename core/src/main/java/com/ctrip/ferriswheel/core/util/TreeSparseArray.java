package com.ctrip.ferriswheel.core.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class TreeSparseArray<E> implements SparseArray<E>, Serializable {
    private TreeMap<Integer, E> holder = new TreeMap<>();

    public int size() {
        return holder.isEmpty() ? 0 : holder.lastKey() + 1;
    }

    public int physicalSize() {
        return holder.size();
    }

    public E set(int index, E newValue) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be greater than or equals to zero.");
        }
        return holder.put(index, newValue);
    }

    public E get(int index) {
        return holder.get(index);
    }

    public E first() {
        Map.Entry<Integer, E> entry = holder.firstEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    public E last() {
        Map.Entry<Integer, E> entry = holder.lastEntry();
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }

    public E move(int from, int to) {
        E element = holder.remove(from);
        if (element == null) {
            return holder.remove(to);
        } else {
            return set(to, element);
        }
    }

    public E remove(int index) {
        return holder.remove(index);
    }

    public boolean isEmpty() {
        return holder.isEmpty();
    }

    public Iterator<Map.Entry<Integer, E>> iterator() {
        return iterator(null, null);
    }

    public Iterator<Map.Entry<Integer, E>> iterator(Integer start, Integer end) {
        if (start == null && end == null) {
            return holder.entrySet().iterator();
        } else if (start == null) {
            return holder.headMap(end).entrySet().iterator();
        } else if (end == null) {
            return holder.tailMap(start).entrySet().iterator();
        } else {
            return holder.subMap(start, end).entrySet().iterator();
        }
    }

    public Collection<E> values() {
        return holder.values();
    }
}
