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

import com.ctrip.ferriswheel.common.automaton.Automaton;
import com.ctrip.ferriswheel.common.table.AutomateConfiguration;
import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Header;
import com.ctrip.ferriswheel.common.table.Row;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.view.Layout;
import com.ctrip.ferriswheel.core.dom.TableElement;

import java.util.Iterator;
import java.util.Map;

public final class TableElementImpl extends AbstractLayoutElement implements TableElement {

    protected TableElementImpl(AbstractDocument ownerDocument) {
        super(ownerDocument);
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public Header getRowHeader(int rowIndex) {
        return null;
    }

    @Override
    public Row getRow(int rowIndex) {
        return null;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public Header getColumnHeader(int columnIndex) {
        return null;
    }

    @Override
    public Cell getCell(int rowIndex, int columnIndex) {
        return null;
    }

    @Override
    public Variant setCellValue(int rowIndex, int columnIndex, Variant value) {
        return null;
    }

    @Override
    public String setCellFormula(int rowIndex, int columnIndex, String formula) {
        return null;
    }

    @Override
    public void fillUp(int rowIndex, int columnIndex, int nRows) {

    }

    @Override
    public void fillUp(int rowIndex, int firstColumn, int lastColumn, int nRows) {

    }

    @Override
    public void fillRight(int rowIndex, int columnIndex, int nColumns) {

    }

    @Override
    public void fillRight(int firstRow, int lastRow, int columnIndex, int nColumns) {

    }

    @Override
    public void fillDown(int rowIndex, int columnIndex, int nRows) {

    }

    @Override
    public void fillDown(int rowIndex, int firstColumn, int lastColumn, int nRows) {

    }

    @Override
    public void fillLeft(int rowIndex, int columnIndex, int nColumns) {

    }

    @Override
    public void fillLeft(int firstRow, int lastRow, int columnIndex, int nColumns) {

    }

    @Override
    public void setCellFillUp(int rowIndex, int columnIndex, boolean fillUp) {

    }

    @Override
    public void setCellFillDown(int rowIndex, int columnIndex, boolean fillDown) {

    }

    @Override
    public void setCellFillLeft(int rowIndex, int columnIndex, boolean fillLeft) {

    }

    @Override
    public void setCellFillRight(int rowIndex, int columnIndex, boolean fillRight) {

    }

    @Override
    public void setCellsFormat(int rowIndex, int columnIndex, int nRows, int nColumns, String format) {

    }

    @Override
    public void eraseCells(int top, int right, int bottom, int left) {

    }

    @Override
    public void addRows(int rowIndex, int nRows) {

    }

    @Override
    public void removeRows(int rowIndex, int nRows) {

    }

    @Override
    public void addColumns(int colIndex, int nCols) {

    }

    @Override
    public void removeColumns(int colIndex, int nCols) {

    }

    @Override
    public void automate(AutomateConfiguration solution) {

    }

    @Override
    public Automaton getAutomaton() {
        return null;
    }

    @Override
    public AutomateConfiguration getAutomateConfiguration() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Layout getLayout() {
        return null;
    }

    @Override
    public Iterator<Map.Entry<Integer, Row>> iterator() {
        return null;
    }
}
