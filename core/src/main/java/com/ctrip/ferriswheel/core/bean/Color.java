package com.ctrip.ferriswheel.core.bean;

import java.io.Serializable;
import java.util.regex.Pattern;

public class Color implements Serializable {
    private static final String CSS_COLOR_REGEX = "^#[0-9a-fA-F]{3}(?:[0-9a-fA-F]{3})?$";

    private float red = 0;
    private float green = 0;
    private float blue = 0;
    private float alpha = 1;

    public Color() {
    }

    public Color(String cssColor) {
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
    public Color(Color another) {
        this(another.red, another.green, another.blue, another.alpha);
    }

    public Color(float red, float green, float blue, float alpha) {
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
