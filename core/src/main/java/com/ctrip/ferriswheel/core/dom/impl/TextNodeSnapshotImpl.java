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

package com.ctrip.ferriswheel.core.dom.impl;

import com.ctrip.ferriswheel.core.dom.TextNodeSnapshot;

public class TextNodeSnapshotImpl extends AbstractNodeSnapshot implements TextNodeSnapshot {
    private final String data;

    public TextNodeSnapshotImpl(String data, TextNodeSnapshot previousSnapshot) {
        super(previousSnapshot);
        this.data = data;
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public TextNodeSnapshot getPreviousSnapshot() {
        return (TextNodeSnapshot) super.getPreviousSnapshot();
    }

    @Override
    public TextNodeSnapshotImpl duplicate(boolean linked) {
        return new TextNodeSnapshotImpl(data, linked ? this : null);
    }

    @Override
    protected String toSingleLineString() {
        return data == null ? "" : data.replaceAll("\\r", "\\\\r")
                .replaceAll("\\n", "\\\\n");
    }

    public static class Builder extends AbstractNodeSnapshot.Builder {
        private String data;

        public Builder setData(String data) {
            if (this.data != data) {
                this.data = data;
                markDirty();
            }
            return this;
        }

        public String getData() {
            return data;
        }

        public Builder setPreviousNode(TextNodeSnapshot previousNode) {
            return (Builder) super.setPreviousNode(previousNode);
        }

        public TextNodeSnapshot getPreviousNode() {
            return (TextNodeSnapshot) super.getPreviousNode();
        }

        @Override
        public TextNodeSnapshot build() {
            if (!isDirty() && getLatestBuild() != null) {
                return (TextNodeSnapshot) getLatestBuild();
            }
            TextNodeSnapshotImpl node = new TextNodeSnapshotImpl(data, getPreviousNode());
            setPreviousNode(null);
            setLatestBuild(node);
            setDirty(false);
            return node;
        }
    }
}
