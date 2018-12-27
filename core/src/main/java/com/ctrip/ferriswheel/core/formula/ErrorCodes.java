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

package com.ctrip.ferriswheel.core.formula;

import com.ctrip.ferriswheel.api.variant.ErrorCode;

public enum ErrorCodes implements ErrorCode {
    OK("OK"),
    UNKNOWN("UNKNOWN"),
    ILLEGAL_REF("REF"),
    ILLEGAL_VALUE("VALUE"),
    DIV_0("DIV/0");

    public static ErrorCodes valueOf(int code) {
        if (code < 0 || code >= values().length) {
            throw new IndexOutOfBoundsException("Invalid code: " + code);
        }
        return values()[code];
    }

    private final String displayName;

    ErrorCodes(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public int getCode() {
        return ordinal();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFullName() {
        return String.valueOf("#" + displayName + "!");
    }
}
