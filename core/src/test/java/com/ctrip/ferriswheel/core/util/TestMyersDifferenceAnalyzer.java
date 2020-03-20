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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TestMyersDifferenceAnalyzer extends TestCase {
    public void testAnalyze() {
        check("", "");
        check("a", "a");
        check("ab", "ab");
        check("abc", "abc");
        check("abcd", "abcd");
        check("abcde", "abcde");
        check("abcdef", "abcdef");

        check("a", "", -1);
        check("ab", "", -1, -2);
        check("abc", "", -1, -2, -3);
        check("abcd", "", -1, -2, -3, -4);
        check("abcde", "", -1, -2, -3, -4, -5);
        check("abcdef", "", -1, -2, -3, -4, -5, -6);

        check("", "a", 1);
        check("", "ab", 1, 2);
        check("", "abc", 1, 2, 3);
        check("", "abcd", 1, 2, 3, 4);
        check("", "abcde", 1, 2, 3, 4, 5);
        check("", "abcdef", 1, 2, 3, 4, 5, 6);

        check("a", "g", -1, +1);
        check("ab", "gh", -1, -2, 1, 2);
        check("abc", "ghi", -1, -2, -3, 1, 2, 3);
        check("abcd", "ghij", -1, -2, -3, -4, 1, 2, 3, 4);
        check("abcdf", "ghijk", -1, -2, -3, -4, -5, 1, 2, 3, 4, 5);

        check("abcdef", "gabcdef", 1);
        check("abcdef", "abcdefg", 7);
        check("abcdef", "abgcdef", 3);
        check("abcdef", "abghcdef", 3, 4);
        check("abcdef", "abghcdefi", 3, 4, 9);

        check("abcdefghi", "bcdefghi", -1);
        check("abcdefghi", "abcdefgh", -9);
        check("abcdefghi", "abcefghi", -4);
        check("abcdefghi", "abcfghi", -4, -5);
        check("abcdefghi", "abcghi", -4, -5, -6);

        check("abcdef", "gbcdef", -1, 1);
        check("abcdef", "abcdeg", -6, 6);
        check("abcdef", "abcgef", -4, 4);
        check("abcdef", "ghcdei", -1, -2, 1, 2, -6, 6);
    }

    private void check(String negative, String positive, int... ops) {
        List<String> negativeSequence = new ArrayList<>(negative.length());
        List<String> positiveSequence = new ArrayList<>(positive.length());
        List<Integer> collector = new LinkedList<>();

        for (char ch : negative.toCharArray()) {
            negativeSequence.add(String.valueOf(ch));
        }
        for (char ch : positive.toCharArray()) {
            positiveSequence.add(String.valueOf(ch));
        }

        if (ops == null) {
            ops = new int[0];
        }

        int d = new MyersDifferenceAnalyzer().analyze(
                new ListSequenceWrapper<>(negativeSequence),
                new ListSequenceWrapper<>(positiveSequence),
                null, i -> collector.add(i));

        assertEquals(ops.length, d);
        for (int i = 0; i < ops.length; i++) {
            assertEquals(Integer.valueOf(ops[i]), collector.get(i));
        }
    }

    /**
     * <p>
     * This is a random test for running manually. It randomly generates
     * sequences and use myers linear space algorithm to analyze them, then the
     * edit script will be checked by applying it to the origin negative
     * (positive) sequence and compare the generated positive(negative)
     * sequence with the origin positive(negative) sequence.
     * </p>
     * <p>
     * The case design is not elaborate as the possibility is not optimized
     * well for coverage.
     * </p>
     *
     * @param args
     */
    public static void main(String[] args) {
        final int maxRound = 1000;

        MyersDifferenceAnalyzer analyzer = new MyersDifferenceAnalyzer();

        for (int i = 0; i < maxRound; i++) {
            System.out.println("# Round " + (i + 1) + "/" + maxRound);

            final List<String> negative = new ArrayList<>();
            final List<String> positive = new ArrayList<>();
            final List<Integer> ops = new ArrayList<>();

            initRandomSequences(negative, positive);
            int d = analyzer.analyze(new ListSequenceWrapper<>(negative),
                    new ListSequenceWrapper<>(positive),
                    null, op -> ops.add(op));
            System.out.println(String.format("\td=%d\t-%d/+%d\t%f",
                    d, negative.size(), positive.size(),
                    d * 1.0 / (negative.size() + positive.size())));

            if (d == 0) {
                if (!ops.isEmpty()) {
                    throw new RuntimeException();
                }
                if (!negative.equals(positive)) {
                    throw new RuntimeException();
                }
            } else {
                final int[] indices = {0, 0};
                final List<String> patchedPositive = new ArrayList<>();
                final List<String> patchedNegative = new ArrayList<>();
                for (int op : ops) {
                    if (op > 0) {
                        for (; indices[1] < op - 1; indices[0]++, indices[1]++) {
                            patchedPositive.add(negative.get(indices[0]));
                            patchedNegative.add(positive.get(indices[1]));
                        }
                        patchedPositive.add(positive.get(indices[1])); // insert
                        indices[1] = op;
                    } else {
                        op = -op;
                        for (; indices[0] < op - 1; indices[0]++, indices[1]++) {
                            patchedPositive.add(negative.get(indices[0]));
                            patchedNegative.add(positive.get(indices[1]));
                        }
                        patchedNegative.add(negative.get(indices[0]));
                        indices[0] = op;
                    }
                }
                if (negative.size() - indices[0] != positive.size() - indices[1]) {
                    throw new RuntimeException();
                }
                for (; indices[0] < negative.size(); indices[0]++, indices[1]++) {
                    patchedPositive.add(negative.get(indices[0]));
                    patchedNegative.add(positive.get(indices[1]));
                }
                if (!patchedPositive.equals(positive)) {
                    throw new RuntimeException();
                }
                if (!patchedNegative.equals(negative)) {
                    throw new RuntimeException();
                }
            }
        }
    }

    private static void initRandomSequences(List<String> negative, List<String> positive) {
        final int maxTokens = 256;

        Random random = new Random();
        String[] tokens = new String[maxTokens];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = generateRandomToken(8);
        }
        for (int i = Math.abs(random.nextInt(tokens.length) * 2); i >= 0; i--) {
            String token = tokens[Math.abs(random.nextInt(tokens.length))];
            for (char ch : token.toCharArray()) {
                negative.add(String.valueOf(ch));
            }
        }
        for (int i = Math.abs(random.nextInt(tokens.length)); i >= 0; i--) {
            String token = tokens[Math.abs(random.nextInt(tokens.length))];
            for (char ch : token.toCharArray()) {
                positive.add(String.valueOf(ch));
            }
        }
    }

    private static String generateRandomToken(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('a' + Math.abs(random.nextInt(26)));
        }
        return sb.toString();
    }
}
