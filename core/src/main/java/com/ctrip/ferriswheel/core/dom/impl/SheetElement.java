/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
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
 */

package com.ctrip.ferriswheel.core.dom.impl;


import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.SheetAsset;
import com.ctrip.ferriswheel.common.Workbook;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.text.Text;
import com.ctrip.ferriswheel.common.view.Layout;
import com.ctrip.ferriswheel.core.dom.helper.TagNames;

import java.util.Iterator;

public class SheetElement extends AbstractElement implements Sheet {
    static final String TAG_NAME = TagNames.SHEET;

    @Override
    public String getTagName() {
        return TAG_NAME;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Workbook getWorkbook() {
        return null;
    }

    @Override
    public int getAssetCount() {
        return 0;
    }

    @Override
    public <T extends SheetAsset> T addAsset(Class<T> clazz, String name) {
        return null;
    }

    @Override
    public <T extends SheetAsset> T addAsset(Class<T> clazz, T asset) {
        return null;
    }

    @Override
    public <T extends SheetAsset> T getAsset(int index) {
        return null;
    }

    @Override
    public <T extends SheetAsset> T getAsset(String name) {
        return null;
    }

    @Override
    public <T extends SheetAsset> T removeAsset(int index) {
        return null;
    }

    @Override
    public <T extends SheetAsset> T removeAsset(String name) {
        return null;
    }

    @Override
    public void renameAsset(String oldName, String newName) {

    }

    @Override
    public Chart updateChart(String name, Chart chartData) {
        return null;
    }

    @Override
    public Text updateText(String name, Text textData) {
        return null;
    }

    @Override
    public void layoutAsset(String assetName, Layout layout) {

    }

    @Override
    public Layout getLayout() {
        return null;
    }

    @Override
    public Iterator<SheetAsset> iterator() {
        return null;
    }
}
