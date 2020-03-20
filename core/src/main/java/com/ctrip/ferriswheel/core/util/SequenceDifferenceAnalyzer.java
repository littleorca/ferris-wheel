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
import java.util.function.Consumer;

public interface SequenceDifferenceAnalyzer {

    /**
     * <p>
     * Analyze the optimal differences from negative sequence to positive sequence.
     * The edit operations are limited to deletion on the negative sequence and
     * insertion on the positive sequence. A number indicates the shortest edit
     * distance will be returned, and the edit script will be emitted to the
     * specified consumer if it is not null.
     * </p>
     * <p>
     * This method emits edit script by the following rules:
     * </p>
     * <ul>
     *    <li>Each action in the edit script is represented by an integer number;</li>
     *    <li>A negative value interprets a deletion on negative sequence;</li>
     *    <li>A positive value interprets a insertion on positive sequence;</li>
     *    <li>The absolute value indicates the position of the correspond sequence;</li>
     *    <li>Zero is excluded and position index starts from 1.</li>
     * </ul>
     *
     * @param negativeSequence   The negative sequence.
     * @param positiveSequence   The positive sequence.
     * @param comparator         An optional comparator that determines if two items
     *                           are identical or not, the {@link #equals} method of
     *                           the sequence item will be used instead if this
     *                           parameter is left as null.
     * @param editScriptConsumer An optional consumer used to collect edit script.
     * @param <T>
     * @return The shortest edit distance.
     */
    <T> int analyze(Sequence<T> negativeSequence,
                    Sequence<T> positiveSequence,
                    Comparator<T> comparator,
                    Consumer<Integer> editScriptConsumer);

}
