package com.ctrip.ferriswheel.core.view;

import com.ctrip.ferriswheel.core.intf.Display;
import com.ctrip.ferriswheel.core.intf.Placement;

public class AssetLayout extends Layout {
    public AssetLayout() {
        super(Display.BLOCK,
                0,
                0,
                Placement.LEFT,
                Placement.TOP,
                new Grid(0, 0, new Span(1, 7), new Span(1, 6)));
    }

    @Override
    public void copy(Layout layout) {
        super.copy(layout);
        if (layout.getDisplay() != Display.BLOCK) {
            // FIXME here do some fix, may remove this in the future.
            setDisplay(Display.BLOCK);
            setWidth(0);
            setHeight(0);
            setAlign(Placement.LEFT);
            setVerticalAlign(Placement.TOP);
            setGrid(new Grid(0, 0, new Span(1, 7), new Span(1, 6)));
        }
    }

}
