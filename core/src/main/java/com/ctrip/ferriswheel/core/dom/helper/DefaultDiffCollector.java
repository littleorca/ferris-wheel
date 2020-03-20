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

import com.ctrip.ferriswheel.core.dom.diff.Diff;
import com.ctrip.ferriswheel.core.dom.diff.NodeLocation;
import com.ctrip.ferriswheel.core.dom.diff.Patch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class DefaultDiffCollector implements DiffCollector {
    private LinkedHashMap<Diff, Diff> diffIndex;
    private Set<NodeLocation> negativeStopLocations;
    private Set<NodeLocation> positiveStopLocations;

    public DefaultDiffCollector() {
        reset();
    }

    @Override
    public void add(Diff diff) {
        if (diff.isDelete() && negativeStopLocations.contains(diff.getNegativeLocation())) {
            return;
        } else if (diff.isInsert() && positiveStopLocations.contains(diff.getPositiveLocation())) {
            return;
        }

        Diff previous = diffIndex.get(diff);
        if (previous != null) {
            previous.merge(diff);
            return;
        }

        if (diff.isUpdate()) {
            diffIndex.remove(new Diff(diff.getNegativeLocation(), null));
            diffIndex.remove(new Diff(null, diff.getPositiveLocation()));
            negativeStopLocations.add(diff.getNegativeLocation());
            positiveStopLocations.add(diff.getPositiveLocation());
        }
        diffIndex.put(diff, diff);
    }

    @Override
    public Patch toPatch() {
        Patch p = new Patch();
        p.setDiffList(new ArrayList<>(diffIndex.values()));
        return p;
    }

    @Override
    public void clear() {
        reset();
    }

    private void reset() {
        this.diffIndex = new LinkedHashMap<>();
        this.negativeStopLocations = new HashSet<>();
        this.positiveStopLocations = new HashSet<>();
    }

}
