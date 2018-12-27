/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
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
 *
 */

package com.ctrip.ferriswheel.api.variant;

public enum VariantType {
    /**
     * Do NOT change their order!
     * Make sure item's ordinal is reliable.
     */

    ERROR,   // 0
    BLANK,   // 1
    DECIMAL, // 2
    BOOL,    // 3
    DATE,    // 4
    STRING,  // 5
    LIST;    // 6

    /*
     * Define type transition table.
     * TYPE_ERROR not included, leave that to error handlers.
     */
    private static final int[][] MATH_TRANSITION = new int[][]{
            /* ------------- ER,BL,DE,BO,DA,ST,LI */
            /* 0. ERROR   */ {0, 0, 0, 0, 0, 0, 0},
            /* 1. BLANK   */ {0, 1, 2, 3, 4, 5, 6},
            /* 2. DECIMAL */ {0, 2, 2, 2, 4, 5, 6},
            /* 3. BOOLEAN */ {0, 3, 2, 3, 4, 5, 6},
            /* 4. DATE    */ {0, 4, 4, 4, 2, 5, 6},
            /* 5. STRING  */ {0, 5, 5, 5, 5, 5, 6},
            /* 6. LIST    */ {0, 6, 6, 6, 6, 6, 6}
    };

    public static VariantType valueOf(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            throw new ArrayIndexOutOfBoundsException("Invalid ordinal.");
        }
        return values()[ordinal];
    }

    public static VariantType compatible(VariantType type1, VariantType type2) {
        return valueOf(MATH_TRANSITION[type1.ordinal()][type2.ordinal()]);
    }
}
