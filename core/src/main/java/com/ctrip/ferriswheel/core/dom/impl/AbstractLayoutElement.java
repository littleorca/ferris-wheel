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

import com.ctrip.ferriswheel.common.view.Layout;
import com.ctrip.ferriswheel.core.dom.helper.AttributeSerializer;

public abstract class AbstractLayoutElement extends AbstractElement {
    protected static final String ATTR_STYLE = "style";

    protected transient Layout layout;

    protected AbstractLayoutElement(AbstractDocument ownerDocument) {
        super(ownerDocument);
    }

    public Layout getLayout() {
        if (layout == null) {
            String styleAttr = getAttribute(ATTR_STYLE);
            if (styleAttr != null) {
                layout = new AttributeSerializer().deserializeLayout(styleAttr);
            }
        }
        return layout;
    }

    protected void setLayout(Layout layout) {
        this.layout = layout;
        if (layout == null) {
            removeAttribute(ATTR_STYLE);
        } else {
            setAttribute(ATTR_STYLE, new AttributeSerializer().serializeLayout(layout));
        }
    }

    @Override
    protected void afterSetAttribute(String name, String value, String oldValue) {
        if (ATTR_STYLE.equalsIgnoreCase(name)) {
            layout = null;
        }
    }

    @Override
    protected void afterRemoveAttribute(String name, String value) {
        if (ATTR_STYLE.equalsIgnoreCase(name)) {
            layout = null;
        }
    }
}
