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

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class LDAnalyzer {

    static <T> void analyze(List<T> negativeList,
                            List<T> positiveList,
                            Comparator<T> comparator,
                            Consumer<Integer> negativeCollector,
                            Consumer<Integer> positiveCollector) {
        int[][] matrix = new int[negativeList.size() + 1][positiveList.size() + 1];

        for (int i = 0; i <= negativeList.size(); i++) {
            for (int j = 0; j <= positiveList.size(); j++) {
                if (i == 0) {
                    matrix[i][j] = j;
                } else if (j == 0) {
                    matrix[i][j] = i;
                } else {
                    int min = Math.min(matrix[i - 1][j], matrix[i][j - 1]);
                    min = Math.min(min, matrix[i - 1][j - 1]);
                    T negativeItem = negativeList.get(i - 1);
                    T positiveItem = positiveList.get(j - 1);
                    matrix[i][j] = comparator.compare(negativeItem, positiveItem) == 0 ? min : min + 1;
                }
            }
        }

        int i = negativeList.size();
        int j = positiveList.size();
        int current = matrix[i][j];

        while (i > 0 && j > 0) {
            int corner = matrix[i - 1][j - 1];
            int upper = matrix[i - 1][j];
            int left = matrix[i][j - 1];

            if (corner <= upper && corner <= left) {
                i--;
                j--;

                if (current != corner) {
                    positiveCollector.accept(j);
                    negativeCollector.accept(i);
                    current = corner;
                }

            } else if (upper <= left) {
                i--;
                if (current != upper) {
                    negativeCollector.accept(i);
                    current = upper;
                }
            } else {
                j--;
                if (current != left) {
                    positiveCollector.accept(j);
                    current = left;
                }
            }
        }
        while (i > 0) {
            i--;
            negativeCollector.accept(i);
        }
        while (j > 0) {
            j--;
            positiveCollector.accept(j);
        }
    }

}
