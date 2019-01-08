package com.ctrip.ferriswheel.core.view;

import com.ctrip.ferriswheel.common.view.Display;
import com.ctrip.ferriswheel.common.view.Layout;
import com.ctrip.ferriswheel.common.view.Placement;

public class SheetLayout extends LayoutImpl {
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
                new GridImpl(DEFAULT_COLUMNS, DEFAULT_ROWS, null, null));
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
            setGrid(new GridImpl(DEFAULT_COLUMNS, DEFAULT_ROWS, null, null));
        }
    }
}
