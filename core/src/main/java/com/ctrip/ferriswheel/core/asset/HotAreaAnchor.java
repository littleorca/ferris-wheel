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

package com.ctrip.ferriswheel.core.asset;

class HotAreaAnchor {
    private EndpointAnchor start;
    private EndpointAnchor end;

    boolean isValid() {
        return start != null && start.isValid() &&
                end != null && end.isValid();
    }

    public EndpointAnchor getStart() {
        return start;
    }

    public void setStart(EndpointAnchor start) {
        this.start = start;
    }

    public EndpointAnchor getEnd() {
        return end;
    }

    public void setEnd(EndpointAnchor end) {
        this.end = end;
    }


    static abstract class EndpointAnchor {
        abstract boolean isValid();
    }

    static class CellAnchor extends EndpointAnchor {
        private long cellId;

        @Override
        boolean isValid() {
            return cellId != Asset.UNSPECIFIED_ASSET_ID;
        }

        CellAnchor(long cellId) {
            this.cellId = cellId;
        }

        public long getCellId() {
            return cellId;
        }

        public void setCellId(long cellId) {
            this.cellId = cellId;
        }
    }
}
