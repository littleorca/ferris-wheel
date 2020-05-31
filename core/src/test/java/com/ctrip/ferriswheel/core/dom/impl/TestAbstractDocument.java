package com.ctrip.ferriswheel.core.dom.impl;

import com.ctrip.ferriswheel.core.dom.Element;
import com.ctrip.ferriswheel.core.dom.helper.ElementSnapshotBuilder;
import com.ctrip.ferriswheel.core.dom.helper.TextNodeSnapshotBuilder;
import junit.framework.TestCase;

import java.util.Arrays;

public class TestAbstractDocument extends TestCase {
    static class DocumentMock extends AbstractDocument {
        @Override
        protected Element createDocumentElement() {
            return createElement("root");
        }

        @Override
        public Element createElement(String tagName) {
            ElementMock el = new ElementMock(this);
            el.setTagName(tagName);
            return el;
        }

        @Override
        public <E extends Element> E createElement(Class<E> elementClass) {
            return null;
        }
    }

    static class ElementMock extends AbstractElement {
        private String tagName;

        protected ElementMock(AbstractDocument ownerDocument) {
            super(ownerDocument);
        }

        @Override
        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }
    }

    public void testCreateFromEssential() {
        ElementSnapshotBuilder snapshotBuilder = new ElementSnapshotBuilder();
        snapshotBuilder.setTagName("root");
        snapshotBuilder.setAttribute("foo", "bar");
        snapshotBuilder.setChildren(Arrays.asList(
                new ElementSnapshotBuilder().setTagName("head"),
                new ElementSnapshotBuilder().setTagName("body")
                        .addChild(0, new TextNodeSnapshotBuilder().setData("body words")),
                new TextNodeSnapshotBuilder().setData("text")
        ));
        System.out.println(snapshotBuilder);
        DocumentMock doc = new DocumentMock();
        Element docEl = doc.createDocumentElement(snapshotBuilder);
        System.out.println(docEl);
    }
}
