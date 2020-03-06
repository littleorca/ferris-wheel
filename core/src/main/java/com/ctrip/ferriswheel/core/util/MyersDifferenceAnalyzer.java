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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * An implementation of Myers's difference algorithm.
 * Refer to the paper "An O(ND) Difference Algorithm and Its Variations" for
 * details about this algorithm.
 */
public class MyersDifferenceAnalyzer implements SequenceDifferenceAnalyzer {

    @Override
    public <T> int analyze(List<T> negativeSequence,
                           List<T> positiveSequence,
                           Comparator<T> comparator,
                           Consumer<Integer> editScriptConsumer) {
        if (comparator == null) {
            comparator = (a, b) -> a.equals(b) ? 0 : -1;
        }
        if (editScriptConsumer == null) {
            editScriptConsumer = i -> {
                // black hole
            };
        }
        return shortestEditScript(negativeSequence,
                positiveSequence,
                0, 0,
                negativeSequence.size(),
                positiveSequence.size(),
                comparator,
                editScriptConsumer);
    }

    private <T> int shortestEditScript(List<T> negativeSequence,
                                       List<T> positiveSequence,
                                       int offX, int offY,
                                       int n, int m,
                                       Comparator<T> comparator,
                                       Consumer<Integer> editScriptConsumer) {
        if (n < 0 || m < 0) {
            return 0; // TODO review
        }

        int delta = n - m;
        boolean odd = (delta & 0x1) != 0;
        int d;
        int max = (m + n + 1) / 2;
        int[] vf = new int[2 * max + 1];
        int[] vb = new int[2 * max + 1];
        vf[max + 1] = 0;
        vb[max - 1] = n;

        int totalDistance = -1;
        int x = 0, y = 0, u = 0, v = 0, act = 0;

        // find the middle snake

        for (d = 0; d <= max; d++) {

            // forward
            for (int k = -d; k <= d; k += 2) {
                act = 0;

                // find the end of the furthest reaching forward
                // D-path in diagonal k
                if (k == -d || (k != d && vf[max + k - 1] < vf[max + k + 1])) {
                    x = u = vf[max + k + 1];
                    y = x - (k + 1);
                    v = x - k;
                    if (v > 0) {
                        act = v; // insert
                    }
                } else {
                    x = vf[max + k - 1];
                    u = x + 1;
                    y = v = x - (k - 1);
                    if (u > 0) {
                        act = -u; // delete
                    }
                }
//                y = x - k;

                while (u < n && v < m &&
                        comparator.compare(
                                negativeSequence.get(offX + u),
                                positiveSequence.get(offX + v)
                        ) == 0) {
                    u++;
                    v++;
                }

                vf[max + k] = u;

                if (odd && k >= delta - (d - 1) && k <= delta + (d - 1)) {
                    // check if overlaps
                    if (u == vb[max + k - delta]) {
                        totalDistance = 2 * d - 1;
                        break;
                    }
                }
            }

            if (totalDistance > 0) {
                break;
            }

            // backward
            for (int k = d; k >= -d; k -= 2) {
                act = 0;

                // find the end of the furthest reaching reverse
                // D-path in diagonal k+delta
                if (k == d || (k != -d && vb[max + k + 1] < vb[max + k - 1])) {
                    u = x = vb[max + k - 1];
                    v = u - (k - 1 + delta);
                    y = u - (k + delta);
                    if (v > 0) {
                        act = v; // insert
                    }

                } else {
                    u = vb[max + k + 1];
                    x = u - 1;
                    v = y = x - (k + 1 + delta);
                    if (u > 0) {
                        act = -u; // delete
                    }
                }

                while (x > 0 && y > 0 &&
                        comparator.compare(
                                negativeSequence.get(offX + x - 1),
                                positiveSequence.get(offY + y - 1)
                        ) == 0) {
                    x--;
                    y--;
                }

                vb[max + k] = x;

                if (!odd && k + delta >= -d && k + delta <= d) {
                    // check if overlaps
                    if (x == vf[max+k + delta]) {
                        totalDistance = 2 * d;
                        break;
                    }
                }
            }

            if (totalDistance >= 0) {
                break;
            }
        }

        if (totalDistance < 0) {
            throw new RuntimeException();
        }

        // divide and conquer

        if (d > 1) {
            shortestEditScript(negativeSequence,
                    positiveSequence,
                    offX, offY, x - offX, y - offY,
                    comparator,
                    editScriptConsumer);
            if (act != 0) { // always be true?
                editScriptConsumer.accept(act);
            }
            shortestEditScript(negativeSequence,
                    positiveSequence,
                    u, v,
                    n - (u - offX), m - (v - offY),
                    comparator,
                    editScriptConsumer);

        } else if (m > n) {
            if (act != 0) {
                editScriptConsumer.accept(act);
            }
        } else {
            if (act != 0) {
                editScriptConsumer.accept(act);
            }
        }

        return totalDistance;
    }

    public static void main(String[] args) {
        MyersDifferenceAnalyzer analyzer = new MyersDifferenceAnalyzer();
        int d = analyzer.analyze(Arrays.asList("a","b"),
                Arrays.asList(),
                null, System.out::println);
        System.out.println("distance=" + d);
    }
}
