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

import com.ctrip.ferriswheel.core.dom.Attribute;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class AttributeMap<A extends Attribute> implements Iterable<A> {
    private LinkedHashMap<String, A> map = new LinkedHashMap<>();

    public int size() {
        return map.size();
    }

    public boolean contains(String name) {
        return map.containsKey(name);
    }

    public A get(String name) {
        return map.get(name);
    }

    public A put(A attr) {
        return map.put(attr.getName(), attr);
    }

    public A remove(String name) {
        return map.remove(name);
    }

    @Override
    public Iterator<A> iterator() {
        return map.values().iterator();
    }

    public void forEach(Consumer<? super A> action) {
        map.values().forEach(action);
    }

    public Collection<A> all() {
        return map.values();
    }
}
