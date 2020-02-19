package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.ElementSnapshot;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestAmendHelper extends TestCase {
    public void testDiffChildList() {
        ElementSnapshot a = createElement("a");
        ElementSnapshot b = createElement("b");
        ElementSnapshot c = createElement("c");
        ElementSnapshot d = createElement("d");
        ElementSnapshot e = createElement("e");

        List<AmendHelper.LDAction> ldActions;
        AmendHelper.LDAction action;

        // both empty
        ldActions = runDiffChildList(Collections.emptyList(), Collections.emptyList());
        assertTrue(ldActions.isEmpty());

        // empty A
        ldActions = runDiffChildList(Collections.emptyList(), Arrays.asList(a));
        assertEquals(1, ldActions.size());
        action = ldActions.get(0);
        assertEquals(0, action.index);
        assertTrue(action.positive);
        assertSame(a, action.node);
        ldActions = runDiffChildList(Collections.emptyList(), Arrays.asList(a, b));
        assertEquals(2, ldActions.size());
        action = ldActions.get(0);
        assertEquals(0, action.index);
        assertTrue(action.positive);
        assertSame(a, action.node);
        action = ldActions.get(1);
        assertEquals(1, action.index);
        assertTrue(action.positive);
        assertSame(b, action.node);

        // empty B
        ldActions = runDiffChildList(Arrays.asList(a), Collections.emptyList());
        assertEquals(1, ldActions.size());
        action = ldActions.get(0);
        assertEquals(0, action.index);
        assertFalse(action.positive);
        assertSame(a, action.node);
        ldActions = runDiffChildList(Arrays.asList(a, b), Collections.emptyList());
        assertEquals(2, ldActions.size());
        action = ldActions.get(0);
        assertEquals(0, action.index);
        assertFalse(action.positive);
        assertSame(a, action.node);
        action = ldActions.get(1);
        assertEquals(1, action.index);
        assertFalse(action.positive);
        assertSame(b, action.node);

        // simple replace
        ldActions = runDiffChildList(Arrays.asList(a), Arrays.asList(b));
        assertEquals(2, ldActions.size());
        action = ldActions.get(0);
        assertEquals(0, action.index);
        assertFalse(action.positive);
        assertSame(a, action.node);
        action = ldActions.get(1);
        assertEquals(0, action.index);
        assertTrue(action.positive);
        assertSame(b, action.node);

        // insert to the first place
        ldActions = runDiffChildList(Arrays.asList(a, b), Arrays.asList(c, a, b));
        assertEquals(1, ldActions.size());
        action = ldActions.get(0);
        assertEquals(0, action.index);
        assertTrue(action.positive);
        assertSame(c, action.node);

        // insert to the middle place
        ldActions = runDiffChildList(Arrays.asList(a, b), Arrays.asList(a, c, b));
        assertEquals(1, ldActions.size());
        action = ldActions.get(0);
        assertEquals(1, action.index);
        assertTrue(action.positive);
        assertSame(c, action.node);

        // insert to the end (append)
        ldActions = runDiffChildList(Arrays.asList(a, b), Arrays.asList(a, b, c));
        assertEquals(1, ldActions.size());
        action = ldActions.get(0);
        assertEquals(2, action.index);
        assertTrue(action.positive);
        assertSame(c, action.node);

        // remove first element
        ldActions = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(b, c));
        assertEquals(1, ldActions.size());
        action = ldActions.get(0);
        assertEquals(0, action.index);
        assertFalse(action.positive);
        assertSame(a, action.node);

        // remove middle element
        ldActions = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, c));
        assertEquals(1, ldActions.size());
        action = ldActions.get(0);
        assertEquals(1, action.index);
        assertFalse(action.positive);
        assertSame(b, action.node);

        // remove last element
        ldActions = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, b));
        assertEquals(1, ldActions.size());
        action = ldActions.get(0);
        assertEquals(2, action.index);
        assertFalse(action.positive);
        assertSame(c, action.node);

        // replace first element
        ldActions = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(d, b, c));
        assertEquals(2, ldActions.size());
        action = ldActions.get(0);
        assertEquals(0, action.index);
        assertFalse(action.positive);
        assertSame(a, action.node);
        action = ldActions.get(1);
        assertEquals(0, action.index);
        assertTrue(action.positive);
        assertSame(d, action.node);

        // replace middle element
        ldActions = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, d, c));
        assertEquals(2, ldActions.size());
        action = ldActions.get(0);
        assertEquals(1, action.index);
        assertFalse(action.positive);
        assertSame(b, action.node);
        action = ldActions.get(1);
        assertEquals(1, action.index);
        assertTrue(action.positive);
        assertSame(d, action.node);

        // replace last element
        ldActions = runDiffChildList(Arrays.asList(a, b, c), Arrays.asList(a, b, d));
        assertEquals(2, ldActions.size());
        action = ldActions.get(0);
        assertEquals(2, action.index);
        assertFalse(action.positive);
        assertSame(c, action.node);
        action = ldActions.get(1);
        assertEquals(2, action.index);
        assertTrue(action.positive);
        assertSame(d, action.node);

        // complex case
        ldActions = runDiffChildList(Arrays.asList(a, b, c, d), Arrays.asList(a, d, c, e));
        ldActions.forEach(act -> System.out.println(act.toString()));
        assertEquals(4, ldActions.size());
        action = ldActions.get(0);
        assertEquals(1, action.index);
        assertFalse(action.positive);
        assertSame(b, action.node);
        action = ldActions.get(1);
        assertEquals(1, action.index);
        assertTrue(action.positive);
        assertSame(d, action.node);
        action = ldActions.get(2);
        assertEquals(3, action.index);
        assertFalse(action.positive);
        assertSame(d, action.node);
        action = ldActions.get(3);
        assertEquals(3, action.index);
        assertTrue(action.positive);
        assertSame(e, action.node);
    }

    private List<AmendHelper.LDAction> runDiffChildList(List<ElementSnapshot> childListA, List<ElementSnapshot> childListB) {
        ElementSnapshot nodeA = new ElementSnapshotImpl("row", Collections.emptyList(), childListA, null);
        ElementSnapshot nodeB = new ElementSnapshotImpl("row", Collections.emptyList(), childListB, null);
        return AmendHelper.diffChildList(nodeA, nodeB);
    }

    public void testIsSameNode() {
        ElementSnapshot a = createElement("a");
        ElementSnapshot b = createElement("b");
        ElementSnapshot c = createElement("c");
        ElementSnapshot c2 = createElement("c", c);
        ElementSnapshot c3 = createElement("c", c2);

        assertFalse(AmendHelper.isSameNode(a, b));

        assertTrue(AmendHelper.isSameNode(c, c));
        assertTrue(AmendHelper.isSameNode(c, c2));
        assertTrue(AmendHelper.isSameNode(c2, c));
        assertTrue(AmendHelper.isSameNode(c, c3));
        assertTrue(AmendHelper.isSameNode(c3, c));
    }

    private ElementSnapshot createElement(String tagName) {
        return createElement(tagName, null);
    }

    private ElementSnapshot createElement(String tagName, ElementSnapshot previous) {
        return new ElementSnapshotImpl(tagName, Collections.emptyList(), Collections.emptyList(), previous);
    }

}
