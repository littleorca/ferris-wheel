package com.ctrip.ferriswheel.core.view;

import com.ctrip.ferriswheel.common.view.Display;
import com.ctrip.ferriswheel.common.view.Layout;
import com.ctrip.ferriswheel.common.view.Placement;

// TODO asset layout should behaves like asset node, with revision mark
public class AssetLayout extends LayoutImpl {
    public AssetLayout() {
        super(Display.BLOCK,
                0,
                0,
                Placement.LEFT,
                Placement.TOP,
                new GridImpl(0, 0, new SpanImpl(1, 7), new SpanImpl(1, 6)));
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
            setGrid(new GridImpl(0, 0, new SpanImpl(1, 7), new SpanImpl(1, 6)));
        }
    }

}
