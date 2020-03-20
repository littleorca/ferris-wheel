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

package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.ElementSnapshot;
import com.ctrip.ferriswheel.core.dom.TextNodeSnapshot;
import com.ctrip.ferriswheel.core.dom.diff.ElementDiff;
import com.ctrip.ferriswheel.core.dom.diff.LineDiff;
import com.ctrip.ferriswheel.core.dom.diff.NodeLocation;
import com.ctrip.ferriswheel.core.dom.diff.TextNodeDiff;
import com.ctrip.ferriswheel.core.dom.impl.AttributeSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.TextNodeSnapshotImpl;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TestPatchHelper extends TestCase {
    private PatchHelper patchHelper;

    @Override
    protected void setUp() throws Exception {
        this.patchHelper = new PatchHelper();
    }

    public void testApplyPatch(){
        // TODO patch on tree.
    }

    public void testApplyNodePatch() {
        ElementSnapshot negativeElement = new ElementSnapshotImpl("Tag",
                Arrays.asList(new AttributeSnapshotImpl("foo", "bar", null)),
                Arrays.asList(),
                null);
        Map<String, String> negAttrs = new HashMap<>();
        negAttrs.put("foo", "bar");
        Map<String, String> posAttrs = new HashMap<>();
        ElementDiff elementDiff = new ElementDiff(new NodeLocation(0), new NodeLocation(0),
                negAttrs, posAttrs);
        ElementSnapshot positiveElement = (ElementSnapshot) patchHelper.applyNodePatch(negativeElement, elementDiff);
        assertNotSame(negativeElement, positiveElement);
        assertSame(negativeElement, positiveElement.getPreviousSnapshot());
        assertTrue(positiveElement.getAttributes().isEmpty());

        TextNodeSnapshot negativeText = new TextNodeSnapshotImpl("hello world", null);
        TextNodeDiff textDiff = new TextNodeDiff(new NodeLocation(0), new NodeLocation(0));
        textDiff.setLines(Arrays.asList(new LineDiff(-1, "hello world")));
        TextNodeSnapshot positiveText = (TextNodeSnapshot) patchHelper.applyNodePatch(negativeText, textDiff);
        assertNotSame(negativeText, positiveText);
        assertSame(negativeText, positiveText.getPreviousSnapshot());
        assertEquals("", positiveText.getData());
    }

    public void testApplyElementPatch() {
        ElementSnapshot negativeElement = new ElementSnapshotImpl("Tag",
                Arrays.asList(), Arrays.asList(), null);
        ElementDiff emptyDiff = new ElementDiff(new NodeLocation(0), new NodeLocation(0));
        ElementSnapshot positiveElement = patchHelper.applyElementPatch(negativeElement, emptyDiff);
        assertSame(negativeElement, positiveElement);
        assertTrue(negativeElement.getAttributes().isEmpty());

        negativeElement = new ElementSnapshotImpl("Tag",
                Arrays.asList(
                        new AttributeSnapshotImpl("a", "a1", null),
                        new AttributeSnapshotImpl("b", "b1", null),
                        new AttributeSnapshotImpl("c", "c1", null)
                ),
                Arrays.asList(), null);

        positiveElement = patchHelper.applyElementPatch(negativeElement, emptyDiff);
        assertSame(negativeElement, positiveElement);
        assertEquals(3, positiveElement.getAttributes().size());

        Map<String, String> negativeAttrs = new HashMap<>();
        negativeAttrs.put("b", "b1"); // remove b
        negativeAttrs.put("c", "c1"); // update c

        Map<String, String> positiveAttrs = new HashMap<>();
        positiveAttrs.put("c", "c2"); // update c
        positiveAttrs.put("d", "d1"); // add d

        ElementDiff diff = new ElementDiff(new NodeLocation(0), new NodeLocation(0));
        diff.setNegativeAttributes(negativeAttrs);
        diff.setPositiveAttributes(positiveAttrs);

        positiveElement = patchHelper.applyElementPatch(negativeElement, diff);
        assertNotSame(negativeElement, positiveElement);
        assertSame(negativeElement, positiveElement.getPreviousSnapshot());
        assertEquals(3, positiveElement.getAttributes().size());
        Map<String, String> attrMap = new HashMap<>();
        positiveElement.getAttributes().forEach(a -> attrMap.put(a.getName(), a.getValue()));
        assertEquals("a1", attrMap.get("a"));
        assertEquals("c2", attrMap.get("c"));
        assertEquals("d1", attrMap.get("d"));
    }

    public void testApplyTextNodePatch() {
        TextNodeSnapshot negativeText = new TextNodeSnapshotImpl(null, null);
        TextNodeDiff emptyDiff = new TextNodeDiff(new NodeLocation(0), new NodeLocation(0));
        TextNodeSnapshot positiveText = patchHelper.applyTextNodePatch(negativeText, emptyDiff);
        assertSame(negativeText, positiveText);
        assertNull(positiveText.getData());

        negativeText = new TextNodeSnapshotImpl("hello\nworld", null);
        positiveText = patchHelper.applyTextNodePatch(negativeText, emptyDiff);
        assertSame(negativeText, positiveText);
        assertEquals("hello\nworld", positiveText.getData());

        TextNodeDiff diff = new TextNodeDiff(new NodeLocation(0), new NodeLocation(0));
        diff.setLines(Arrays.asList(
                new LineDiff(-2, "world"),
                new LineDiff(2, "world\n!!")
        ));
        positiveText = patchHelper.applyTextNodePatch(negativeText, diff);
        assertNotSame(negativeText, positiveText);
        assertSame(negativeText, positiveText.getPreviousSnapshot());
        assertEquals("hello\nworld\n!!", positiveText.getData());
    }
}
