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

package com.ctrip.ferriswheel.core.dom.impl;

import com.ctrip.ferriswheel.core.dom.Attribute;
import com.ctrip.ferriswheel.core.dom.AttributeSnapshot;

public class AttributeSnapshotImpl extends AbstractNodeSnapshot implements AttributeSnapshot {
    private final String name;
    private final String value;

    public AttributeSnapshotImpl(Attribute attribute, AttributeSnapshot previousSnapshot) {
        this(attribute.getName(), attribute.getValue(), previousSnapshot);
    }

    public AttributeSnapshotImpl(String name, String value) {
        this(name, value, null);
    }

    public AttributeSnapshotImpl(String name, String value, AttributeSnapshot previousSnapshot) {
        super(previousSnapshot);
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public AttributeSnapshot getPreviousSnapshot() {
        return (AttributeSnapshot) super.getPreviousSnapshot();
    }

    @Override
    public AttributeSnapshotImpl duplicate(boolean linked) {
        return new AttributeSnapshotImpl(name, value, linked ? this : null);
    }

    @Override
    protected String toSingleLineString() {
        return name + "=" + value.replaceAll("\\r", "\\\\r")
                .replaceAll("\\n", "\\\\n");
    }
}
