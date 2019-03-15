/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
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

package com.ctrip.ferriswheel.core.util;

import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class VariantFormatter {
//    private static final String DECIMAL_FMT_PATTERN = "^(#,##)?0(\\.0+)?(%)?$";
//    private static final String DATE_FMT_PATTERN = "";
//    private static final String BLANK = "";

    public static String format(Variant value, String format) {
        if (VariantType.DECIMAL.equals(value.valueType())) {
            try {
                return new DecimalFormat(format).format(value.decimalValue());
            } catch (NullPointerException e) {
                return DecimalFormat.getInstance().format(value.decimalValue());
            } catch (IllegalArgumentException e) {
                return DecimalFormat.getInstance().format(value.decimalValue());
            }
        } else if (VariantType.DATE.equals(value.valueType())) {
            Instant instant = value.dateValue().toInstant();
            ZonedDateTime dateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            try {
                return DateTimeFormatter.ofPattern(format).format(dateTime);
            } catch (IllegalArgumentException e) {
                return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
            }
        } else {
            return value.toString();
        }
    }
}
