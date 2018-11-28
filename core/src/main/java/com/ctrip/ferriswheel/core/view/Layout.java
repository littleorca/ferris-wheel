package com.ctrip.ferriswheel.core.view;

import com.ctrip.ferriswheel.core.intf.Display;
import com.ctrip.ferriswheel.core.intf.Placement;

public class Layout {
    private Display display;
    private int width;
    private int height;
    private Placement align;
    private Placement verticalAlign;
    private Grid grid;

    public Layout() {
    }

    public Layout(Layout layout) {
        this(layout.display,
                layout.width,
                layout.height,
                layout.align,
                layout.verticalAlign,
                layout.grid);
    }

    public Layout(Display display,
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
        this.grid = grid;
    }

    public void copy(Layout layout) {
        this.display = layout.display;
        this.width = layout.width;
        this.height = layout.height;
        this.align = layout.align;
        this.verticalAlign = layout.verticalAlign;
        if (layout.getGrid() == null) {
            this.grid = null;
        } else {
            if (this.grid == null) {
                this.grid = new Grid(layout.getGrid());
            } else {
                this.grid.copy(layout.getGrid());
            }
        }
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

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public static class Grid {
        private int columns;
        private int rows;
        private Span column;
        private Span row;

        public Grid() {
        }

        public Grid(Grid grid) {
            this(grid.getColumns(), grid.getRows(), grid.getColumn(), grid.getRow());
        }

        public Grid(int columns, int rows, Span column, Span row) {
            this.columns = columns;
            this.rows = rows;
            this.column = column;
            this.row = row;
        }

        public void copy(Grid grid) {
            this.columns = grid.getColumns();
            this.rows = grid.getRows();
            if (grid.getColumn() == null) {
                this.column = null;
            } else {
                if (this.column == null) {
                    this.column = new Span(grid.getColumn());
                } else {
                    this.column.copy(grid.getColumn());
                }
            }
            if (grid.getRow() == null) {
                this.row = null;
            } else {
                if (this.row == null) {
                    this.row = new Span(grid.getRow());
                } else {
                    this.row.copy(grid.getRow());
                }
            }
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

        public Span getColumn() {
            return column;
        }

        public void setColumn(Span column) {
            this.column = column;
        }

        public Span getRow() {
            return row;
        }

        public void setRow(Span row) {
            this.row = row;
        }
    }

    public static class Span {
        private int start;
        private int end;

        public Span() {
        }

        public Span(Span span) {
            this(span.getStart(), span.getEnd());
        }

        public Span(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public void copy(Span span) {
            this.start = span.getStart();
            this.end = span.getEnd();
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
