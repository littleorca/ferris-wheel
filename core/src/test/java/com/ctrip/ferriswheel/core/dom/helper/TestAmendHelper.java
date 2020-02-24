package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.ElementSnapshot;
import com.ctrip.ferriswheel.core.dom.diff.ElementDiff;
import com.ctrip.ferriswheel.core.dom.diff.NodeLocation;
import com.ctrip.ferriswheel.core.dom.diff.TextNodeDiff;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TestAmendHelper extends TestCase {
    public void testDiffChildList() {
        ElementSnapshot a = createElement("a");
        ElementSnapshot b = createElement("b");
        ElementSnapshot c = createElement("c");
        ElementSnapshot d = createElement("d");
        ElementSnapshot e = createElement("e");

        List<ElementDiff> elementDiffList;
        ElementDiff elementDiff;

        // both empty
        elementDiffList = runDiffChildList(Collections.emptyList(), Collections.emptyList());
        assertTrue(elementDiffList.isEmpty());

        // empty A
        elementDiffList = runDiffChildList(Collections.emptyList(), Arrays.asList(a));
        assertEquals(1, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(0), elementDiff.getPositiveLocation());
        elementDiffList = runDiffChildList(Collections.emptyList(), Arrays.asList(a, b));
        assertEquals(2, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(0), elementDiff.getPositiveLocation());
        elementDiff = elementDiffList.get(1);
        assertEquals(new NodeLocation(1), elementDiff.getPositiveLocation());

        // empty B
        elementDiffList = runDiffChildList(Arrays.asList(a), Collections.emptyList());
        assertEquals(1, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(0), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());
        elementDiffList = runDiffChildList(Arrays.asList(a, b), Collections.emptyList());
        assertEquals(2, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(0), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());
        elementDiff = elementDiffList.get(1);
        assertEquals(new NodeLocation(1), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());

        // simple replace
        elementDiffList = runDiffChildList(Arrays.asList(a), Arrays.asList(b));
        assertEquals(2, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(0), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());
        elementDiff = elementDiffList.get(1);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(0), elementDiff.getPositiveLocation());

        // insert to the first place
        elementDiffList = runDiffChildList(Arrays.asList(a, b), Arrays.asList(c, a, b));
        assertEquals(1, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(0), elementDiff.getPositiveLocation());

        // insert to the middle place
        elementDiffList = runDiffChildList(Arrays.asList(a, b), Arrays.asList(a, c, b));
        assertEquals(1, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(1), elementDiff.getPositiveLocation());

        // insert to the end (append)
        elementDiffList = runDiffChildList(Arrays.asList(a, b), Arrays.asList(a, b, c));
        assertEquals(1, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(2), elementDiff.getPositiveLocation());

        // remove first element
        elementDiffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(b, c));
        assertEquals(1, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(0), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());

        // remove middle element
        elementDiffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, c));
        assertEquals(1, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(1), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());

        // remove last element
        elementDiffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, b));
        assertEquals(1, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(2), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());

        // replace first element
        elementDiffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(d, b, c));
        assertEquals(2, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(0), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());
        elementDiff = elementDiffList.get(1);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(0), elementDiff.getPositiveLocation());

        // replace middle element
        elementDiffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, d, c));
        assertEquals(2, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(1), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());
        elementDiff = elementDiffList.get(1);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(1), elementDiff.getPositiveLocation());

        // replace last element
        elementDiffList = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, b, d));
        assertEquals(2, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(2), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());
        elementDiff = elementDiffList.get(1);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(2), elementDiff.getPositiveLocation());

        // complex case
        elementDiffList = runDiffChildList(Arrays.asList(a, b, c, d), Arrays.asList(a, d, c, e));
        elementDiffList.forEach(act -> System.out.println(act.toString()));
        assertEquals(4, elementDiffList.size());
        elementDiff = elementDiffList.get(0);
        assertEquals(new NodeLocation(1), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());
        elementDiff = elementDiffList.get(1);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(1), elementDiff.getPositiveLocation());
        elementDiff = elementDiffList.get(2);
        assertEquals(new NodeLocation(3), elementDiff.getNegativeLocation());
        assertNull(elementDiff.getPositiveLocation());
        elementDiff = elementDiffList.get(3);
        assertNull(elementDiff.getNegativeLocation());
        assertEquals(new NodeLocation(3), elementDiff.getPositiveLocation());
    }

    private List<ElementDiff> runDiffChildList(List<ElementSnapshot> childListA, List<ElementSnapshot> childListB) {
        ElementSnapshot nodeA = new ElementSnapshotImpl("row", Collections.emptyList(), childListA, null);
        ElementSnapshot nodeB = new ElementSnapshotImpl("row", Collections.emptyList(), childListB, null);

        FakeChangeCollector changeCollector = new FakeChangeCollector();
        AmendHelper.TreeIndexer negativeTreeIndexer = new AmendHelper.TreeIndexer(nodeA, new NodeLocation());
        AmendHelper.DiffContext context = new AmendHelper.DiffContext();
        context.collector = changeCollector;
        context.negativeIndexer = negativeTreeIndexer;
        context.positiveLocation = new NodeLocation();

        new AmendHelper().diffChildList(context, nodeA, nodeB);

        LinkedList<ElementDiff> list = changeCollector.elementDiffList;
        Collections.reverse(changeCollector.elementDiffList);
        return list;
    }

    public void testIsSameNode() {
        ElementSnapshot a = createElement("a");
        ElementSnapshot b = createElement("b");
        ElementSnapshot c = createElement("c");
        ElementSnapshot c2 = createElement("c", c);
        ElementSnapshot c3 = createElement("c", c2);

        AmendHelper amendHelper = new AmendHelper();

        assertFalse(amendHelper.isSameNode(a, b));

        assertTrue(amendHelper.isSameNode(c, c));
        assertTrue(amendHelper.isSameNode(c, c2));
        assertTrue(amendHelper.isSameNode(c2, c));
        assertTrue(amendHelper.isSameNode(c, c3));
        assertTrue(amendHelper.isSameNode(c3, c));
    }

    private ElementSnapshot createElement(String tagName) {
        return createElement(tagName, null);
    }

    private ElementSnapshot createElement(String tagName, ElementSnapshot previous) {
        return new ElementSnapshotImpl(tagName, Collections.emptyList(), Collections.emptyList(), previous);
    }

    class FakeChangeCollector implements ChangeCollector {
        LinkedList<ElementDiff> elementDiffList = new LinkedList<>();

        @Override
        public void add(ElementDiff elementDiff) {
            elementDiffList.add(elementDiff);
        }

        @Override
        public void add(TextNodeDiff textNodeDiff) {
            throw new RuntimeException();
        }
    }
}
