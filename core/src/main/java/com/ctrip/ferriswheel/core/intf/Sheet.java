package com.ctrip.ferriswheel.core.intf;

import com.ctrip.ferriswheel.core.view.Layout;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.TableData;
import com.ctrip.ferriswheel.core.bean.TextData;
import com.ctrip.ferriswheel.core.view.Layout;

/**
 * Sheet holds tables and charts, it is a layout container.
 */
public interface Sheet extends NamedAsset, Displayable, Iterable<NamedAsset> {
    Workbook getWorkbook();

    int getAssetCount();

    NamedAsset getAsset(int index);

    NamedAsset getAsset(String name);

    NamedAsset removeAsset(int index);

    NamedAsset removeAsset(String name);

    void renameAsset(String oldName, String newName);

    Table getTable(String name);

    Table addTable(String name);

    Table addTable(String name, TableData tableData);

    /**
     * Get chart by name.
     *
     * @param name
     * @return
     */
    Chart getChart(String name);

    /**
     * Add new chart.
     *
     * @param name
     * @param chartData
     * @return
     */
    Chart addChart(String name, ChartData chartData);

    /**
     * Update chart with the specified name.
     *
     * @param name
     * @param chartData
     * @return
     */
    Chart updateChart(String name, ChartData chartData);

    Text getText(String name);

    Text addText(String name, TextData textData);

    Text updateText(String name, TextData textData);

    /**
     * Set displayable asset's layout.
     *
     * @param assetName
     * @param layout
     */
    void layoutAsset(String assetName, Layout layout);
}
