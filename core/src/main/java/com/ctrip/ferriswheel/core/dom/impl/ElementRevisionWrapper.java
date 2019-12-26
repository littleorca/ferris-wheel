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

package com.ctrip.ferriswheel.core.dom.impl;

import com.ctrip.ferriswheel.core.dom.Attribute;
import com.ctrip.ferriswheel.core.dom.Element;

import java.util.Collection;

public class ElementRevisionWrapper extends NodeRevisionWrapper<Element> implements Element {
    public ElementRevisionWrapper(Element element) {
        super(element);
    }

    @Override
    public String getTagName() {
        return getWrappedNode().getTagName();
    }

    @Override
    public boolean hasAttribute(String name) {
        return getWrappedNode().hasAttribute(name);
    }

    @Override
    public String getAttribute(String name) {
        return getWrappedNode().getAttribute(name);
    }

    @Override
    public String setAttribute(String name, String value) {
        return getWrappedNode().setAttribute(name, value);
    }

    @Override
    public String removeAttribute(String name) {
        return getWrappedNode().removeAttribute(name);
    }

    @Override
    public Collection<? extends Attribute> getAttributes() {
        return getWrappedNode().getAttributes();
    }
}
