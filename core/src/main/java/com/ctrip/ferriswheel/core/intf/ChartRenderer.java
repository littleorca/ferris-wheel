package com.ctrip.ferriswheel.core.intf;

import java.awt.image.RenderedImage;

public interface ChartRenderer {
    boolean accepts(Chart chart);

    RenderedImage render(Chart chart);
}
