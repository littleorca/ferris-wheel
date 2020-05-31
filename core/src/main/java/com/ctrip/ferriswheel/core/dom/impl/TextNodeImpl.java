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

package com.ctrip.ferriswheel.core.dom.impl;

import com.ctrip.ferriswheel.core.dom.TextNode;

import java.util.Objects;

public final class TextNodeImpl extends AbstractNode implements TextNode {
    public static final String DEFAULT_TEXT_CONTENT = "";

    private String data = DEFAULT_TEXT_CONTENT;

    public TextNodeImpl(AbstractDocument ownerDocument, String data) {
        super(ownerDocument);
        setData(data);
    }

    @Override
    public void setTextContent(String textContent) {
        setData(textContent);
    }

    @Override
    public void setNodeValue(String nodeValue) {
        setData(nodeValue);
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        if (data == null) {
            data = DEFAULT_TEXT_CONTENT;
        }
        if (!Objects.equals(this.data, data)) {
            setDirty(true);
            this.data = data;
        }
    }

//    // FIXME @Override
//    TextNodeSnapshot snapshot() {
//        return new TextNodeSnapshotImpl(getData());
//    }
}
