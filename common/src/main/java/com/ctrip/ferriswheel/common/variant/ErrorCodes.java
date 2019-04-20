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

package com.ctrip.ferriswheel.common.variant;

public enum ErrorCodes implements ErrorCode {
    OK("OK"),
    NULL("#NULL!")                /* = 1 */,
    DIV("#DIV/0!")                /* = 2 */,
    VALUE("#VALUE!")              /* = 3 */,
    REF("#REF!")                  /* = 4 */,
    NAME("#NAME?")                /* = 5 */,
    NUM("#NUM!")                  /* = 6 */,
    NA("#N/A")                    /* = 7 */,
    GETTING_DATA("#GETTING_DATA") /* = 8 */;

    public static ErrorCodes valueOf(int code) {
        if (code < 0 || code >= values().length) {
            throw new IndexOutOfBoundsException("Invalid code: " + code);
        }
        return values()[code];
    }

    private final String name;

    ErrorCodes(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int getCode() {
        return ordinal();
    }

    public String getName() {
        return name;
    }
}
