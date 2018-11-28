package com.ctrip.ferriswheel.core.renderer;

import com.ctrip.ferriswheel.core.intf.Chart;
import com.ctrip.ferriswheel.core.intf.ChartRenderer;

import java.util.concurrent.CopyOnWriteArrayList;

public class ChartRendererManager {
    private static final CopyOnWriteArrayList<ChartRenderer> RENDERERS = new CopyOnWriteArrayList<>();

    public static void registerRenderer(ChartRenderer chartRenderer) {
        RENDERERS.addIfAbsent(chartRenderer);
    }

    public static ChartRenderer getRenderer(Chart chart) {
        for (ChartRenderer renderer : RENDERERS) {
            if (isRendererMatches(renderer, chart)) {
                return renderer;
            }
        }
        return null;
    }

    private static boolean isRendererMatches(ChartRenderer renderer, Chart chart) {
        return renderer.accepts(chart);
    }

}
