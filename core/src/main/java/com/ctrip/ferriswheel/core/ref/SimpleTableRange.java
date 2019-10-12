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

package com.ctrip.ferriswheel.core.ref;

import java.util.Objects;

public class SimpleTableRange implements TableRange {
    private Integer left;
    private Integer top;
    private Integer right;
    private Integer bottom;

    public SimpleTableRange(TableRange tableRange) {
        this(tableRange.getLeft(),
                tableRange.getTop(),
                tableRange.getRight(),
                tableRange.getBottom());
    }

    public SimpleTableRange(Integer left, Integer top, Integer right, Integer bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public SimpleTableRange intersection(TableRange range) {
        if (range == null || !isOverlap(range.getLeft(), range.getTop(), range.getRight(), range.getBottom())) {
            return null;
        }

        return new SimpleTableRange(
                maxIgnoreNull(left, range.getLeft()),
                maxIgnoreNull(top, range.getTop()),
                minIgnoreNull(right, range.getRight()),
                minIgnoreNull(bottom, range.getBottom())
        );
    }

    private Integer maxIgnoreNull(Integer a, Integer b) {
        if (a != null && b != null) {
            return Math.max(a, b);
        } else {
            return a == null ? b : a;
        }
    }

    private Integer minIgnoreNull(Integer a, Integer b) {
        if (a != null && b != null) {
            return Math.min(a, b);
        } else {
            return a == null ? b : a;
        }
    }

    public boolean isOverlap(Integer left, Integer top, Integer right, Integer bottom) {
        return isRowOverlap(top, bottom) && isColumnOverlap(left, right);
    }

    public boolean isRowOverlap(Integer top, Integer bottom) {
        return (top == null || this.getBottom() == null || top <= this.getBottom()) &&
                (bottom == null || this.getTop() == null || bottom >= this.getTop());
    }

    public boolean isColumnOverlap(Integer left, Integer right) {
        return (left == null || this.getRight() == null || left <= this.getRight()) &&
                (right == null || this.getLeft() == null || right >= this.getLeft());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTableRange that = (SimpleTableRange) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(top, that.top) &&
                Objects.equals(right, that.right) &&
                Objects.equals(bottom, that.bottom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, top, right, bottom);
    }

    @Override
    public String toString() {
        return String.format("{l:%d,t:%d,r:%d,b:%d}", left, top, right, bottom);
    }

    @Override
    public Integer getLeft() {
        return left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

    @Override
    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    @Override
    public Integer getRight() {
        return right;
    }

    public void setRight(Integer right) {
        this.right = right;
    }

    @Override
    public Integer getBottom() {
        return bottom;
    }

    public void setBottom(Integer bottom) {
        this.bottom = bottom;
    }
}
