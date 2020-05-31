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

import com.ctrip.ferriswheel.common.variant.DynamicVariant;
import com.ctrip.ferriswheel.common.view.Orientation;
import com.ctrip.ferriswheel.common.view.Placement;
import com.ctrip.ferriswheel.core.dom.ChartBinderElement;

public final class ChartBinderElementImpl extends AbstractVariantElement implements ChartBinderElement {
    private static final String ATTR_ORIENTATION = "orientation";
    private static final String ATTR_CATEGORIES_PLACEMENT = "categories-placement";
    private static final String ATTR_SERIES_NAME_PLACEMENT = "series-name-placement";

    protected ChartBinderElementImpl(AbstractDocument ownerDocument) {
        super(ownerDocument);
    }

    @Override
    public DynamicVariant getData() {
        return this.getVariant();
    }

    @Override
    public Orientation getOrientation() {
        String attrValue = getAttribute(ATTR_ORIENTATION);
        return attrValue == null ? null : Orientation.valueOf(attrValue);
    }

    @Override
    public Placement getCategoriesPlacement() {
        String attrValue = getAttribute(ATTR_CATEGORIES_PLACEMENT);
        return attrValue == null ? null : Placement.valueOf(attrValue);
    }

    @Override
    public Placement getSeriesNamePlacement() {
        String attrValue = getAttribute(ATTR_SERIES_NAME_PLACEMENT);
        return attrValue == null ? null : Placement.valueOf(attrValue);
    }

    @Override
    public String getTagName() {
        return null;
    }
}
