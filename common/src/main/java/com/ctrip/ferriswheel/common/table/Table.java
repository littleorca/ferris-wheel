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

package com.ctrip.ferriswheel.common.table;

import com.ctrip.ferriswheel.common.SheetAsset;
import com.ctrip.ferriswheel.common.automaton.Automaton;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.view.Displayable;

import java.util.Map;

/**
 * Holds data grid and provides manipulating interface.
 *
 * @see Row
 * @see Cell
 */
public interface Table extends Iterable<Map.Entry<Integer, Row>>, Displayable, SheetAsset {
    /**
     * Get row count.
     *
     * @return
     */
    int getRowCount();

    /**
     * Get row header by index.
     *
     * @param rowIndex
     * @return
     */
    Header getRowHeader(int rowIndex);

    /**
     * Get row by index.
     *
     * @param rowIndex
     * @return
     */
    Row getRow(int rowIndex);

    /**
     * Get column count.
     *
     * @return
     */
    int getColumnCount();

    /**
     * Get column header by index.
     *
     * @param columnIndex
     * @return
     */
    Header getColumnHeader(int columnIndex);

    /**
     * Get cell at the specified position.
     *
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    Cell getCell(int rowIndex, int columnIndex);

    /**
     * Set cell value at specified position, return the old cell value or null if absent.
     *
     * @param rowIndex
     * @param columnIndex
     * @param value
     * @return
     */
    Variant setCellValue(int rowIndex, int columnIndex, Variant value);

    /**
     * Set cell formula at specified position, return the old cell formula or null if absent.
     *
     * @param rowIndex
     * @param columnIndex
     * @param formula
     * @return
     */
    String setCellFormula(int rowIndex, int columnIndex, String formula);

    /**
     * Fill upward from a single cell.
     *
     * @param rowIndex    Row index of the source cell.
     * @param columnIndex Column index of the source cell.
     * @param nRows       Number of rows to fill up.
     */
    void fillUp(int rowIndex, int columnIndex, int nRows);

    /**
     * Fill upward from a row segment.
     *
     * @param rowIndex    Row index of the source cells.
     * @param firstColumn First column index of the source cells.
     * @param lastColumn  Last column index of the source cells.
     * @param nRows       Number of rows to fill upward.
     */
    void fillUp(int rowIndex, int firstColumn, int lastColumn, int nRows);

    /**
     * Fill rightward from a single cell.
     *
     * @param rowIndex    Row index of the source cell.
     * @param columnIndex Column index of the source cell.
     * @param nColumns    Number of columns to fill rightward.
     */
    void fillRight(int rowIndex, int columnIndex, int nColumns);

    /**
     * Fill rightward from a column segment.
     *
     * @param firstRow    First row index of the source cells.
     * @param lastRow     Last row index of the source cells.
     * @param columnIndex Column index of the source cells.
     * @param nColumns    Number of columns to fill rightward.
     */
    void fillRight(int firstRow, int lastRow, int columnIndex, int nColumns);

    /**
     * Fill downward from a single cell.
     *
     * @param rowIndex    Row index of the source cell.
     * @param columnIndex Column index of the source cell.
     * @param nRows       Number of rows to fill downward.
     */
    void fillDown(int rowIndex, int columnIndex, int nRows);

    /**
     * Fill downward from a row segment.
     *
     * @param rowIndex    Row index of the source cells.
     * @param firstColumn First column index of the source cells.
     * @param lastColumn  Last column index of the source cells.
     * @param nRows       Number of rows to fill downward.
     */
    void fillDown(int rowIndex, int firstColumn, int lastColumn, int nRows);

    /**
     * Fill leftward from a single cell.
     *
     * @param rowIndex    Row index of the source cell.
     * @param columnIndex Column index of the source cell.
     * @param nColumns    Number of columns to fill leftward.
     */
    void fillLeft(int rowIndex, int columnIndex, int nColumns);

    /**
     * Fill leftward from a column segment.
     *
     * @param firstRow    First row index of the source cells.
     * @param lastRow     Last row index of the source cells.
     * @param columnIndex Column index of the source cells.
     * @param nColumns    Number of columns to fill leftward.
     */
    void fillLeft(int firstRow, int lastRow, int columnIndex, int nColumns);

    /**
     * Set cell fill up flag
     *
     * @param rowIndex
     * @param columnIndex
     * @param fillUp
     */
    @Deprecated
    void setCellFillUp(int rowIndex, int columnIndex, boolean fillUp);

    /**
     * Set fill down flag
     *
     * @param rowIndex
     * @param columnIndex
     * @param fillDown
     */
    @Deprecated
    void setCellFillDown(int rowIndex, int columnIndex, boolean fillDown);

    /**
     * Set fill left flag
     *
     * @param rowIndex
     * @param columnIndex
     * @param fillLeft
     */
    @Deprecated
    void setCellFillLeft(int rowIndex, int columnIndex, boolean fillLeft);

    /**
     * Set fill right flag
     *
     * @param rowIndex
     * @param columnIndex
     * @param fillRight
     */
    @Deprecated
    void setCellFillRight(int rowIndex, int columnIndex, boolean fillRight);

    /**
     * Set cells' format.
     *
     * @param rowIndex
     * @param columnIndex
     * @param nRows
     * @param nColumns
     * @param format
     */
    void setCellsFormat(int rowIndex, int columnIndex, int nRows, int nColumns, String format);

    /**
     * Erase cells' values without remove themselves.
     *
     * @param top
     * @param right
     * @param bottom
     * @param left
     */
    void eraseCells(int top, int right, int bottom, int left);

    /**
     * Add rows to the bottom.
     *
     * @param nRows positive number of rows to insert.
     * @see #addRows(int, int)
     */
    default void addRows(int nRows) {
        addRows(getRowCount(), nRows);
    }

    /**
     * Add rows at specified position.
     *
     * @param rowIndex row index begin with 0, where to insert new rows.
     * @param nRows    positive number of rows to insert.
     * @see #addRows(int)
     */
    void addRows(int rowIndex, int nRows);

    /**
     * Remove rows and shrink spaces.
     *
     * @param rowIndex row index begin with 0, first row to remove.
     * @param nRows    positive number of rows to remove
     */
    void removeRows(int rowIndex, int nRows);

    /**
     * Add columns to the right.
     *
     * @param nCols positive number of columns to insert.
     * @see #addColumns(int, int)
     */
    default void addColumns(int nCols) {
        addColumns(getColumnCount(), nCols);
    }

    /**
     * Add columns at specified position.
     *
     * @param colIndex column index begin with 0, where to insert new columns.
     * @param nCols    positive number of columns to insert.
     * @see #addColumns(int)
     */
    void addColumns(int colIndex, int nCols);

    /**
     * Remove columns and shrink spaces.
     *
     * @param colIndex column index begin with 0, first column to remove.
     * @param nCols    positive number of columns to remove.
     */
    void removeColumns(int colIndex, int nCols);

    /**
     * Using the specified automate solution to automatically fill table data.
     *
     * @param solution
     */
    void automate(AutomateConfiguration solution);

    /**
     * Get table automaton.
     *
     * @return correlated table automaton, or null if this table is not automated.
     */
    Automaton getAutomaton();

    /**
     * Get automate configuration.
     *
     * @return Automate configuration.
     */
    AutomateConfiguration getAutomateConfiguration();

}
