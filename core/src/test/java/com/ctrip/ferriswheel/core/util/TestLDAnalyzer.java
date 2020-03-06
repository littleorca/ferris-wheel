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

package com.ctrip.ferriswheel.core.util;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TestLDAnalyzer extends TestCase {

    public void testAnalyze() {
        // empty to empty
        checkAnalyze(Arrays.asList(), Arrays.asList(),
                Collections.emptyList(), Collections.emptyList());
        // single item to empty
        checkAnalyze(Arrays.asList("a"), Arrays.asList(),
                Arrays.asList(0), Collections.emptyList());
        // items to empty
        checkAnalyze(Arrays.asList("a", "b"), Arrays.asList(),
                Arrays.asList(1, 0), Collections.emptyList());
        // empty to single item
        checkAnalyze(Arrays.asList(), Arrays.asList("a"),
                Collections.emptyList(), Arrays.asList(0));
        // empty to items
        checkAnalyze(Arrays.asList(), Arrays.asList("a", "b"),
                Collections.emptyList(), Arrays.asList(1, 0));
        // single to single, unchanged
        checkAnalyze(Arrays.asList("a"), Arrays.asList("a"),
                Collections.emptyList(), Collections.emptyList());
        // single to single, different
        checkAnalyze(Arrays.asList("a"), Arrays.asList("b"),
                Arrays.asList(0), Arrays.asList(0));
        // unchanged items
        checkAnalyze(Arrays.asList("a", "b"), Arrays.asList("a", "b"),
                Collections.emptyList(), Collections.emptyList());

        // add first
        checkAnalyze(Arrays.asList("a", "b"), Arrays.asList("a0", "a", "b"),
                Arrays.asList(), Arrays.asList(0));
        checkAnalyze(Arrays.asList("a", "b"), Arrays.asList("a0", "a1", "a", "b"),
                Arrays.asList(), Arrays.asList(1, 0));

        // insert
        checkAnalyze(Arrays.asList("a", "b"), Arrays.asList("a", "a0", "b"),
                Arrays.asList(), Arrays.asList(1));
        checkAnalyze(Arrays.asList("a", "b"), Arrays.asList("a", "a0", "a1", "b"),
                Arrays.asList(), Arrays.asList(2, 1));

        // append
        checkAnalyze(Arrays.asList("a", "b"), Arrays.asList("a", "b", "c"),
                Arrays.asList(), Arrays.asList(2));
        checkAnalyze(Arrays.asList("a", "b"), Arrays.asList("a", "b", "c", "d"),
                Arrays.asList(), Arrays.asList(3, 2));

        // remove first
        checkAnalyze(Arrays.asList("a", "b", "c", "d"), Arrays.asList("b", "c", "d"),
                Arrays.asList(0), Arrays.asList());
        checkAnalyze(Arrays.asList("a", "b", "c", "d"), Arrays.asList("c", "d"),
                Arrays.asList(1, 0), Arrays.asList());

        // remove middle
        checkAnalyze(Arrays.asList("a", "b", "c", "d"), Arrays.asList("a", "c", "d"),
                Arrays.asList(1), Arrays.asList());
        checkAnalyze(Arrays.asList("a", "b", "c", "d"), Arrays.asList("a", "d"),
                Arrays.asList(2, 1), Arrays.asList());

        // remove last
        checkAnalyze(Arrays.asList("a", "b", "c", "d"), Arrays.asList("a", "b", "c"),
                Arrays.asList(3), Arrays.asList());
        checkAnalyze(Arrays.asList("a", "b", "c", "d"), Arrays.asList("a", "b"),
                Arrays.asList(3, 2), Arrays.asList());

        // replace
        checkAnalyze(Arrays.asList("a", "b", "c", "d"), Arrays.asList("a", "b1", "c", "d"),
                Arrays.asList(1), Arrays.asList(1));
        checkAnalyze(Arrays.asList("a", "b", "c", "d"), Arrays.asList("a", "b1", "c1", "d"),
                Arrays.asList(2, 1), Arrays.asList(2, 1));

        // complex
        checkAnalyze(Arrays.asList("a", "b", "c", "d"), Arrays.asList("d", "a", "c", "e"),
                Arrays.asList(3, 1), Arrays.asList(3, 0));
        checkAnalyze(Arrays.asList("d", "a", "c", "e"), Arrays.asList("a", "b", "c", "d"),
                Arrays.asList(3, 0), Arrays.asList(3, 1));
    }

    private void checkAnalyze(List<String> negativeList,
                              List<String> positiveList,
                              List<Integer> expectedNegativeIndices,
                              List<Integer> expectedPositiveIndices) {

        List<Integer> negativeItems = new LinkedList<>();
        List<Integer> positiveItems = new LinkedList<>();

        LDAnalyzer.analyze(negativeList,
                positiveList,
                String::compareTo,
                i -> negativeItems.add(i),
                j -> positiveItems.add(j));

        assertEquals(expectedNegativeIndices, negativeItems);
        assertEquals(expectedPositiveIndices, positiveItems);
    }
}
