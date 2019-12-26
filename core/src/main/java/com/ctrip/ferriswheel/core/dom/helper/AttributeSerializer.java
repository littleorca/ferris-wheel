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
 */

package com.ctrip.ferriswheel.core.dom.helper;

import com.ctrip.ferriswheel.common.chart.Interval;
import com.ctrip.ferriswheel.common.view.*;
import com.ctrip.ferriswheel.core.bean.ColorImpl;
import com.ctrip.ferriswheel.core.bean.IntervalImpl;
import com.ctrip.ferriswheel.core.util.HtmlHelper;
import com.ctrip.ferriswheel.core.view.LayoutImpl;

public class AttributeSerializer {
    private static final char WHITESPACE = ' ';
    private static final char SLASH = '/';
    private static final char ATTR_FIELDS_DELIMITER = ';';
    private static final char ATTR_KEY_VALUE_DELIMITER = ':';
    private static final char ATTR_VALUES_DELIMITER = WHITESPACE;

    private static final String DISPLAY = "display";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String ALIGN = "align";
    private static final String VERTICAL_ALIGN = "vertical-align";
    private static final String GRID_CONTAINER_ROWS = "grid-rows";
    private static final String GRID_CONTAINER_COLUMNS = "grid-columns";
    private static final String GRID_ITEM_ROW = "grid-row";
    private static final String GRID_ITEM_COLUMN = "grid-column";

    public String serializeInterval(Interval interval) {
        return String.format("%lf %lf",
                interval.getFrom(), interval.getTo());
    }

    public Interval deserializeInterval(String interval) {
        if (interval == null) {
            return null;
        }
        String[] parts = interval.split(String.valueOf(WHITESPACE));
        if (parts.length != 2) {
            throw new IllegalArgumentException("Malformed interval string: " + interval);
        }
        return new IntervalImpl(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    public String serializeColor(Color color) {
        return String.format("%f %f %f %f",
                color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public Color deserializeColor(String color) {
        if (color == null) {
            return null;
        }
        String[] parts = color.split(String.valueOf(WHITESPACE));
        if (parts.length != 4) {
            throw new IllegalArgumentException("Malformed color string: " + color);
        }
        return new ColorImpl(Float.parseFloat(parts[0]),
                Float.parseFloat(parts[1]),
                Float.parseFloat(parts[2]),
                Float.parseFloat(parts[3]));
    }

    public String serializeLayout(Layout layout) {
        StringBuilder sb = new StringBuilder();
        appendDisplay(sb, layout.getDisplay());
        appendWidth(sb, layout.getWidth());
        appendHeight(sb, layout.getHeight());
        appendAlign(sb, layout.getAlign());
        appendVerticalAlign(sb, layout.getVerticalAlign());
        appendGrid(sb, layout.getGrid());
        return sb.toString();
    }

    private void appendDisplay(StringBuilder sb, Display display) {
        if (display != null) {
            appendAttrField(sb, DISPLAY, display.name());
        }
    }

    private void appendWidth(StringBuilder sb, int width) {
        appendAttrField(sb, WIDTH, width);
    }

    private void appendHeight(StringBuilder sb, int height) {
        appendAttrField(sb, HEIGHT, height);
    }

    private void appendAlign(StringBuilder sb, Placement align) {
        if (align != null) {
            appendAttrField(sb, ALIGN, align.name());
        }
    }

    private void appendVerticalAlign(StringBuilder sb, Placement verticalAlign) {
        if (verticalAlign != null) {
            appendAttrField(sb, VERTICAL_ALIGN, verticalAlign.name());
        }
    }

    private void appendGrid(StringBuilder sb, Grid grid) {
        if (grid == null) {
            return;
        }
        if (grid.getRows() > 0) {
            appendAttrField(sb, GRID_CONTAINER_ROWS, grid.getRows());
        }
        if (grid.getColumns() > 0) {
            appendAttrField(sb, GRID_CONTAINER_COLUMNS, grid.getColumns());
        }
        if (grid.getRow() != null) {
            appendAttrField(sb, GRID_ITEM_ROW, grid.getRow());
        }
        if (grid.getColumn() != null) {
            appendAttrField(sb, GRID_ITEM_COLUMN, grid.getColumn());
        }
    }

    private void appendAttrField(StringBuilder sb, String name, Span span) {
        if (sb.length() > 0) {
            sb.append(WHITESPACE);
        }
        sb.append(name)
                .append(ATTR_KEY_VALUE_DELIMITER)
                .append(WHITESPACE)
                .append(span.getStart())
                .append(WHITESPACE)
                .append(SLASH)
                .append(WHITESPACE)
                .append(span.getEnd())
                .append(ATTR_FIELDS_DELIMITER);
    }

    private void appendAttrField(StringBuilder sb, String name, int value) {
        if (sb.length() > 0) {
            sb.append(WHITESPACE);
        }
        sb.append(name)
                .append(ATTR_KEY_VALUE_DELIMITER)
                .append(WHITESPACE)
                .append(value)
                .append(ATTR_FIELDS_DELIMITER);
    }

    private void appendAttrField(StringBuilder sb, String name, String value) {
        if (sb.length() > 0) {
            sb.append(WHITESPACE);
        }
        sb.append(name)
                .append(ATTR_KEY_VALUE_DELIMITER)
                .append(WHITESPACE)
                .append(safeAttrValue(value))
                .append(ATTR_FIELDS_DELIMITER);
    }

    private String safeAttrValue(String value) {
        return HtmlHelper.escapeHtml(value);
    }

    public Layout deserializeLayout(String layoutAttribute) {
        if (layoutAttribute == null) {
            return null;
        }
        LayoutImpl layout = new LayoutImpl();
        DetectState detectState = new DetectState();
        while (detectState.nextPos < layoutAttribute.length()) {
            detectFieldName(detectState, layoutAttribute);
            if (!detectState.valid && detectState.nextPos < layoutAttribute.length()) {
                throw new IllegalArgumentException("Malformed attribute: " + layoutAttribute);
            }
            String fieldName = layoutAttribute.substring(detectState.from, detectState.to);

            detectFieldValue(detectState, layoutAttribute);
            if (!detectState.valid) {
                throw new IllegalArgumentException("Malformed attribute: " + layoutAttribute);
            }
            String fieldValue = HtmlHelper.unescapeHtml(layoutAttribute, detectState.from, detectState.to);

            mergeLayoutAttribute(layout, fieldName, fieldValue);
        }
        return layout;
    }

    private void mergeLayoutAttribute(LayoutImpl layout, String fieldName, String fieldValue) {
        if (DISPLAY.equalsIgnoreCase(fieldName)) {
            layout.setDisplay(Display.valueOf(fieldValue));

        } else if (WIDTH.equalsIgnoreCase(fieldName)) {
            layout.setWidth(Integer.parseInt(fieldValue));

        } else if (HEIGHT.equalsIgnoreCase(fieldName)) {
            layout.setHeight(Integer.parseInt(fieldValue));

        } else if (ALIGN.equalsIgnoreCase(fieldName)) {
            layout.setAlign(Placement.valueOf(fieldValue));

        } else if (VERTICAL_ALIGN.equalsIgnoreCase(fieldName)) {
            layout.setVerticalAlign(Placement.valueOf(fieldValue));

        } else if (GRID_CONTAINER_ROWS.equalsIgnoreCase(fieldName)) {
            if (layout.getGrid() == null) {
                layout.setGrid(new LayoutImpl.GridImpl());
            }
            layout.getGrid().setRows(Integer.parseInt(fieldValue));

        } else if (GRID_CONTAINER_COLUMNS.equalsIgnoreCase(fieldName)) {
            if (layout.getGrid() == null) {
                layout.setGrid(new LayoutImpl.GridImpl());
            }
            layout.getGrid().setColumns(Integer.parseInt(fieldValue));

        } else if (GRID_ITEM_ROW.equalsIgnoreCase(fieldName)) {
            if (layout.getGrid() == null) {
                layout.setGrid(new LayoutImpl.GridImpl());
            }
            layout.getGrid().setRow(parseSpan(fieldValue));

        } else if (GRID_ITEM_COLUMN.equalsIgnoreCase(fieldName)) {
            if (layout.getGrid() == null) {
                layout.setGrid(new LayoutImpl.GridImpl());
            }
            layout.getGrid().setColumn(parseSpan(fieldValue));

        } else {
            throw new IllegalArgumentException("Invalid field name: " + fieldName);
        }
    }

    private Span parseSpan(String value) {
        String[] parts = value.split(String.valueOf(SLASH));
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid span value: " + value);
        }
        return new LayoutImpl.SpanImpl(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    private void detectFieldName(DetectState detectState, String layoutAttribute) {
        detectState.valid = false;

        detectState.from = skipEmptyChars(layoutAttribute, detectState.nextPos);
        if (detectState.from >= layoutAttribute.length()) {
            return;
        }

        if (!isNameStart(layoutAttribute.charAt(detectState.from))) {
            throw new IllegalArgumentException("Malformed attribute, expects field name: " +
                    detectState.from + "@" + layoutAttribute);
        }

        detectState.nextPos = detectState.from + 1;
        while (detectState.nextPos < layoutAttribute.length()) {
            if (isNamePart(layoutAttribute.charAt(detectState.nextPos))) {
                detectState.nextPos++;
            } else {
                break;
            }
        }
        int pos = detectState.nextPos;
        if (pos < layoutAttribute.length()) {
            pos = skipEmptyChars(layoutAttribute, pos);
        }
        if (pos >= layoutAttribute.length()) {
            throw new IllegalArgumentException("Malformed attribute, expects colon: " +
                    pos + "@" + layoutAttribute);
        } else {
            char ch = layoutAttribute.charAt(pos);
            if (ch != ATTR_KEY_VALUE_DELIMITER) {
                throw new IllegalArgumentException("Malformed attribute, expects colon: " +
                        pos + "@" + layoutAttribute);
            }
            pos++;
        }
        detectState.to = detectState.nextPos;
        detectState.nextPos = pos;
        detectState.valid = true;
    }

    private boolean isNameStart(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private boolean isNamePart(char ch) {
        return isNameStart(ch) || ch == '-' || (ch >= '0' && ch <= '9');
    }

    private void detectFieldValue(DetectState detectState, String layoutAttribute) {
        detectState.valid = false;

        detectState.from = skipEmptyChars(layoutAttribute, detectState.nextPos);
        if (detectState.from >= layoutAttribute.length()) {
            throw new IllegalArgumentException("Malformed attribute, expects value: " +
                    detectState.from + "@" + layoutAttribute);
        }

        while (detectState.nextPos < layoutAttribute.length()) {
            char ch = layoutAttribute.charAt(detectState.nextPos++);
            if (ch == ATTR_FIELDS_DELIMITER) {
                break;

            } else if (!Character.isWhitespace(ch)) {
                detectState.to = detectState.nextPos;
            }
        }

        if (detectState.to > detectState.from) {
            detectState.valid = true;
        }
    }

    class DetectState {
        int from;
        int to;
        int nextPos;
        boolean valid;
    }

    private int skipEmptyChars(String layoutAttribute, int pos) {
        if (pos < 0 || pos >= layoutAttribute.length()) {
            throw new IllegalArgumentException();
        }
        while (pos < layoutAttribute.length()
                && Character.isWhitespace(layoutAttribute.charAt(pos)))
            pos++;
        return pos;
    }
}
