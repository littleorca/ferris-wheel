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

import com.ctrip.ferriswheel.core.dom.NodeSnapshot;

import java.lang.ref.WeakReference;

public abstract class AbstractNodeSnapshot implements NodeSnapshot {
    private final WeakReference<NodeSnapshot> previous;

    AbstractNodeSnapshot(NodeSnapshot previous) {
        this.previous = new WeakReference<>(previous);
    }

    @Override
    public NodeSnapshot getPreviousSnapshot() {
        return previous.get();
    }

    @Override
    public NodeSnapshot getOriginalSnapshot() {
        NodeSnapshot originalSnapshot = this;
        NodeSnapshot temp;
        while ((temp = originalSnapshot.getPreviousSnapshot()) != null) {
            originalSnapshot = temp;
        }
        return originalSnapshot;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildStringDescriptor(sb, "");
        return sb.toString();
    }

    protected void buildStringDescriptor(StringBuilder sb, String linePrefix) {
        sb.append(linePrefix).append(toSingleLineString()).append("\n");
    }

    protected String toSingleLineString() {
        return String.valueOf(getNodeType());
    }
}
