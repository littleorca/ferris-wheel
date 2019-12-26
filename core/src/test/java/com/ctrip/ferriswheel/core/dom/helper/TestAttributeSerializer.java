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

import com.ctrip.ferriswheel.common.view.Display;
import com.ctrip.ferriswheel.common.view.Layout;
import com.ctrip.ferriswheel.common.view.Placement;
import com.ctrip.ferriswheel.core.view.LayoutImpl;
import junit.framework.TestCase;

public class TestAttributeSerializer extends TestCase {
    private AttributeSerializer serializer;

    @Override
    protected void setUp() throws Exception {
        this.serializer = new AttributeSerializer();
    }

    public void testSerializeAndDeserializeLayout() {
        LayoutImpl originLayout = new LayoutImpl();
        String serialized = serializer.serializeLayout(originLayout);
        System.out.println(serialized);
        Layout deserializedLayout = serializer.deserializeLayout(serialized);
        assertEquals(originLayout, deserializedLayout);

        originLayout = new LayoutImpl(Display.BLOCK, 100, 50, Placement.LEFT, Placement.TOP, new LayoutImpl.GridImpl(12, 6, new LayoutImpl.SpanImpl(0, 12), new LayoutImpl.SpanImpl(0, 6)));
        serialized = serializer.serializeLayout(originLayout);
        System.out.println(serialized);
    }

    public void testDeserializeMalformedLayoutString() {
        try {
            serializer.deserializeLayout("width:");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            serializer.deserializeLayout("height");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            serializer.deserializeLayout("foo:bar;");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            serializer.deserializeLayout("grid-row:1,2;");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            serializer.deserializeLayout("display:1;");
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

}
