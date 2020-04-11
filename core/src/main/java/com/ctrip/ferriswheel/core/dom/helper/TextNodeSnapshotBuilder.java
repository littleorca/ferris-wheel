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

package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.core.dom.NodeSnapshot;
import com.ctrip.ferriswheel.core.dom.TextNodeSnapshot;

public class TextNodeSnapshotBuilder extends AbstractNodeSnapshotBuilder
        implements TextNodeSnapshot {
    private String data;

    public TextNodeSnapshotBuilder() {
    }

    /**
     * Create builder based on the specified origin node snapshot. The previous
     * node will be set to the specified origin node.
     *
     * @param origin
     */
    public TextNodeSnapshotBuilder(TextNodeSnapshot origin) {
        if (origin instanceof TextNodeSnapshotBuilder) {
            throw new IllegalArgumentException();
        }
        this.data = origin.getData();
        setPreviousSnapshot(origin);
    }

    @Override
    public String getData() {
        return data;
    }

    public TextNodeSnapshotBuilder setData(String data) {
        this.data = data;
        return this;
    }

    @Override
    public TextNodeSnapshot getPreviousSnapshot() {
        return (TextNodeSnapshot) super.getPreviousSnapshot();
    }

    @Override
    public TextNodeSnapshotBuilder setPreviousSnapshot(NodeSnapshot previousSnapshot) {
        return (TextNodeSnapshotBuilder) super.setPreviousSnapshot(previousSnapshot);
    }

    @Override
    public TextNodeSnapshot getOriginalSnapshot() {
        return (TextNodeSnapshot) super.getOriginalSnapshot();
    }

    @Override
    public TextNodeSnapshot duplicate(boolean linked) {
        return null; // FIXME
    }
}
