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
import com.ctrip.ferriswheel.api.text.Text;
import com.ctrip.ferriswheel.api.view.Displayable;
import com.ctrip.ferriswheel.api.view.Layout;

/**
 * Sheet holds tables and charts, it is a layout container.
 */
public interface Sheet extends Displayable, Iterable<SheetAsset> {
    /**
     * Get sheet name.
     *
     * @return
     */
    String getName();

    /**
     * Get workbook to which the represented sheet belongs to.
     *
     * @return
     */
    Workbook getWorkbook();

    /**
     * Get asset count.
     *
     * @return
     */
    int getAssetCount();

    /**
     * Add asset.
     *
     * @param clazz Class object that represents an interface which extends
     *              {@link SheetAsset}, the class object decides what kind of
     *              asset will be added.
     * @param name  Asset name.
     * @param <T>   Subclass of {@link SheetAsset} which represents an specific
     *              asset, should be an interface.
     * @return New asset object.
     */
    <T extends SheetAsset> T addAsset(Class<T> clazz, String name);

    /**
     * Add asset.
     *
     * @param clazz Class object that represents an interface which extends
     *              {@link SheetAsset}, the class object decides what kind of
     *              asset will be added.
     * @param asset Asset object with initial data.
     * @param <T>   Subclass of {@link SheetAsset} which represents an specific
     *              asset, and should be an interface.
     * @return New asset object.
     */
    <T extends SheetAsset> T addAsset(Class<T> clazz, T asset);

    /**
     * Get asset by index.
     *
     * @param index Index of the target asset.
     * @param <T>   Subclass of {@link SheetAsset} which represents an specific
     *              asset.
     * @return Asset object.
     */
    <T extends SheetAsset> T getAsset(int index);

    /**
     * Get asset by name.
     *
     * @param name Name of the target asset.
     * @param <T>  Subclass of {@link SheetAsset} which represents an specific
     *             asset.
     * @return Asset object.
     */
    <T extends SheetAsset> T getAsset(String name);

    /**
     * Remove asset by index.
     *
     * @param index Index of the target asset.
     * @param <T>   Subclass of {@link SheetAsset} which represents an specific
     *              asset.
     * @return Removed asset object.
     */
    <T extends SheetAsset> T removeAsset(int index);


    /**
     * Remove asset by name.
     *
     * @param name Name of the target asset.
     * @param <T>  Subclass of {@link SheetAsset} which represents an specific
     *             asset.
     * @return Removed asset object.
     */
    <T extends SheetAsset> T removeAsset(String name);

    /**
     * Rename asset.
     *
     * @param oldName Old asset name.
     * @param newName New asset name.
     */
    void renameAsset(String oldName, String newName);

    /**
     * TODO move it to Chart implementation.
     *
     * @param name
     * @param chartData
     * @return
     */
    @Deprecated
    Chart updateChart(String name, Chart chartData);

    /**
     * TODO move it to Text implementation.
     *
     * @param name
     * @param textData
     * @return
     */
    @Deprecated
    Text updateText(String name, Text textData);

    /**
     * Layout asset by name.
     *
     * @param assetName Name of the target asset.
     * @param layout    Layout data.
     */
    void layoutAsset(String assetName, Layout layout);
}
