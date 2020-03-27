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
import com.ctrip.ferriswheel.core.dom.NodeSnapshot;
import com.ctrip.ferriswheel.core.dom.TextNodeSnapshot;
import com.ctrip.ferriswheel.core.dom.TreeSnapshotUtil;
import com.ctrip.ferriswheel.core.dom.diff.*;
import com.ctrip.ferriswheel.core.dom.impl.AttributeSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.TextNodeSnapshotImpl;
import junit.framework.TestCase;

import java.util.*;

import static com.ctrip.ferriswheel.core.dom.TreeSnapshotUtil.assertTreeEquals;

public class TestPatchHelper extends TestCase {
    private PatchHelper patchHelper;

    @Override
    protected void setUp() throws Exception {
        this.patchHelper = new PatchHelper();
    }

    public void testApplyPatchWithEmptyPatch() {
        ElementSnapshotImpl negative = new ElementSnapshotImpl("E",
                Arrays.asList(new AttributeSnapshotImpl("foo", "bar", null)),
                Arrays.asList(),
                null);
        Patch patch = new Patch();
        ElementSnapshot patched = patchHelper.applyPatch(negative, patch);
        assertTreeEquals(negative, patched);
    }

    public void testApplyPatchOnRootUpdate() {
        ElementSnapshotImpl negative = new ElementSnapshotImpl("E",
                Arrays.asList(new AttributeSnapshotImpl("foo", "bar", null)),
                Arrays.asList(),
                null);
        ElementSnapshotImpl positive = new ElementSnapshotImpl("E",
                Arrays.asList(new AttributeSnapshotImpl("foo", "bar2", null)),
                Arrays.asList(),
                negative);

        Patch patch = new Patch();
        Map<String, String> negativeAttrs = new HashMap<>();
        negativeAttrs.put("foo", "bar");
        Map<String, String> positiveAttrs = new HashMap<>();
        positiveAttrs.put("foo", "bar2");
        patch.setDiffList(Arrays.asList(new ElementDiff(
                negative.getTagName(),
                NodeLocation.root(),
                NodeLocation.root(),
                negativeAttrs,
                positiveAttrs)));

        ElementSnapshot patched = patchHelper.applyPatch(negative, patch);
        System.out.println(patched);
    }

    public void testApplyPatchOfMultipleLevelInsertions() {
        ElementSnapshotImpl negative = new ElementSnapshotImpl("E",
                Arrays.asList(new AttributeSnapshotImpl("foo", "bar", null)),
                Arrays.asList(),
                null);
        Patch patch = new Patch();
        patch.setDiffList(Arrays.asList(
                new ElementDiff(
                        "E2",
                        null,
                        new NodeLocation(0, 0),
                        Collections.emptyMap(),
                        Collections.emptyMap()),
                new ElementDiff(
                        "E3",
                        null,
                        new NodeLocation(0, 0, 0),
                        Collections.emptyMap(),
                        Collections.emptyMap())
        ));

        ElementSnapshot patched = patchHelper.applyPatch(negative, patch);
        System.out.println(patched);
        assertTreeEquals(new ElementSnapshotImpl("E",
                        Arrays.asList(new AttributeSnapshotImpl("foo", "bar", null)),
                        Arrays.asList(
                                new ElementSnapshotImpl("E2",
                                        Collections.emptyList(),
                                        Arrays.asList(new ElementSnapshotImpl("E3",
                                                Collections.emptyList(),
                                                Collections.emptyList(),
                                                null)),
                                        null)
                        ),
                        negative),
                patched);
    }

    public void testApplyDeletions() {
        ElementSnapshot nTree1 = TreeSnapshotUtil.buildTree("E");
        ElementSnapshot pTree1 = patchHelper.applyDeletions(nTree1,
                Collections.emptyNavigableSet(), Collections.emptyMap());
        assertSame(nTree1, pTree1);

        ElementSnapshot nTree2 = TreeSnapshotUtil.buildTree("E(E2)");
        NavigableSet<NodeLocation> deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0));
        Map<NodeLocation, NodeSnapshot> dirtyMap = new HashMap<>();
        ElementSnapshot pTree2 = patchHelper.applyDeletions(nTree2,
                deletions, dirtyMap);
        assertTreeEquals(nTree1, pTree2);
        assertEquals(1, dirtyMap.size());
        assertSame(nTree2, dirtyMap.get(NodeLocation.root()).getPreviousSnapshot());

        ElementSnapshot nTree3 = TreeSnapshotUtil.buildTree("E(E2(E3))");
        deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0, 0));
        dirtyMap = new HashMap<>();
        ElementSnapshot pTree3 = patchHelper.applyDeletions(nTree3,
                deletions, dirtyMap);
        assertTreeEquals(nTree2, pTree3);
        assertEquals(2, dirtyMap.size());
        assertEquals("E2", dirtyMap.get(new NodeLocation(0, 0)).getNodeName());
        assertEquals("E", dirtyMap.get(NodeLocation.root()).getNodeName());

        ElementSnapshot nTree4 = TreeSnapshotUtil.buildTree("E(E2(E3))");
        deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0));
        dirtyMap = new HashMap<>();
        ElementSnapshot pTree4 = patchHelper.applyDeletions(nTree4,
                deletions, dirtyMap);
        assertTreeEquals(nTree1, pTree4);
        assertEquals(1, dirtyMap.size());
        assertEquals("E", dirtyMap.get(NodeLocation.root()).getNodeName());

        ElementSnapshot nTree5 = TreeSnapshotUtil.buildTree("E(E2(E3))");
        deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0));
        deletions.add(new NodeLocation(0, 0, 0));
        dirtyMap = new HashMap<>();
        ElementSnapshot pTree5 = patchHelper.applyDeletions(nTree5,
                deletions, dirtyMap);
        assertTreeEquals(nTree1, pTree5);
        assertEquals(2, dirtyMap.size());
        assertEquals("E2", dirtyMap.get(new NodeLocation(0, 0)).getNodeName());
        assertEquals("E", dirtyMap.get(NodeLocation.root()).getNodeName());

        ElementSnapshot nTree6 = TreeSnapshotUtil.buildTree("E(E2,E3,E4(E6),E5)");
        deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0));
        deletions.add(new NodeLocation(0, 2));
        deletions.add(new NodeLocation(0, 3));
        dirtyMap = new HashMap<>();
        ElementSnapshot pTree6 = patchHelper.applyDeletions(nTree6,
                deletions, dirtyMap);
        assertTreeEquals(TreeSnapshotUtil.buildTree("E(E3)"), pTree6);
        assertEquals(1, dirtyMap.size());
        assertEquals("E", dirtyMap.get(NodeLocation.root()).getNodeName());
    }

    public void testApplyInsertionsWithSimpleCases() {
        ElementSnapshot nTree1 = TreeSnapshotUtil.buildTree("E");
        TreeMap<NodeLocation, Diff> insertions = new TreeMap<>();
        ElementDiff elemDiff = new ElementDiff("E2", null, new NodeLocation(0, 0));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        ElementSnapshot pTree1 = patchHelper.applyInsertions(nTree1, Collections.emptyMap(), insertions);
        assertTreeEquals(TreeSnapshotUtil.buildTree("E(E2)"), pTree1);

        ElementSnapshot nTree2 = TreeSnapshotUtil.buildTree("E");
        insertions = new TreeMap<>();
        elemDiff = new ElementDiff("E2", null, new NodeLocation(0, 0));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        elemDiff = new ElementDiff("E3", null, new NodeLocation(0, 1));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        ElementSnapshot pTree2 = patchHelper.applyInsertions(nTree2, Collections.emptyMap(), insertions);
        assertTreeEquals(TreeSnapshotUtil.buildTree("E(E2,E3)"), pTree2);

        ElementSnapshot nTree3 = TreeSnapshotUtil.buildTree("E");
        insertions = new TreeMap<>();
        elemDiff = new ElementDiff("E2", null, new NodeLocation(0, 0));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        elemDiff = new ElementDiff("E3", null, new NodeLocation(0, 0, 0));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        ElementSnapshot pTree3 = patchHelper.applyInsertions(nTree3, Collections.emptyMap(), insertions);
        assertTreeEquals(TreeSnapshotUtil.buildTree("E(E2(E3))"), pTree3);
    }

    public void testApplyNodePatch() {
        ElementSnapshot negativeElement = new ElementSnapshotImpl("Tag",
                Arrays.asList(new AttributeSnapshotImpl("foo", "bar", null)),
                Arrays.asList(),
                null);
        Map<String, String> negAttrs = new HashMap<>();
        negAttrs.put("foo", "bar");
        Map<String, String> posAttrs = new HashMap<>();
        ElementDiff elementDiff = new ElementDiff(
                negativeElement.getTagName(),
                NodeLocation.root(), NodeLocation.root(),
                negAttrs, posAttrs);
        ElementSnapshot positiveElement = (ElementSnapshot) patchHelper.applyNodePatch(negativeElement, elementDiff, false);
        assertNotSame(negativeElement, positiveElement);
        assertSame(negativeElement, positiveElement.getPreviousSnapshot());
        assertTrue(positiveElement.getAttributes().isEmpty());

        TextNodeSnapshot negativeText = new TextNodeSnapshotImpl("hello world", null);
        TextNodeDiff textDiff = new TextNodeDiff(NodeLocation.root(), NodeLocation.root());
        textDiff.setLines(Arrays.asList(new LineDiff(-1, "hello world")));
        TextNodeSnapshot positiveText = (TextNodeSnapshot) patchHelper.applyNodePatch(negativeText, textDiff, false);
        assertNotSame(negativeText, positiveText);
        assertSame(negativeText, positiveText.getPreviousSnapshot());
        assertEquals("", positiveText.getData());

        // TODO apply patch on null node(create by patch only, for insert new node)
    }

    public void testApplyElementPatch() {
        ElementSnapshot negativeElement = new ElementSnapshotImpl("Tag",
                Arrays.asList(), Arrays.asList(), null);
        ElementDiff emptyDiff = new ElementDiff(
                negativeElement.getTagName(),
                NodeLocation.root(), NodeLocation.root());
        ElementSnapshot positiveElement = patchHelper.applyElementPatch(negativeElement, emptyDiff, false);
        assertSame(negativeElement, positiveElement.getPreviousSnapshot());
        assertEquals(negativeElement.getTagName(), positiveElement.getTagName());
        assertTrue(positiveElement.getAttributes().isEmpty());

        negativeElement = new ElementSnapshotImpl("Tag",
                Arrays.asList(
                        new AttributeSnapshotImpl("a", "a1", null),
                        new AttributeSnapshotImpl("b", "b1", null),
                        new AttributeSnapshotImpl("c", "c1", null)
                ),
                Arrays.asList(), null);

        positiveElement = patchHelper.applyElementPatch(negativeElement, emptyDiff, false);
        assertSame(negativeElement, positiveElement.getPreviousSnapshot());
        assertEquals(negativeElement.getTagName(), positiveElement.getTagName());
        assertEquals(3, positiveElement.getAttributes().size());

        Map<String, String> negativeAttrs = new HashMap<>();
        negativeAttrs.put("b", "b1"); // remove b
        negativeAttrs.put("c", "c1"); // update c

        Map<String, String> positiveAttrs = new HashMap<>();
        positiveAttrs.put("c", "c2"); // update c
        positiveAttrs.put("d", "d1"); // add d

        ElementDiff diff = new ElementDiff(
                negativeElement.getTagName(),
                NodeLocation.root(), NodeLocation.root());
        diff.setNegativeAttributes(negativeAttrs);
        diff.setPositiveAttributes(positiveAttrs);

        positiveElement = patchHelper.applyElementPatch(negativeElement, diff, false);
        assertNotSame(negativeElement, positiveElement);
        assertSame(negativeElement, positiveElement.getPreviousSnapshot());
        assertEquals(3, positiveElement.getAttributes().size());
        Map<String, String> attrMap = new HashMap<>();
        positiveElement.getAttributes().forEach(a -> attrMap.put(a.getName(), a.getValue()));
        assertEquals("a1", attrMap.get("a"));
        assertEquals("c2", attrMap.get("c"));
        assertEquals("d1", attrMap.get("d"));

        // TODO apply patch on null node(create by patch only, for insert new node)
    }

    public void testApplyTextNodePatch() {
        TextNodeSnapshot negativeText = new TextNodeSnapshotImpl(null, null);
        TextNodeDiff emptyDiff = new TextNodeDiff(NodeLocation.root(), NodeLocation.root());
        TextNodeSnapshot positiveText = patchHelper.applyTextNodePatch(negativeText, emptyDiff, false);
        assertSame(negativeText, positiveText.getPreviousSnapshot());
        assertEquals(negativeText.getData(), positiveText.getData());
        assertNull(positiveText.getData());

        negativeText = new TextNodeSnapshotImpl("hello\nworld", null);
        positiveText = patchHelper.applyTextNodePatch(negativeText, emptyDiff, false);
        assertSame(negativeText, positiveText.getPreviousSnapshot());
        assertEquals("hello\nworld", positiveText.getData());

        TextNodeDiff diff = new TextNodeDiff(NodeLocation.root(), NodeLocation.root());
        diff.setLines(Arrays.asList(
                new LineDiff(-2, "world"),
                new LineDiff(2, "world\n!!")
        ));
        positiveText = patchHelper.applyTextNodePatch(negativeText, diff, false);
        assertNotSame(negativeText, positiveText);
        assertSame(negativeText, positiveText.getPreviousSnapshot());
        assertEquals("hello\nworld\n!!", positiveText.getData());

        // TODO apply patch on null node(create by patch only, for insert new node)
    }

    public void testSomethingTemporarily() {
        ElementSnapshot negative = TreeSnapshotUtil.buildTree("A(B(C(D(E))))");
        ElementSnapshot b = (ElementSnapshot) negative.getChildren().get(0);
        ElementSnapshot c = (ElementSnapshot) b.getChildren().get(0);
        ElementSnapshot d = (ElementSnapshot) c.getChildren().get(0);
        ElementSnapshot e = (ElementSnapshot) d.getChildren().get(0);
        ElementSnapshot positive = new ElementSnapshotImpl(
                negative.getTagName(),
                Collections.emptyList(),
                Arrays.asList(
                        new ElementSnapshotImpl(
                                c.getTagName(),
                                c.getAttributes(),
                                Arrays.asList(
                                        new ElementSnapshotImpl(
                                                d.getTagName(),
                                                d.getAttributes(),
                                                Arrays.asList(
                                                        new ElementSnapshotImpl(
                                                                e.getTagName(),
                                                                Arrays.asList(
                                                                        new AttributeSnapshotImpl("foo", "bar")
                                                                ),
                                                                e.getChildren(),
                                                                e
                                                        )
                                                ),
                                                d
                                        )
                                ),
                                c)
                ),
                negative);

        System.out.println(negative);
        System.out.println(positive);
        Patch patch = new DiffHelper().diff(negative, positive);
        System.out.println(patch);
        ElementSnapshot patched = patchHelper.applyPatch(negative, patch);
        System.out.println(patched);
        System.out.println();
    }
}
