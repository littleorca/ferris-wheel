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

package com.ctrip.ferriswheel.core.view;

import com.ctrip.ferriswheel.common.view.*;

import java.util.Objects;

public class LayoutImpl implements Layout {
    private static final String ATTR_FIELDS_DELIMITER = ";";
    private static final String ATTR_KEY_VALUE_DELIMITER = ":";
    private static final String ATTR_VALUES_DELIMITER = " ";

    private static final String ATTR_PATTERN = "()[\\s*;\\s*()]*";

    private Display display;
    private int width;
    private int height;
    private Placement align;
    private Placement verticalAlign;
    private GridImpl grid;

    public LayoutImpl() {
    }

    public LayoutImpl(Layout layout) {
        this(layout.getDisplay(),
                layout.getWidth(),
                layout.getHeight(),
                layout.getAlign(),
                layout.getVerticalAlign(),
                layout.getGrid());
    }

    public LayoutImpl(Display display,
                      int width,
                      int height,
                      Placement align,
                      Placement verticalAlign,
                      Grid grid) {
        this.display = display;
        this.width = width;
        this.height = height;
        this.align = align;
        this.verticalAlign = verticalAlign;
        this.setGrid(grid);
    }

    public LayoutImpl(String serialized) {
        if (serialized == null) {
            return;
        }
        String[] fields = serialized.split(ATTR_FIELDS_DELIMITER);
        for (String field : fields) {
            field.split(ATTR_KEY_VALUE_DELIMITER);
        }
    }

    public void copy(Layout layout) {
        this.display = layout.getDisplay();
        this.width = layout.getWidth();
        this.height = layout.getHeight();
        this.align = layout.getAlign();
        this.verticalAlign = layout.getVerticalAlign();
        if (layout.getGrid() == null) {
            this.grid = null;
        } else {
            if (this.grid == null) {
                this.grid = new GridImpl(layout.getGrid());
            } else {
                this.grid.copy(layout.getGrid());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Layout)) return false;
        Layout layout = (Layout) o;
        return getWidth() == layout.getWidth() &&
                getHeight() == layout.getHeight() &&
                getDisplay() == layout.getDisplay() &&
                getAlign() == layout.getAlign() &&
                getVerticalAlign() == layout.getVerticalAlign() &&
                Objects.equals(getGrid(), layout.getGrid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplay(), getWidth(), getHeight(), getAlign(), getVerticalAlign(), getGrid());
    }

    public Display getDisplay() {
        return display;
    }

    public void setDisplay(Display display) {
        this.display = display;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Placement getAlign() {
        return align;
    }

    public void setAlign(Placement align) {
        this.align = align;
    }

    public Placement getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(Placement verticalAlign) {
        this.verticalAlign = verticalAlign;
    }

    public GridImpl getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = (grid instanceof GridImpl) ? (GridImpl) grid : new GridImpl(grid);
    }

    public static class GridImpl implements Grid {
        private int columns;
        private int rows;
        private SpanImpl column;
        private SpanImpl row;

        public GridImpl() {
        }

        public GridImpl(Grid grid) {
            this(grid.getColumns(), grid.getRows(), grid.getColumn(), grid.getRow());
        }

        public GridImpl(int columns, int rows, Span column, Span row) {
            this.columns = columns;
            this.rows = rows;
            this.setColumn(column);
            this.setRow(row);
        }

        public void copy(Grid grid) {
            this.columns = grid.getColumns();
            this.rows = grid.getRows();
            if (grid.getColumn() == null) {
                this.column = null;
            } else {
                if (this.column == null) {
                    this.column = new SpanImpl(grid.getColumn());
                } else {
                    this.column.copy(grid.getColumn());
                }
            }
            if (grid.getRow() == null) {
                this.row = null;
            } else {
                if (this.row == null) {
                    this.row = new SpanImpl(grid.getRow());
                } else {
                    this.row.copy(grid.getRow());
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Grid)) return false;
            Grid grid = (Grid) o;
            return getColumns() == grid.getColumns() &&
                    getRows() == grid.getRows() &&
                    Objects.equals(getColumn(), grid.getColumn()) &&
                    Objects.equals(getRow(), grid.getRow());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getColumns(), getRows(), getColumn(), getRow());
        }

        public int getColumns() {
            return columns;
        }

        public void setColumns(int columns) {
            this.columns = columns;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public SpanImpl getColumn() {
            return column;
        }

        public void setColumn(Span column) {
            this.column = (column instanceof SpanImpl) ? (SpanImpl) column : new SpanImpl(column);
        }

        public SpanImpl getRow() {
            return row;
        }

        public void setRow(Span row) {
            this.row = (row instanceof SpanImpl) ? (SpanImpl) row : new SpanImpl(row);
        }
    }

    public static class SpanImpl implements Span {
        private int start;
        private int end;

        public SpanImpl() {
        }

        public SpanImpl(Span span) {
            if (span != null) {
                this.start = span.getStart();
                this.end = span.getEnd();
            }
        }

        public SpanImpl(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public void copy(Span span) {
            this.start = span.getStart();
            this.end = span.getEnd();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Span)) return false;
            Span span = (Span) o;
            return getStart() == span.getStart() &&
                    getEnd() == span.getEnd();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getStart(), getEnd());
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }
}
