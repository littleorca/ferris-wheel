package com.ctrip.ferriswheel.core.view;

import com.ctrip.ferriswheel.core.intf.Display;
import com.ctrip.ferriswheel.core.intf.Placement;

public class SheetLayout extends Layout {
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 720;
    private static final int DEFAULT_COLUMNS = 12;
    private static final int DEFAULT_ROWS = 0;

    public SheetLayout() {
        super(Display.GRID,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                Placement.LEFT,
                Placement.TOP,
                new Grid(DEFAULT_COLUMNS, DEFAULT_ROWS, null, null));
    }

    @Override
    public void copy(Layout layout) {
        super.copy(layout);
        if (getDisplay() != Display.GRID) {
            // FIXME here do some fix, may remove this in the future.
            setDisplay(Display.GRID);
            setWidth(DEFAULT_WIDTH);
            setHeight(DEFAULT_HEIGHT);
            setAlign(Placement.LEFT);
            setVerticalAlign(Placement.TOP);
            setGrid(new Grid(DEFAULT_COLUMNS, DEFAULT_ROWS, null, null));
        }
    }
}
