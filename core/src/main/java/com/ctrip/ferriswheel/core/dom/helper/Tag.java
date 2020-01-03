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

import com.ctrip.ferriswheel.core.dom.*;
import com.ctrip.ferriswheel.core.dom.impl.*;

public enum Tag {
    WORKBOOK("workbook", WorkbookElement.class, WorkbookElementImpl.class),
    SHEET("sheet", SheetElement.class, SheetElementImpl.class),
    TABLE("table", TableElement.class, TableElementImpl.class),
    CHART("chart", ChartElement.class, ChartElementImpl.class),
    TEXT("text", TextElement.class, TextElementImpl.class),
    FORM("form", FormElement.class, FormElementImpl.class),

    NAME("name", NameElement.class, NameElementImpl.class),
    TITLE("title", TitleElement.class, TitleElementImpl.class),

    BAND_LIST("band-list", AxisBandListElement.class, AxisBandListElementImpl.class),
    BAND("band", AxisBandElement.class, AxisBandElementImpl.class),
    BINDER("binder", ChartBinderElement.class, ChartBinderElementImpl.class),
    CATEGORIES("categories", CategoriesElement.class, CategoriesElementImpl.class),
    SERIES_LIST("series-list", DataSeriesListElement.class, DataSeriesListElementImpl.class),
    SERIES("series", DataSeriesElement.class, DataSeriesElementImpl.class),
    X_AXIS("x-axis", XAxisElement.class, XAxisElementImpl.class),
    Y_AXIS("y-axis", YAxisElement.class, YAxisElementImpl.class),
    Z_AXIS("z-axis", ZAxisElement.class, ZAxisElementImpl.class),
    X_VALUES("x-values", XValuesElement.class, XValuesElementImpl.class),
    Y_VALUES("y-values", YValuesElement.class, YValuesElementImpl.class),
    Z_VALUES("z-values", ZValuesElement.class, ZValuesElementImpl.class),

    VARIANT("var", VariantElement.class, VariantElementImpl.class);

    private final String tagName;
    private final Class<? extends Element> intf;
    private final Class<? extends Element> impl;

    <T extends Element> Tag(String tagName, Class<T> intf, Class<? extends T> impl) {
        this.tagName = tagName;
        this.intf = intf;
        this.impl = impl;
    }

    public String getTagName() {
        return tagName;
    }

    public Class<? extends Element> getInterface() {
        return intf;
    }

    public Class<? extends Element> getImplementation() {
        return impl;
    }

}
