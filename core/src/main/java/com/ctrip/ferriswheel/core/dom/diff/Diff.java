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

package com.ctrip.ferriswheel.core.dom.diff;

import java.util.Objects;

public class Diff {
    private final NodeLocation negativeLocation;
    private final NodeLocation positiveLocation;

    public Diff(NodeLocation negativeLocation, NodeLocation positiveLocation) {
        this.negativeLocation = negativeLocation;
        this.positiveLocation = positiveLocation;
    }

    public boolean isDelete() {
        return negativeLocation != null && positiveLocation == null;
    }

    public boolean isInsert() {
        return negativeLocation == null && positiveLocation != null;
    }

    public boolean isUpdate() {
        return negativeLocation != null && positiveLocation != null;
    }

    public boolean hasContent() {
        return false;
    }

    public void merge(Diff another) {
        if (!this.equals(another)) {
            throw new IllegalArgumentException("Merge operation only purposed for same locations.");
        }
        doMerge(another);
    }

    protected void doMerge(Diff another) {
        // override this to do merge
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Diff)) return false;
        Diff diff = (Diff) o;
        return Objects.equals(getNegativeLocation(), diff.getNegativeLocation()) &&
                Objects.equals(getPositiveLocation(), diff.getPositiveLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNegativeLocation(), getPositiveLocation());
    }

    @Override
    public String toString() {
        return negativeLocation + " -> " + positiveLocation;
    }

    public NodeLocation getNegativeLocation() {
        return negativeLocation;
    }

    public NodeLocation getPositiveLocation() {
        return positiveLocation;
    }
}
