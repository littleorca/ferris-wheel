package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.AttributeSnapshot;
import com.ctrip.ferriswheel.core.dom.ElementSnapshot;
import com.ctrip.ferriswheel.core.dom.NodeSnapshot;
import com.ctrip.ferriswheel.core.dom.TextNodeSnapshot;
import com.ctrip.ferriswheel.core.dom.diff.*;
import com.ctrip.ferriswheel.core.dom.impl.AttributeSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.TextNodeSnapshotImpl;
import junit.framework.TestCase;

import java.util.*;

public class TestDiffHelper extends TestCase {
    private DiffHelper diffHelper;
    private FakeDiffCollector collector;

    @Override
    protected void setUp() throws Exception {
        diffHelper = new DiffHelper();
        collector = new FakeDiffCollector();
    }

    public void testDiffWithoutChange0() {
        ElementSnapshot nA = e("A");
        ElementSnapshot pA = e(nA, "A");
        diffHelper.diff(nA, pA, collector);
        assertTrue(collector.diffList.isEmpty());
    }

    public void testDiffWithoutChange1() {
        AttributeSnapshot attr = a("foo", "bar");
        TextNodeSnapshot txt = t("hello world");
        ElementSnapshot nA = e("A", Arrays.asList(attr), Arrays.asList(txt));
        ElementSnapshot pA = e(nA, "A", Arrays.asList(attr), Arrays.asList(txt));
        diffHelper.diff(nA, pA, collector);
        assertTrue(collector.diffList.isEmpty());
    }

    public void testDiffWithoutMove() {
        AttributeSnapshot attr1 = a("foo", "bar1");
        AttributeSnapshot attr2 = a(attr1, "foo", "bar2");
        TextNodeSnapshot txt1 = t("hello world\n");
        TextNodeSnapshot txt2 = t(txt1, "hello world\n!!");
        ElementSnapshot nA = e("A", Arrays.asList(attr1), Arrays.asList(txt1));
        ElementSnapshot pA = e(nA, "A", Arrays.asList(attr2), Arrays.asList(txt2));
        diffHelper.diff(nA, pA, collector);

        assertEquals(2, collector.diffList.size());
        Diff diff = collector.diffList.get(0);
        assertEquals(new NodeLocation(0), diff.getNegativeLocation());
        assertEquals(new NodeLocation(0), diff.getPositiveLocation());
        assertTrue(diff instanceof ElementDiff);
        ElementDiff elemDiff = (ElementDiff) diff;
        assertEquals(1, elemDiff.getNegativeAttributes().size());
        assertEquals("bar1", elemDiff.getNegativeAttributes().get("foo"));
        assertEquals(1, elemDiff.getPositiveAttributes().size());
        assertEquals("bar2", elemDiff.getPositiveAttributes().get("foo"));

        diff = collector.diffList.get(1);
        assertEquals(new NodeLocation(0, 0), diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 0), diff.getPositiveLocation());
        assertTrue(diff instanceof TextNodeDiff);
        TextNodeDiff txtDiff = (TextNodeDiff) diff;
        assertEquals(1, txtDiff.getLines().size());
        assertEquals(2, txtDiff.getLines().get(0).getOp());
        assertEquals("!!", txtDiff.getLines().get(0).getContent());
    }

    public void testDiffWithInsertion0() {
        ElementSnapshot b1 = e("B", Arrays.asList(), Arrays.asList());
        ElementSnapshot e1 = e("D", Arrays.asList(), Arrays.asList());
        TextNodeSnapshot txt1 = t("hello world\n");
        ElementSnapshot b2 = e(b1, "B", Arrays.asList(), Arrays.asList(e1, txt1));
        ElementSnapshot e2 = e("D", Arrays.asList(), Arrays.asList());
        TextNodeSnapshot txt2 = t("hello world\n");
        ElementSnapshot nA = e("A", Arrays.asList(), Arrays.asList(b1));
        ElementSnapshot pA = e(nA, "A", Arrays.asList(), Arrays.asList(b2, e2, txt2));
        diffHelper.diff(nA, pA, collector);

        assertEquals(6, collector.diffList.size());
        Diff diff = collector.diffList.get(0);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 1), diff.getPositiveLocation());
        assertTrue(diff instanceof ElementDiff);
        assertFalse(diff.hasContent());

        diff = collector.diffList.get(1);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 2), diff.getPositiveLocation());
        assertTrue(diff instanceof TextNodeDiff);
        assertFalse(diff.hasContent());

        diff = collector.diffList.get(2);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 0, 0), diff.getPositiveLocation());
        assertTrue(diff instanceof ElementDiff);
        assertFalse(diff.hasContent());

        diff = collector.diffList.get(3);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 0, 1), diff.getPositiveLocation());
        assertTrue(diff instanceof TextNodeDiff);
        assertFalse(diff.hasContent());

        diff = collector.diffList.get(4);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 0, 1), diff.getPositiveLocation());
        assertTrue(diff instanceof TextNodeDiff);
        assertTrue(diff.hasContent());
        TextNodeDiff txtDiff = (TextNodeDiff) diff;
        assertEquals(1, txtDiff.getLines().size());
        assertEquals(1, txtDiff.getLines().get(0).getOp());
        assertEquals("hello world\n", txtDiff.getLines().get(0).getContent());

        diff = collector.diffList.get(5);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 2), diff.getPositiveLocation());
        assertTrue(diff instanceof TextNodeDiff);
        assertTrue(diff.hasContent());
        txtDiff = (TextNodeDiff) diff;
        assertEquals(1, txtDiff.getLines().size());
        assertEquals(1, txtDiff.getLines().get(0).getOp());
        assertEquals("hello world\n", txtDiff.getLines().get(0).getContent());
    }

    public void testDiffWithInsertion1() {
        ElementSnapshot b = e("B", Arrays.asList(), Arrays.asList());
        ElementSnapshot d = e("D", Arrays.asList(), Arrays.asList());
        ElementSnapshot c = e("C", Arrays.asList(), Arrays.asList(d));
        ElementSnapshot nA = e("A", Arrays.asList(), Arrays.asList(b));
        ElementSnapshot pA = e(nA, "A", Arrays.asList(), Arrays.asList(c, b));
        diffHelper.diff(nA, pA, collector);

        assertEquals(2, collector.diffList.size());
        Diff diff = collector.diffList.get(0);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 0), diff.getPositiveLocation());
        assertTrue(diff instanceof ElementDiff);
        assertFalse(diff.hasContent());

        diff = collector.diffList.get(1);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 0, 0), diff.getPositiveLocation());
        assertTrue(diff instanceof ElementDiff);
        assertFalse(diff.hasContent());
    }

    // TODO test remove/move/etc.

    public void testDiffX() {
        // A(B(C), D, E(F), G) -> A(D(F*), B(C), E*, H)
        ElementSnapshot fixNodeC = e("C-Fix",
                Arrays.asList(a("foo", "bar")),
                Arrays.asList());
        ElementSnapshot moveOnlyB = e("B-MoveOnly", fixNodeC);
        ElementSnapshot moveAndUpdateF = e("F-MoveAndUpdate",
                Arrays.asList(
                        a("a", "1"),
                        a("b", "2"),
                        a("c", "3")
                ),
                Arrays.asList());
        ElementSnapshot moveAndUpdateF2 = e(moveAndUpdateF, "F-MoveAndUpdate",
                Arrays.asList(
                        a("b", "2"),
                        a("c", "30"),
                        a("d", "4")
                ),
                Arrays.asList());
        ElementSnapshot moveOnlyD = e("D-MoveOnly",
                Arrays.asList(a("hello", "world")),
                Arrays.asList());
        ElementSnapshot moveOnlyD2 = e(moveOnlyD, "D-MoveOnly",
                Arrays.asList(a("hello", "world")),
                Arrays.asList(moveAndUpdateF2));
        ElementSnapshot updateOnlyE = e("E-UpdateOnly",
                Arrays.asList(a("foo", "bar")),
                Arrays.asList(moveAndUpdateF));
        ElementSnapshot updateOnlyE2 = e(updateOnlyE, "E-UpdateOnly",
                Arrays.asList(a("foo", "bar2")),
                Arrays.asList());
        ElementSnapshot removeG = e("G-remove",
                Arrays.asList(a("good", "bye")),
                Arrays.asList());
        ElementSnapshot addH = e("H-add",
                Arrays.asList(a("hello", "world")),
                Arrays.asList());

        ElementSnapshot negative = e("A", moveOnlyB, moveOnlyD, updateOnlyE, removeG);
        ElementSnapshot positive = e(negative, "A", moveOnlyD2, moveOnlyB, updateOnlyE2, addH);

        System.out.println(negative);
        System.out.println(positive);

        // check raw diffs
        diffHelper.diff(negative, positive, collector);
        LinkedList<Diff> deletions = new LinkedList<>();
        LinkedList<Diff> insertions = new LinkedList<>();
        LinkedList<Diff> renames = new LinkedList<>();
        LinkedList<Diff> updates = new LinkedList<>();
        for (Diff diff : collector.diffList) {
            if (diff.isDelete()) {
                deletions.add(diff);
            } else if (diff.isInsert()) {
                insertions.add(diff);
            } else if (diff.isUpdate()) {
                if (diff.hasContent()) {
                    updates.add(diff);
                } else {
                    renames.add(diff);
                }
            } else {
                throw new RuntimeException();
            }
        }

        assertEquals(3, deletions.size());
        Diff diff = deletions.get(0);
        assertEquals(new NodeLocation(0, 0), diff.getNegativeLocation());
        diff = deletions.get(1);
        assertEquals(new NodeLocation(0, 3), diff.getNegativeLocation());
        diff = deletions.get(2);
        assertEquals(new NodeLocation(0, 2, 0), diff.getNegativeLocation());

        assertEquals(2, insertions.size());
        diff = insertions.get(0);
        assertEquals(new NodeLocation(0, 3), diff.getPositiveLocation());
        assertFalse(diff.hasContent());
        diff = insertions.get(1);
        assertEquals(new NodeLocation(0, 3), diff.getPositiveLocation());
        assertTrue(diff.hasContent());
        ElementDiff elemDiff = (ElementDiff) diff;
        assertEquals(1, elemDiff.getPositiveAttributes().size());
        assertEquals("world", elemDiff.getPositiveAttributes().get("hello"));
        assertTrue(elemDiff.getNegativeAttributes() == null || elemDiff.getNegativeAttributes().isEmpty());

        assertEquals(2, renames.size());
        diff = renames.get(0);
        assertEquals(new NodeLocation(0, 0), diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 1), diff.getPositiveLocation());
        diff = renames.get(1);
        assertEquals(new NodeLocation(0, 2, 0), diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 0, 0), diff.getPositiveLocation());

        assertEquals(2, updates.size());
        diff = updates.get(0);
        assertTrue(diff instanceof ElementDiff);
        assertEquals(new NodeLocation(0, 2, 0), diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 0, 0), diff.getPositiveLocation());

        Map<String, String> delAttrs = ((ElementDiff) diff).getNegativeAttributes();
        Map<String, String> addAttrs = ((ElementDiff) diff).getPositiveAttributes();
        assertEquals(2, delAttrs.size());
        assertEquals("1", delAttrs.get("a"));
        assertEquals("3", delAttrs.get("c"));
        assertEquals(2, addAttrs.size());
        assertEquals("30", addAttrs.get("c"));
        assertEquals("4", addAttrs.get("d"));

        diff = updates.get(1);
        assertTrue(diff instanceof ElementDiff);
        assertEquals(new NodeLocation(0, 2), diff.getNegativeLocation());
        assertEquals(new NodeLocation(0, 2), diff.getPositiveLocation());

        delAttrs = ((ElementDiff) diff).getNegativeAttributes();
        addAttrs = ((ElementDiff) diff).getPositiveAttributes();
        assertEquals(1, delAttrs.size());
        assertEquals("bar", delAttrs.get("foo"));
        assertEquals(1, addAttrs.size());
        assertEquals("bar2", addAttrs.get("foo"));

        System.out.println("{\n" + diffHelper.diff(negative, positive) + "}\n");
    }

    public void testDiffTextByLine() {
        List<LineDiff> diff;

        diff = new DiffHelper().diffTextByLine("", "");
        assertTrue(diff.isEmpty());

        diff = new DiffHelper().diffTextByLine("\n", "\n");
        assertTrue(diff.isEmpty());

        diff = new DiffHelper().diffTextByLine("abc", "abc");
        assertTrue(diff.isEmpty());

        diff = new DiffHelper().diffTextByLine("abc\n", "abc\n");
        assertTrue(diff.isEmpty());

        diff = new DiffHelper().diffTextByLine("abc", "abc\n");
        assertEquals(2, diff.size());
        LineDiff lineDiff = diff.get(0);
        assertEquals(-1, lineDiff.getOp());
        assertEquals("abc", lineDiff.getContent());
        lineDiff = diff.get(1);
        assertEquals(1, lineDiff.getOp());
        assertEquals("abc\n", lineDiff.getContent());

        diff = new DiffHelper().diffTextByLine("abc\ndef\nghi", "abc\n#def\nghi");
        assertEquals(2, diff.size());
        lineDiff = diff.get(0);
        assertEquals(-2, lineDiff.getOp());
        assertEquals("def\n", lineDiff.getContent());
        lineDiff = diff.get(1);
        assertEquals(2, lineDiff.getOp());
        assertEquals("#def\n", lineDiff.getContent());
    }

    public void testDiffAttributes() {
        LinkedHashMap<String, String> delMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> addMap = new LinkedHashMap<>();
        new DiffHelper().diffAttributes(
                Arrays.asList(
                        a("a", "a1"),
                        a("b", "b1"),
                        a("c", "c1")
                ),
                Arrays.asList(
                        a("b", "b1"),
                        a("c", "c2"),
                        a("d", "d1")
                ),
                delMap, addMap);
        assertEquals(2, delMap.size());
        assertEquals("a1", delMap.get("a"));
        assertEquals("c1", delMap.get("c"));
        assertEquals(2, addMap.size());
        assertEquals("c2", addMap.get("c"));
        assertEquals("d1", addMap.get("d"));
    }

    /**
     * More tests around diff can be found in the test classes of specific
     * difference algorithms.
     */
    public void testDiffChildList() {
        ElementSnapshot a = e("a");
        ElementSnapshot b = e("b");
        ElementSnapshot c = e("c");
        ElementSnapshot d = e("d");
        ElementSnapshot e = e("e");

        List<Diff> diffList;
        Diff diff;

        // both empty
        diffList = runDiffChildList(Collections.emptyList(), Collections.emptyList());
        assertTrue(diffList.isEmpty());

        // empty A
        diffList = runDiffChildList(Collections.emptyList(), Arrays.asList(a));
        assertEquals(1, diffList.size());
        diff = diffList.get(0);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0), diff.getPositiveLocation());
        diffList = runDiffChildList(Collections.emptyList(), Arrays.asList(a, b));
        assertEquals(2, diffList.size());
        diff = diffList.get(0);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0), diff.getPositiveLocation());
        diff = diffList.get(1);
        assertEquals(new NodeLocation(1), diff.getPositiveLocation());

        // empty B
        diffList = runDiffChildList(Arrays.asList(a), Collections.emptyList());
        assertEquals(1, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(0), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());
        diffList = runDiffChildList(Arrays.asList(a, b), Collections.emptyList());
        assertEquals(2, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(0), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());
        diff = diffList.get(1);
        assertEquals(new NodeLocation(1), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());

        // simple replace
        diffList = runDiffChildList(Arrays.asList(a), Arrays.asList(b));
        assertEquals(2, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(0), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());
        diff = diffList.get(1);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0), diff.getPositiveLocation());

        // insert to the first place
        diffList = runDiffChildList(Arrays.asList(a, b), Arrays.asList(c, a, b));
        assertEquals(1, diffList.size());
        diff = diffList.get(0);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0), diff.getPositiveLocation());

        // insert to the middle place
        diffList = runDiffChildList(Arrays.asList(a, b), Arrays.asList(a, c, b));
        assertEquals(1, diffList.size());
        diff = diffList.get(0);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(1), diff.getPositiveLocation());

        // insert to the end (append)
        diffList = runDiffChildList(Arrays.asList(a, b), Arrays.asList(a, b, c));
        assertEquals(1, diffList.size());
        diff = diffList.get(0);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(2), diff.getPositiveLocation());

        // remove first element
        diffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(b, c));
        assertEquals(1, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(0), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());

        // remove middle element
        diffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, c));
        assertEquals(1, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(1), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());

        // remove last element
        diffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, b));
        assertEquals(1, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(2), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());

        // replace first element
        diffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(d, b, c));
        assertEquals(2, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(0), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());
        diff = diffList.get(1);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(0), diff.getPositiveLocation());

        // replace middle element
        diffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, d, c));
        assertEquals(2, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(1), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());
        diff = diffList.get(1);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(1), diff.getPositiveLocation());

        // replace last element
        diffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, b, d));
        assertEquals(2, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(2), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());
        diff = diffList.get(1);
        assertNull(diff.getNegativeLocation());
        assertEquals(new NodeLocation(2), diff.getPositiveLocation());

        // reorder
        diffList = runDiffChildList(Arrays.asList(a, b), Arrays.asList(b, a));
        assertEquals(2, diffList.size());
        diff = diffList.get(0);
        assertEquals(new NodeLocation(0), diff.getNegativeLocation());
        assertNull(diff.getPositiveLocation());
        diff = diffList.get(1);
        assertEquals(new NodeLocation(0), diff.getNegativeLocation());
        assertEquals(new NodeLocation(1), diff.getPositiveLocation());
    }

    private List<Diff> runDiffChildList(List<NodeSnapshot> childListA, List<NodeSnapshot> childListB) {
        ElementSnapshot nodeA = e("row", Collections.emptyList(), childListA);
        ElementSnapshot nodeB = e(nodeA, "row", Collections.emptyList(), childListB);

        DiffCollector collector = new FakeDiffCollector();
        DiffHelper.DiffContext context = new DiffHelper.DiffContext(nodeA, new NodeLocation(), nodeB, new NodeLocation(), collector);
        new DiffHelper().diffChildList(context, nodeA, nodeB);

        return context.createPatch().getDiffList();
    }

    public void testIsSameNode() {
        ElementSnapshot a = e("a");
        ElementSnapshot b = e("b");
        ElementSnapshot c = e("c");
        ElementSnapshot c2 = e(c, "c");
        ElementSnapshot c3 = e(c2, "c");

        DiffHelper diffHelper = new DiffHelper();

        assertFalse(diffHelper.isSameNode(a, b));

        assertTrue(diffHelper.isSameNode(c, c));
        assertTrue(diffHelper.isSameNode(c, c2));
        assertTrue(diffHelper.isSameNode(c2, c));
        assertTrue(diffHelper.isSameNode(c, c3));
        assertTrue(diffHelper.isSameNode(c3, c));
    }

    private ElementSnapshot e(String name, ElementSnapshot... children) {
        return e(null, name, children);
    }

    private ElementSnapshot e(ElementSnapshot previous, String name, ElementSnapshot... children) {
        return e(previous, name, null, children);
    }

    private ElementSnapshot e(ElementSnapshot previous, String name, AttributeSnapshot[] attrs, ElementSnapshot[] children) {
        return e(previous, name,
                attrs == null ? Collections.emptyList() : Arrays.asList(attrs),
                children == null ? Collections.emptyList() : Arrays.asList(children));
    }

    private ElementSnapshot e(String name, List<AttributeSnapshot> attrs, List<NodeSnapshot> children) {
        return e(null, name, attrs, children);
    }

    private ElementSnapshot e(ElementSnapshot previous, String name, List<AttributeSnapshot> attrs, List<NodeSnapshot> children) {
        return new ElementSnapshotImpl(name,
                attrs == null ? Collections.emptyList() : attrs,
                children == null ? Collections.emptyList() : children,
                previous);
    }

    private AttributeSnapshot a(String name, String value) {
        return a(null, name, value);
    }

    private AttributeSnapshot a(AttributeSnapshot previous, String name, String value) {
        return new AttributeSnapshotImpl(name, value, previous);
    }

    private TextNodeSnapshot t(String data) {
        return t(null, data);
    }

    private TextNodeSnapshot t(TextNodeSnapshot prev, String data) {
        return new TextNodeSnapshotImpl(data, prev);
    }

    class FakeDiffCollector implements DiffCollector {
        private List<Diff> diffList = new LinkedList<>();

        @Override
        public void add(Diff diff) {
            diffList.add(diff);
        }

        @Override
        public Patch toPatch() {
            Patch p = new Patch();
            p.setDiffList(diffList);
            return p;
        }

        @Override
        public void clear() {
            diffList = new LinkedList<>();
        }
    }
}
