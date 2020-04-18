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

package com.ctrip.ferriswheel.core.dom;

import com.ctrip.ferriswheel.core.dom.impl.AttributeSnapshotImpl;
import com.ctrip.ferriswheel.core.dom.impl.ElementSnapshotImpl;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static junit.framework.TestCase.*;

public class TreeSnapshotUtil {

    /**
     * Build tree from descriptor, e.g. E(C1,C2[a=1]).
     * Tag name and attribute name should follow regexp [a-zA-Z]+
     * Attribute value should follow regexp [0-9]+
     *
     * @param descriptor
     * @return
     */
    public static ElementSnapshot buildTree(String descriptor) {
        AtomicReference ref = new AtomicReference();
        parseTree(descriptor, 0, ref);
        return (ElementSnapshot) ref.get();
    }

    private static int parseTree(String descriptor, int offset, AtomicReference ref) {
        int pos = offset;
        while (pos < descriptor.length() &&
                descriptor.charAt(pos) != '(' &&
                descriptor.charAt(pos) != '[' &&
                descriptor.charAt(pos) != ',' &&
                descriptor.charAt(pos) != ')') {
            pos++;
        }

        String tagName = descriptor.substring(offset, pos).trim();

        if (pos >= descriptor.length()) {
            ref.set(new ElementSnapshotImpl(tagName,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    null));
            return pos;
        }

        List<AttributeSnapshot> attrs = new LinkedList<>();

        if (descriptor.charAt(pos) == '[') {
            AtomicReference<AttributeSnapshot> attrRef = new AtomicReference<>();
            do {
                pos = parseAttr(descriptor, pos + 1, attrRef);
                if (attrRef.get() != null) {
                    attrs.add(attrRef.get());
                    attrRef.set(null);
                }
            } while (descriptor.charAt(pos) == ',');
            if (descriptor.charAt(pos) != ']') {
                throw new IllegalArgumentException();
            }
            pos++;
        }

        List<NodeSnapshot> children = new LinkedList<>();
        if (pos < descriptor.length() && descriptor.charAt(pos) == '(') {
            AtomicReference<NodeSnapshot> childRef = new AtomicReference<>();
            do {
                pos = parseTree(descriptor, pos + 1, childRef);
                if (childRef.get() != null) {
                    children.add(childRef.get());
                    childRef.set(null);
                }
            } while (descriptor.charAt(pos) == ',');
            if (descriptor.charAt(pos) != ')') {
                throw new IllegalArgumentException();
            }
            pos++;
        }

        ref.set(new ElementSnapshotImpl(tagName, attrs, children, null));

        return pos;
    }

    private static int parseAttr(String descriptor, int offset, AtomicReference<AttributeSnapshot> attrRef) {
        int prevPos = offset, pos = prevPos;
        while (pos < descriptor.length() && descriptor.charAt(pos) != '=') {
            pos++;
        }
        if (pos >= descriptor.length()) {
            throw new IllegalArgumentException();
        }

        String name = descriptor.substring(prevPos, pos).trim();

        prevPos = ++pos;
        while (pos < descriptor.length() && descriptor.charAt(pos) != ',' && descriptor.charAt(pos) != ']') {
            pos++;
        }
        if (pos >= descriptor.length()) {
            throw new IllegalArgumentException();
        }

        String value = descriptor.substring(prevPos, pos).trim();

        attrRef.set(new AttributeSnapshotImpl(name, value, null));

        return pos;
    }


    public static void assertTreeEquals(ElementSnapshot expected, ElementSnapshotOrBuilder actual) {
        Stack<NodeSnapshotOrBuilder> stackExpected = new Stack<>();
        Stack<NodeSnapshotOrBuilder> stackActual = new Stack<>();
        stackExpected.push(expected);
        stackActual.push(actual);
        while (!stackExpected.isEmpty()) {
            assertFalse(stackActual.isEmpty());
            NodeSnapshotOrBuilder expectedOne = stackExpected.pop();
            NodeSnapshotOrBuilder actualOne = stackActual.pop();

            if (expectedOne instanceof ElementSnapshotOrBuilder) {
                assertTrue(actualOne instanceof ElementSnapshotOrBuilder);

                ElementSnapshotOrBuilder expectedElem = (ElementSnapshotOrBuilder) expectedOne;
                ElementSnapshotOrBuilder actualElem = (ElementSnapshotOrBuilder) actualOne;
                assertElementContentEquals(expectedElem, actualElem);

                assertEquals(expectedElem.getChildren().size(), actualElem.getChildren().size());
                expectedElem.getChildren().forEach(c -> stackExpected.push(c));
                actualElem.getChildren().forEach(c -> stackActual.push(c));

            } else {
                assertTrue(expectedOne instanceof TextNodeSnapshot);
                assertTrue(actualOne instanceof TextNodeSnapshot);

                TextNodeSnapshot expectedTxt = (TextNodeSnapshot) expectedOne;
                TextNodeSnapshot actualTxt = (TextNodeSnapshot) actualOne;
                assertTextContentEquals(expectedTxt, actualTxt);
            }
        }

        assertTrue(stackActual.isEmpty());
    }

    public static void assertElementContentEquals(ElementSnapshotOrBuilder expectedElem,
                                                  ElementSnapshotOrBuilder actualElem) {
        assertEquals(expectedElem.getTagName(), actualElem.getTagName());
        Map<String, String> expectedAttrs = new HashMap<>();
        Map<String, String> actualAttrs = new HashMap<>();
        expectedElem.getAttributes().forEach(a -> expectedAttrs.put(a.getName(), a.getValue()));
        actualElem.getAttributes().forEach(a -> actualAttrs.put(a.getName(), a.getValue()));
        assertEquals(expectedAttrs, actualAttrs);
    }

    public static void assertTextContentEquals(TextNodeSnapshot expectedTxt, TextNodeSnapshot actualTxt) {
        assertEquals(expectedTxt.getData(), actualTxt.getData());
    }


    public static void main(String[] args) {
        System.out.println(buildTree("P(C1[a=1],C2(GC1[b=2]),C3)"));
    }
}
