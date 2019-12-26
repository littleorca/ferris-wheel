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

import com.ctrip.ferriswheel.core.dom.*;

import java.io.IOException;

public class Serializer {
    public static String serialize(Node node) {
        StringBuilder sb = new StringBuilder();
        try {
            serialize(sb, node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static <T extends Appendable> T serialize(T appendable, Node node) throws IOException {
        if (node instanceof Element) {
            return serialize(appendable, (Element) node);
        } else if (node instanceof Document) {
            return serialize(appendable, ((Document) node).getDocumentElement());
        } else if (node instanceof TextNode) {
            return serialize(appendable, (TextNode) node);
        } else if (node instanceof Attribute) {
            return serialize(appendable, (Attribute) node);
        } else {
            throw new IllegalArgumentException("Unsupported node implementation: " + node.getClass());
        }
    }

    public static <T extends Appendable> T serialize(T appendable, Element element) throws IOException {
        appendable.append('<').append(element.getTagName());

        for (Attribute attr : element.getAttributes()) {
            appendable.append(' ');
            serialize(appendable, attr);
        }

        if (element.getChildCount() > 0) {
            appendable.append(">");
            for (int i = 0; i < element.getChildCount(); i++) {
                Node child = element.getChild(i);
                if (child instanceof Element) {
                    serialize(appendable, (Element) child);
                } else if (child instanceof TextNode) {
                    serialize(appendable, (TextNode) child);
                } else {
                    throw new RuntimeException();
                }
            }
            appendable.append("</").append(element.getTagName()).append(">");

        } else { // self-close
            appendable.append("/>");
        }

        return appendable;
    }

    public static <T extends Appendable> T serialize(T appendable, Attribute attr) throws IOException {
        appendable.append(attr.getName()).append("=\"").append(escapeXml(attr.getValue())).append('"');
        return appendable;
    }

    public static <T extends Appendable> T serialize(T appendable, TextNode textNode) throws IOException {
        appendable.append(escapeXml(textNode.getNodeValue()));
        return appendable;
    }

    private static String escapeXml(String value) {
        return value.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;");
    }
}
