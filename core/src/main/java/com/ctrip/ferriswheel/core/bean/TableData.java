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

package com.ctrip.ferriswheel.core.bean;

import com.ctrip.ferriswheel.api.table.AutomateSolution;
import com.ctrip.ferriswheel.api.table.DryRowData;
import com.ctrip.ferriswheel.api.table.DryTableData;
import com.ctrip.ferriswheel.api.view.Layout;
import com.ctrip.ferriswheel.core.util.TreeSparseArray;

import java.io.Serializable;

public class TableData implements DryTableData, Serializable {
    private TreeSparseArray<DryRowData> rows;
    private AutomateSolution automatonInfo;
    private Layout layout;

    public TreeSparseArray<DryRowData> getRows() {
        return rows;
    }

    public void setRows(TreeSparseArray<DryRowData> rows) {
        this.rows = rows;
    }

    public AutomateSolution getAutomatonSolution() {
        return automatonInfo;
    }

    public void setAutomatonSolution(AutomateSolution automatonInfo) {
        this.automatonInfo = automatonInfo;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }
}
