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

public enum NodeType {
    ELEMENT_NODE,                   //  1   An Element node like <p> or <div>.
    ATTRIBUTE_NODE,                 //  2	An Attribute of an Element.
    TEXT_NODE,                      //  3   The actual Text inside an Element or Attr.
    CDATA_SECTION_NODE,             //  4   A CDATASection, such as <!CDATA[[ … ]]>.
    PROCESSING_INSTRUCTION_NODE,    //  7   A ProcessingInstruction of an XML document, such as <?xml-stylesheet … ?>.
    COMMENT_NODE,                   //  8   A Comment node, such as <!-- … -->.
    DOCUMENT_NODE,                  //  9   A Document node.
    DOCUMENT_TYPE_NODE,             // 10   A DocumentType node, such as <!DOCTYPE html>.
    DOCUMENT_FRAGMENT_NODE,         // 11   A DocumentFragment node.
}
