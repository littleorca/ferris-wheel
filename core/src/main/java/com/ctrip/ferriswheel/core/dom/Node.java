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

package com.ctrip.ferriswheel.core.dom;

public interface Node {

    Document getOwnerDocument();

    Node getRootNode();

    Node getParentNode();

    Node previousSibling();

    Node nextSibling();

    String getNodeName();

    String getTextContent();

    void setTextContent(String textContent);

    String getNodeValue();

    void setNodeValue(String nodeValue);

    boolean hasChildNodes();

    boolean contains(Node otherNode);

    int getChildCount();

    Node getChild(int index);

    Node getChild(String name);

    Node firstChild();

    Node lastChild();

    void insertChild(Node child, Node ref);

    void appendChild(Node child);

    boolean removeChild(Node child);

    Node replaceChild(Node newChild, Node oldChild);

//    Node cloneNode();

}