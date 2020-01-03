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
import com.ctrip.ferriswheel.core.dom.impl.AbstractElement;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NodeList<N extends Node> implements Iterable<N> {
    private ArrayList<N> array = new ArrayList<>();
    private transient Map<String, N> names;

    public N first() {
        return array.isEmpty() ? null : array.get(0);
    }

    public <T> T first(Class<T> clazz) {
        return clazz.cast(first(clazz::isInstance));
    }

    public N first(Predicate<? super N> predicate) {
        for (N n : array) {
            if (predicate.test(n)) {
                return n;
            }
        }
        return null;
    }

    public N last() {
        return array.isEmpty() ? null : array.get(array.size() - 1);
    }

    public <T> T last(Class<T> clazz) {
        return clazz.cast(last(clazz::isInstance));
    }

    public N last(Predicate<? super N> predicate) {
        for (int i = array.size() - 1; i >= 0; i--) {
            N n = array.get(i);
            if (predicate.test(n)) {
                return n;
            }
        }
        return null;
    }

    public <T> List<T> filter(Class<T> clazz) {
        return array.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }

    public List<N> filter(Predicate<? super N> predicate) {
        return array.stream().filter(predicate).collect(Collectors.toList());
    }

    public int size() {
        return array.size();
    }

    public long count(Predicate<? super N> predicate) {
        return array.stream().filter(predicate).count();
    }

    public N get(int index) {
        return array.get(index);
    }

    public N get(String name) {
        prepareNameIndexIfNeeded();
        return names.get(name);
    }

    public boolean contains(N node) {
        return array.contains(node);
    }

    public void insertBefore(N node, N ref) {
        int index = ref == null ? 0 : array.indexOf(ref);
        if (index == -1) {
            throw new IllegalArgumentException("Referred node not found.");
        }
        array.add(index, node);
    }

    public void append(N node) {
        array.add(node);
    }

    public boolean remove(N node) {
        return array.remove(node);
    }

    @Override
    public Iterator<N> iterator() {
        return array.iterator();
    }

    private void prepareNameIndexIfNeeded() {
        if (names != null) {
            return;
        }

        names = new HashMap<>(array.size());
        for (N node : array) {
            if (!(node instanceof AbstractElement)) {
                continue;
            }
            AbstractElement elem = (AbstractElement) node;
            if (elem.hasAttribute("name")) {
                String name = elem.getAttribute("name");
                names.put(name, node);
            }
        }
    }

}
