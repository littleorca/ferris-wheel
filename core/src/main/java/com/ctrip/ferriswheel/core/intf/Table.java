package com.ctrip.ferriswheel.core.intf;

import com.ctrip.ferriswheel.core.bean.ColumnHeader;
import com.ctrip.ferriswheel.core.bean.ColumnHeader;
import com.ctrip.ferriswheel.core.bean.RowHeader;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;

/**
 * Holds data grid and provides manipulating interface.
 *
 * @see Row
 * @see Cell
 */
public interface Table extends NamedAsset, Iterable<Row>, Displayable {

    /**
     * Get max supported rows.
     *
     * @return
     */
    int getMaxRowLimit();

    /**
     * Get max supported columns.
     *
     * @return
     */
    int getMaxColumnLimit();

    /**
     * Get sheet which this table belongs to.
     *
     * @return
     */
    Sheet getSheet();

    /**
     * Get row count.
     *
     * @return
     */
    int getRowCount();

    /**
     * Get row by index.
     *
     * @param rowIndex
     * @return
     */
    Row getRow(int rowIndex);

    RowHeader getRowHeader(int rowIndex);

    /**
     * Get column count.
     *
     * @return
     */
    int getColumnCount();

    ColumnHeader getColumnHeader(int columnIndex);

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
    void setCellFillUp(int rowIndex, int columnIndex, boolean fillUp);

    /**
     * Set fill down flag
     *
     * @param rowIndex
     * @param columnIndex
     * @param fillDown
     */
    void setCellFillDown(int rowIndex, int columnIndex, boolean fillDown);

    /**
     * Set fill left flag
     *
     * @param rowIndex
     * @param columnIndex
     * @param fillLeft
     */
    void setCellFillLeft(int rowIndex, int columnIndex, boolean fillLeft);

    /**
     * Set fill right flag
     *
     * @param rowIndex
     * @param columnIndex
     * @param fillRight
     */
    void setCellFillRight(int rowIndex, int columnIndex, boolean fillRight);

    /**
     * Erase cell without remove cell object.
     *
     * @param rowIndex
     * @param columnIndex
     */
    void eraseCell(int rowIndex, int columnIndex);

    /**
     * Insert rows at specified position.
     *
     * @param rowIndex row index begin with 0, where to insert new rows.
     * @param nRows    positive number of rows to insert.
     */
    void insertRows(int rowIndex, int nRows);

    /**
     * Erase rows without remove there spaces.
     *
     * @param rowIndex row index begin with 0, first row to erase.
     * @param nRows    positive number of rows to erase.
     */
    void eraseRows(int rowIndex, int nRows);

    /**
     * Remove rows and shrink spaces.
     *
     * @param rowIndex row index begin with 0, first row to remove.
     * @param nRows    positive number of rows to remove
     */
    void removeRows(int rowIndex, int nRows);

    /**
     * Insert columns at specified position.
     *
     * @param colIndex column index begin with 0, where to insert new columns.
     * @param nCols    positive number of columns to insert.
     */
    void insertColumns(int colIndex, int nCols);

    /**
     * Erase columns without remove there spaces.
     *
     * @param colIndex column index begin with 0, first column to erase.
     * @param nCols    positive number of columns to erase.
     */
    void eraseColumns(int colIndex, int nCols);

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
    void automate(TableAutomatonInfo solution);

    /**
     * Get table automaton.
     *
     * @return correlated table automaton, or null if this table is not automated.
     */
    TableAutomaton getAutomaton();
}
