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
    private int left;
    private int top;
    private int right;
    private int bottom;

    public SimpleTableRange() {
    }

    public SimpleTableRange(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public boolean isOverlap(Integer left, Integer top, Integer right, Integer bottom) {
        return isRowOverlap(top, bottom) && isColumnOverlap(left, right);
    }

    public boolean isRowOverlap(Integer top, Integer bottom) {
        return (top == null || top <= this.getBottom()) && (bottom == null || bottom >= this.getTop());
    }

    public boolean isColumnOverlap(Integer left, Integer right) {
        return (left == null || left <= this.getRight()) && (right == null || right >= this.getLeft());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTableRange range = (SimpleTableRange) o;
        return left == range.left &&
                top == range.top &&
                right == range.right &&
                bottom == range.bottom;
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, top, right, bottom);
    }

    @Override
    public int getLeft() {
        return left;
    }

    @Override
    public int getTop() {
        return top;
    }

    @Override
    public int getRight() {
        return right;
    }

    @Override
    public int getBottom() {
        return bottom;
    }

    @Override
    public int width() {
        return right - left + 1;
    }

    @Override
    public int height() {
        return bottom - top + 1;
    }
}
