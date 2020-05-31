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

import com.ctrip.ferriswheel.core.dom.*;
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
        System.out.println(patched);// FIXME do assertions
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

    public void testAnalyseDiff() {
        Patch patch = new Patch();
        TreeSet<NodeLocation> deletions = new TreeSet<>();
        TreeSet<NodeLocation> renames = new TreeSet<>();
        TreeMap<NodeLocation, Diff> insertions = new TreeMap<>();
        patchHelper.analysePatch(patch, deletions, renames, insertions);
        assertTrue(deletions.isEmpty());
        assertTrue(renames.isEmpty());
        assertTrue(insertions.isEmpty());

        ElementDiff diff1 = new ElementDiff("E", new NodeLocation(0, 0), null);
        ElementDiff diff2_1 = new ElementDiff("E2", new NodeLocation(0, 1), new NodeLocation(0, 2));
        ElementDiff diff2_2 = new ElementDiff("E2", new NodeLocation(0, 1), new NodeLocation(0, 2));
        Map<String, String> positiveAttrs = new HashMap<>();
        positiveAttrs.put("a", "1");
        diff2_2.setPositiveAttributes(positiveAttrs);
        ElementDiff diff2_3 = new ElementDiff("E2", new NodeLocation(0, 1), new NodeLocation(0, 2));
        ElementDiff diff3 = new ElementDiff("E3", null, new NodeLocation(0, 3));
        patch.setDiffList(Arrays.asList(diff1, diff2_1, diff2_2, diff2_3, diff3));

        patchHelper.analysePatch(patch, deletions, renames, insertions);
        assertEquals(2, deletions.size());
        assertTrue(deletions.contains(new NodeLocation(0, 0)));
        assertTrue(deletions.contains(new NodeLocation(0, 1)));
        assertEquals(1, renames.size());
        assertTrue(renames.contains(new NodeLocation(0, 1)));
        assertEquals(2, insertions.size());
        ElementDiff diff2 = (ElementDiff) insertions.get(new NodeLocation(0, 2));
        assertEquals(new NodeLocation(0, 1), diff2.getNegativeLocation());
        assertTrue(diff2.getNegativeAttributes().isEmpty());
        assertEquals(1, diff2.getPositiveAttributes().size());
        assertEquals("1", diff2.getPositiveAttributes().get("a"));
        assertNull(insertions.get(new NodeLocation(0, 3)).getNegativeLocation());
    }

    public void testApplyDeletions() {
        ElementSnapshot nTree1 = TreeSnapshotUtil.buildTree("E");
        NavigableMap<NodeLocation, NodeSnapshotOrBuilder> tempNodes = patchHelper.applyDeletions(nTree1,
                Collections.emptyNavigableSet(), Collections.emptyNavigableSet());
        assertTrue(tempNodes.isEmpty());

        ElementSnapshot nTree2 = TreeSnapshotUtil.buildTree("E(E2)");
        NavigableSet<NodeLocation> deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0));
        tempNodes = patchHelper.applyDeletions(nTree2, deletions, Collections.emptyNavigableSet());
        ElementSnapshotOrBuilder pTree2 = (ElementSnapshotOrBuilder) tempNodes.get(NodeLocation.root());
        assertTreeEquals(nTree1, pTree2);
        assertEquals(1, tempNodes.size());
        assertSame(nTree2, pTree2.getPreviousSnapshot());

        ElementSnapshot nTree3 = TreeSnapshotUtil.buildTree("E(E2(E3))");
        deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0, 0));
        tempNodes = patchHelper.applyDeletions(nTree3, deletions, Collections.emptyNavigableSet());
        ElementSnapshotOrBuilder pTree3 = (ElementSnapshotOrBuilder) tempNodes.get(NodeLocation.root());
        assertTreeEquals(nTree2, pTree3);
        assertEquals(2, tempNodes.size());
        assertEquals("E2", tempNodes.get(new NodeLocation(0, 0)).getNodeName());

        ElementSnapshot nTree4 = TreeSnapshotUtil.buildTree("E(E2(E3))");
        deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0));
        tempNodes = patchHelper.applyDeletions(nTree4, deletions, Collections.emptyNavigableSet());
        ElementSnapshotOrBuilder pTree4 = (ElementSnapshotOrBuilder) tempNodes.get(NodeLocation.root());
        assertTreeEquals(nTree1, pTree4);
        assertEquals(1, tempNodes.size());

        ElementSnapshot nTree5 = TreeSnapshotUtil.buildTree("E(E2(E3))");
        deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0));
        deletions.add(new NodeLocation(0, 0, 0));
        tempNodes = patchHelper.applyDeletions(nTree5, deletions, Collections.emptyNavigableSet());
        ElementSnapshotOrBuilder pTree5 = (ElementSnapshotOrBuilder) tempNodes.get(NodeLocation.root());
        assertTreeEquals(nTree1, pTree5);
        assertEquals(2, tempNodes.size());
        assertEquals("E2", tempNodes.get(new NodeLocation(0, 0)).getNodeName());

        ElementSnapshot nTree6 = TreeSnapshotUtil.buildTree("E(E2,E3,E4(E6),E5)");
        deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0));
        deletions.add(new NodeLocation(0, 2));
        deletions.add(new NodeLocation(0, 3));
        tempNodes = patchHelper.applyDeletions(nTree6, deletions, Collections.emptyNavigableSet());
        ElementSnapshotOrBuilder pTree6 = (ElementSnapshotOrBuilder) tempNodes.get(NodeLocation.root());
        assertTreeEquals(TreeSnapshotUtil.buildTree("E(E3)"), pTree6);
        assertEquals(1, tempNodes.size());

        ElementSnapshot nTree7 = TreeSnapshotUtil.buildTree("E(E2,E3,E4(E6),E5)");
        deletions = new TreeSet<>();
        deletions.add(new NodeLocation(0, 0));
        deletions.add(new NodeLocation(0, 2));
        deletions.add(new NodeLocation(0, 3));
        TreeSet<NodeLocation> renames = new TreeSet();
        renames.add(new NodeLocation(0, 2));
        tempNodes = patchHelper.applyDeletions(nTree7, deletions, renames);
        ElementSnapshotOrBuilder pTree7 = (ElementSnapshotOrBuilder) tempNodes.get(NodeLocation.root());
        assertTreeEquals(TreeSnapshotUtil.buildTree("E(E3)"), pTree7);
        assertEquals(2, tempNodes.size());
        ElementSnapshotOrBuilder pE6 = (ElementSnapshotOrBuilder) tempNodes.get(new NodeLocation(0, 2));
        assertTreeEquals(TreeSnapshotUtil.buildTree("E4(E6)"), pE6);
    }

    public void testApplyStaticPatch() {
        NavigableMap<NodeLocation, NodeSnapshotOrBuilder> tempNodes = new TreeMap<>();
        NavigableMap<NodeLocation, NodeSnapshotOrBuilder> positiveTempNodes =
                patchHelper.applyStaticPatch(tempNodes, Arrays.asList());
        assertTrue(positiveTempNodes.isEmpty());

        // temp nodes indexed by negative location
        // diff: move, update, update and move
        //

        ElementSnapshotImpl e = new ElementSnapshotImpl("E",
                Collections.emptyList(), Collections.emptyList(), null);
        ElementSnapshotImpl e1 = new ElementSnapshotImpl("E1",
                Collections.emptyList(), Collections.emptyList(), null);
        ElementSnapshotImpl r = new ElementSnapshotImpl("R",
                Collections.emptyList(), Arrays.asList(e, e1), null);
        tempNodes.put(new NodeLocation(0), r);
        tempNodes.put(new NodeLocation(0, 0), e);
        tempNodes.put(new NodeLocation(0, 1), e1);
        positiveTempNodes = patchHelper.applyStaticPatch(tempNodes, Arrays.asList(
                new ElementDiff("E", new NodeLocation(0, 0), new NodeLocation(0, 1)),
                new ElementDiff("NEW", null, new NodeLocation(0, 2))
        ));
        assertEquals(2, positiveTempNodes.size());
        ElementSnapshotOrBuilder readE = (ElementSnapshotOrBuilder) positiveTempNodes.get(new NodeLocation(0, 1));
        assertTreeEquals(e, readE);
        ElementSnapshotOrBuilder readNew = (ElementSnapshotOrBuilder) positiveTempNodes.get(new NodeLocation(0, 2));
        assertTreeEquals(new ElementSnapshotImpl("NEW",
                Collections.emptyList(), Collections.emptyList(), null), readNew);
    }

    public void testApplyInsertionsWithSimpleCases() {
        ElementSnapshot nTree1 = TreeSnapshotUtil.buildTree("E");
        TreeMap<NodeLocation, NodeSnapshotOrBuilder> tempNodes = new TreeMap<>();
        tempNodes.put(new NodeLocation(0, 0), new ElementSnapshotImpl("E2",
                Collections.emptyList(), Collections.emptyList(), null));
        TreeMap<NodeLocation, Diff> insertions = new TreeMap<>();
        ElementDiff elemDiff = new ElementDiff("E2", null, new NodeLocation(0, 0));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        ElementSnapshotOrBuilder pTree1 = patchHelper.applyInsertions(nTree1, tempNodes, insertions);
        assertTreeEquals(TreeSnapshotUtil.buildTree("E(E2)"), pTree1);

        ElementSnapshot nTree2 = TreeSnapshotUtil.buildTree("E");
        tempNodes = new TreeMap<>();
        tempNodes.put(new NodeLocation(0, 0), new ElementSnapshotImpl("E2",
                Collections.emptyList(), Collections.emptyList(), null));
        tempNodes.put(new NodeLocation(0, 1), new ElementSnapshotImpl("E3",
                Collections.emptyList(), Collections.emptyList(), null));
        insertions = new TreeMap<>();
        elemDiff = new ElementDiff("E2", null, new NodeLocation(0, 0));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        elemDiff = new ElementDiff("E3", null, new NodeLocation(0, 1));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        ElementSnapshotOrBuilder pTree2 = patchHelper.applyInsertions(nTree2, tempNodes, insertions);
        assertTreeEquals(TreeSnapshotUtil.buildTree("E(E2,E3)"), pTree2);

        ElementSnapshot nTree3 = TreeSnapshotUtil.buildTree("E");
        tempNodes = new TreeMap<>();
        tempNodes.put(new NodeLocation(0, 0), new ElementSnapshotImpl("E2",
                Collections.emptyList(), Collections.emptyList(), null));
        tempNodes.put(new NodeLocation(0, 0, 0), new ElementSnapshotImpl("E3",
                Collections.emptyList(), Collections.emptyList(), null));
        insertions = new TreeMap<>();
        elemDiff = new ElementDiff("E2", null, new NodeLocation(0, 0));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        elemDiff = new ElementDiff("E3", null, new NodeLocation(0, 0, 0));
        insertions.put(elemDiff.getPositiveLocation(), elemDiff);
        ElementSnapshotOrBuilder pTree3 = patchHelper.applyInsertions(nTree3, tempNodes, insertions);
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
        ElementSnapshotOrBuilder positiveElement = (ElementSnapshotOrBuilder) patchHelper.applyNodePatch(negativeElement, elementDiff);
        assertNotSame(negativeElement, positiveElement);
        assertSame(negativeElement, positiveElement.getPreviousSnapshot());
        assertTrue(positiveElement.getAttributes().isEmpty());

        TextNodeSnapshot negativeText = new TextNodeSnapshotImpl("hello world", null);
        TextNodeDiff textDiff = new TextNodeDiff(NodeLocation.root(), NodeLocation.root());
        textDiff.setLines(Arrays.asList(new LineDiff(-1, "hello world")));
        TextNodeSnapshotOrBuilder positiveText = (TextNodeSnapshotOrBuilder) patchHelper.applyNodePatch(negativeText, textDiff);
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
        ElementSnapshotBuilder positiveElement = patchHelper.applyElementPatch(negativeElement, emptyDiff);
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

        positiveElement = patchHelper.applyElementPatch(negativeElement, emptyDiff);
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

        positiveElement = patchHelper.applyElementPatch(negativeElement, diff);
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
        TextNodeSnapshotOrBuilder positiveText = patchHelper.applyTextNodePatch(negativeText, emptyDiff);
        assertSame(negativeText, positiveText.getPreviousSnapshot());
        assertEquals(negativeText.getData(), positiveText.getData());
        assertNull(positiveText.getData());

        negativeText = new TextNodeSnapshotImpl("hello\nworld", null);
        positiveText = patchHelper.applyTextNodePatch(negativeText, emptyDiff);
        assertSame(negativeText, positiveText.getPreviousSnapshot());
        assertEquals("hello\nworld", positiveText.getData());

        TextNodeDiff diff = new TextNodeDiff(NodeLocation.root(), NodeLocation.root());
        diff.setLines(Arrays.asList(
                new LineDiff(-2, "world"),
                new LineDiff(2, "world\n!!")
        ));
        positiveText = patchHelper.applyTextNodePatch(negativeText, diff);
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
