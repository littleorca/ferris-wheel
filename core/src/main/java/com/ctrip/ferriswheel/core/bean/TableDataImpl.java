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

import com.ctrip.ferriswheel.api.table.AutomateConfiguration;
import com.ctrip.ferriswheel.api.table.Row;
import com.ctrip.ferriswheel.api.table.TableData;
import com.ctrip.ferriswheel.api.view.Layout;
import com.ctrip.ferriswheel.core.util.TreeSparseArray;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

public class TableDataImpl implements TableData, Serializable {
    private TreeSparseArray<Row> rows;
    private AutomateConfiguration automatonInfo;
    private Layout layout;

    public TreeSparseArray<Row> getRows() {
        return rows;
    }

    public void setRows(TreeSparseArray<Row> rows) {
        this.rows = rows;
    }

    @Override
    public int getRowCount() {
        return rows == null ? 0 : rows.size();
    }

    @Override
    public Row getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    public AutomateConfiguration getAutomateConfiguration() {
        return automatonInfo;
    }

    public void setAutomatonSolution(AutomateConfiguration automatonInfo) {
        this.automatonInfo = automatonInfo;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public Iterator<Row> iterator() {
        if (rows == null) {
            return Collections.emptyIterator();
        }
        return Collections.unmodifiableCollection(rows.values()).iterator();
    }
}
