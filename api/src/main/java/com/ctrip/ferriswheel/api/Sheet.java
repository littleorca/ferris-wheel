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

package com.ctrip.ferriswheel.api;

import com.ctrip.ferriswheel.api.chart.Chart;
import com.ctrip.ferriswheel.api.table.TableData;
import com.ctrip.ferriswheel.api.table.Table;
import com.ctrip.ferriswheel.api.text.Text;
import com.ctrip.ferriswheel.api.view.Displayable;
import com.ctrip.ferriswheel.api.view.Layout;

/**
 * Sheet holds tables and charts, it is a layout container.
 */
public interface Sheet extends Displayable, Iterable<SheetAsset> {
    String getName();

    Workbook getWorkbook();

    int getAssetCount();

    SheetAsset getAsset(int index);

    SheetAsset getAsset(String name);

    SheetAsset removeAsset(int index);

    SheetAsset removeAsset(String name);

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
    Chart addChart(String name, Chart chartData);

    /**
     * Update chart with the specified name.
     *
     * @param name
     * @param chartData
     * @return
     */
    Chart updateChart(String name, Chart chartData);

    Text getText(String name);

    Text addText(String name, Text textData);

    Text updateText(String name, Text textData);

    /**
     * Set displayable asset's layout.
     *
     * @param assetName
     * @param layout
     */
    void layoutAsset(String assetName, Layout layout);
}
