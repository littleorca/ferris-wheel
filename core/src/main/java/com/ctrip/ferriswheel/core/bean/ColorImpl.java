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

package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.api.view.Color;

import java.io.Serializable;
import java.util.regex.Pattern;

public class ColorImpl implements Color, Serializable {
    private static final String CSS_COLOR_REGEX = "^#[0-9a-fA-F]{3}(?:[0-9a-fA-F]{3})?$";

    private float red = 0;
    private float green = 0;
    private float blue = 0;
    private float alpha = 1;

    public ColorImpl() {
    }

    public ColorImpl(String cssColor) {
        if (cssColor == null || !Pattern.compile(CSS_COLOR_REGEX).matcher(cssColor).matches()) {
            throw new IllegalArgumentException("Invalid CSS color string: " + cssColor);
        }
        float[] values = new float[3];
        int i = 1, j = 0;
        while (i < cssColor.length()) {
            int rawValue;
            if (cssColor.length() == 4) {
                rawValue = Integer.parseInt(cssColor.substring(i, i + 1), 16);
                rawValue = rawValue * 16 + rawValue;
                i += 1;
            } else {
                rawValue = Integer.parseInt(cssColor.substring(i, i + 2), 16);
                i += 2;
            }
            values[j++] = (float) (rawValue / 255.0);
        }
        red = values[0];
        green = values[1];
        blue = values[2];
        // alpha = 1; // which is already the default value.
    }

    /**
     * Construct a Color by copying another Color.
     *
     * @param another
     */
    public ColorImpl(Color another) {
        this(another.getRed(), another.getGreen(), another.getBlue(), another.getAlpha());
    }

    public ColorImpl(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
