package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.Element;
import com.ctrip.ferriswheel.core.dom.ElementSnapshot;
import com.ctrip.ferriswheel.core.dom.NodeSnapshot;
import com.ctrip.ferriswheel.core.dom.TextNodeSnapshot;
import com.ctrip.ferriswheel.core.dom.impl.AbstractDocument;
import com.ctrip.ferriswheel.core.dom.impl.AbstractElement;
import com.ctrip.ferriswheel.core.dom.impl.TextNodeImpl;
import junit.framework.TestCase;

public class TestSnapshotHelper extends TestCase {
    private NodeRevisionTracer tracer;
    private TestDocument doc;

    @Override
    protected void setUp() throws Exception {
        this.tracer = new WeakNodeSnapshotTracer();
        this.doc = new TestDocument();
    }

    public void testSnapshotNode() {
        Element a = doc.createElement("A");
        a.setAttribute("foo", "bar");

        TextNodeImpl b = doc.createTextNode("Hello world!");

        Element c = doc.createElement("C");
        c.setAttribute("f", "b");

        NodeSnapshot snapshotA = SnapshotHelper.snapshotNode(a, tracer);
        assertNull(snapshotA.getPreviousSnapshot());
        assertEquals("A", snapshotA.getNodeName());
        assertNotNull(tracer.getSnapshot(a));

        NodeSnapshot snapshotB = SnapshotHelper.snapshotNode(b, tracer);
        assertNull(snapshotB.getPreviousSnapshot());
        assertEquals("Hello world!", snapshotB.getNodeValue());
        assertNotNull(tracer.getSnapshot(a));

        // clean but no previous snapshot
        c.setDirty(false);
        NodeSnapshot snapshotC = SnapshotHelper.snapshotNode(c, tracer);
        assertNull(snapshotC.getPreviousSnapshot());
        assertEquals("C", snapshotC.getNodeName());

        // dirty with previous snapshot

        a.setDirty(true);
        NodeSnapshot snapshotA2 = SnapshotHelper.snapshotNode(a, tracer);
        assertNotSame(snapshotA, snapshotA2);

        b.setDirty(true);
        NodeSnapshot snapshotB2 = SnapshotHelper.snapshotNode(b, tracer);
        assertNotSame(snapshotB, snapshotB2);

        // clean with previous snapshot

        a.setDirty(false);
        NodeSnapshot snapshotA3 = SnapshotHelper.snapshotNode(a, tracer);
        assertSame(snapshotA2, snapshotA3);

        b.setDirty(false);
        NodeSnapshot snapshotB3 = SnapshotHelper.snapshotNode(b, tracer);
        assertSame(snapshotB2, snapshotB3);
    }

    public void testSnapshotElement() {
        Element a = doc.createElement("A");
        a.setAttribute("foo", "bar");

        Element b = doc.createElement("B");
        b.setAttribute("f", "b");

        TextNodeImpl c = doc.createTextNode("Hello world!");

        a.appendChild(b);
        a.appendChild(c);

        ElementSnapshot snapshotA = SnapshotHelper.snapshotElement(a, tracer);
        assertNull(snapshotA.getPreviousSnapshot());
        assertEquals("A", snapshotA.getTagName());
        assertEquals(1, snapshotA.getAttributes().size());
        assertEquals("bar", snapshotA.getAttribute("foo"));

        assertEquals(2, snapshotA.getChildCount());
        NodeSnapshot snapshotB = snapshotA.getChildren().get(0);
        NodeSnapshot snapshotC = snapshotA.getChildren().get(1);

        assertNull(snapshotB.getPreviousSnapshot());
        assertEquals("B", snapshotB.getNodeName());
        assertNull(snapshotC.getPreviousSnapshot());
        assertEquals("Hello world!", snapshotC.getNodeValue());

        tracer = new WeakNodeSnapshotTracer();
        tracer.setSnapshot(a, snapshotA);
        tracer.setSnapshot(b, snapshotB);
        tracer.setSnapshot(c, snapshotC);

        ElementSnapshot snapshotA2 = SnapshotHelper.snapshotElement(a, tracer);
        assertNotSame(snapshotA, snapshotA2);
        assertSame(snapshotA, snapshotA2.getPreviousSnapshot());
        assertEquals("A", snapshotA2.getTagName());
        assertEquals(1, snapshotA2.getAttributes().size());
        assertEquals("bar", snapshotA2.getAttribute("foo"));

        assertEquals(2, snapshotA2.getChildCount());
        NodeSnapshot snapshotB2 = snapshotA2.getChildren().get(0);
        NodeSnapshot snapshotC2 = snapshotA2.getChildren().get(1);

        assertSame(snapshotB, snapshotB2.getPreviousSnapshot());
        assertEquals("B", snapshotB2.getNodeName());
        assertSame(snapshotC, snapshotC2.getPreviousSnapshot());
        assertEquals("Hello world!", snapshotC2.getNodeValue());
    }

    public void testSnapshotTextNode() {
        TextNodeImpl textNode = doc.createTextNode("Hello world!");
        TextNodeSnapshot snapshot = SnapshotHelper.snapshotText(textNode, null);
        assertNull(snapshot.getPreviousSnapshot());
        assertEquals(textNode.getData(), snapshot.getNodeValue());

        TextNodeSnapshot snapshot2 = SnapshotHelper.snapshotText(textNode, snapshot);
        assertNotSame(snapshot, snapshot2);
        assertSame(snapshot, snapshot2.getPreviousSnapshot());
        assertEquals(textNode.getData(), snapshot2.getNodeValue());
    }

    class TestDocument extends AbstractDocument {
        @Override
        protected Element createDocumentElement() {
            return createElement("root");
        }

        @Override
        public Element createElement(String tagName) {
            return new TestElement(tagName, this);
        }

        @Override
        public <E extends Element> E createElement(Class<E> elementClass) {
            return null;
        }
    }

    class TestElement extends AbstractElement {
        private String tagName;

        public TestElement(String tagName, AbstractDocument ownerDoc) {
            super(ownerDoc);
            this.tagName = tagName;
        }

        @Override
        public String getTagName() {
            return tagName;
        }
    }

}
