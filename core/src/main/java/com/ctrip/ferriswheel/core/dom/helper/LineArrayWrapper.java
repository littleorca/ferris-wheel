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

import com.ctrip.ferriswheel.core.util.Sequence;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class LineArrayWrapper implements Sequence<LineArrayWrapper.LineWrapper> {
    private final List<LineWrapper> lines;

    public LineArrayWrapper(String originText) {
        this.lines = scanLines(originText);
    }

    private List<LineWrapper> scanLines(String originText) {
        if (originText == null) {
            return Collections.emptyList();
        }
        List<LineWrapper> list = new LinkedList<>();
        int offset = 0;
        int pos = 0;
        while (pos < originText.length()) {
            if (originText.charAt(pos++) == '\n') {
                list.add(new LineWrapper(originText, offset, pos));
                offset = pos;
            }
        }
        if (offset < pos) {
            list.add(new LineWrapper(originText, offset, pos));
        }
        return list;
    }

    @Override
    public int size() {
        return lines.size();
    }

    @Override
    public LineWrapper get(int index) {
        return lines.get(index);
    }

    public final class LineWrapper {
        private final String originText;
        private final int start;
        private final int end;
        private transient int hash = 0;

        LineWrapper(String originText, int start, int end) {
            this.originText = originText;
            this.start = start;
            this.end = end;
        }

        @Override
        public int hashCode() {
            if (hash == 0 && end > start) {
                int h = 0;
                for (int i = start; i < end; i++) {
                    h = h * 31 + originText.charAt(i);
                }
                hash = h;
            }
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LineWrapper)) {
                return false;
            }
            LineWrapper another = (LineWrapper) obj;
            if (end - start != another.end - another.start) {
                return false;
            }
            for (int i = start, j = another.start; i < end; i++, j++) {
                if (originText.charAt(i) != another.originText.charAt(j)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return originText.substring(start, end);
        }

        public String getOriginText() {
            return originText;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }
}
