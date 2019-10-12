package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.core.formula.Formula;
import com.ctrip.ferriswheel.core.formula.FormulaParser;
import com.ctrip.ferriswheel.core.util.AreaPatternAnalyzer;

public class AutoFiller {
    //    private Workbook workbook;
    private AreaPatternAnalyzer areaPatternAnalyzer = new AreaPatternAnalyzer();

    public AutoFiller(/*Workbook workbook*/) {
//        this.workbook = workbook;
    }

    public void autoFillRowsIfPossible(Table table, int startRowIndex, int nRows) {
        for (int colIndex = 0; colIndex < table.getColumnCount(); colIndex++) {
            Cell cell = null;
            // try fill down
            if (startRowIndex > 0) {
                cell = table.getCell(startRowIndex - 1, colIndex);
            }
            if (cell != null && cell.isFillDown()) {
                fillDown(table, startRowIndex - 1, colIndex, nRows);
                continue;
            }
            // try fill up
            if (startRowIndex + nRows < table.getRowCount()) {
                cell = table.getCell(startRowIndex + nRows, colIndex);
            }
            if (cell != null && cell.isFillUp()) {
                fillUp(table, startRowIndex + nRows, colIndex, nRows);
            }
        }
    }

    public void autoFillColumnsIfPossible(Table table, int startColIndex, int nCols) {
        for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
            Cell cell = null;
            // try fill right
            if (startColIndex > 0) {
                cell = table.getCell(rowIndex, startColIndex - 1);
            }
            if (cell != null && cell.isFillRight()) {
                fillRight(table, rowIndex, startColIndex - 1, nCols);
                continue;
            }
            // try fill left
            if (startColIndex + nCols < table.getColumnCount()) {
                cell = table.getCell(rowIndex, startColIndex + nCols);
            }
            if (cell != null && cell.isFillLeft()) {
                fillLeft(table, rowIndex, startColIndex + nCols, nCols);
            }
        }
    }

    public boolean autoFillCellIfPossible(Table table, int rowIndex, int colIndex) {
        if (autoFillByNeighbor(table, rowIndex, colIndex, -1, 0))
            return true;
        if (autoFillByNeighbor(table, rowIndex, colIndex, 0, -1))
            return true;
        if (autoFillByNeighbor(table, rowIndex, colIndex, 1, 0))
            return true;
        if (autoFillByNeighbor(table, rowIndex, colIndex, 0, 1))
            return true;
        return false;
    }

    /**
     * Try to fill cell by it's neighbor. Row/column index delta of the neighbor must be
     * either 0 or 1 or -1, and their must has one and only one been set to 0.
     *
     * @param table
     * @param rowIndex
     * @param colIndex
     * @param neighborRowDelta
     * @param neighborColDelta
     * @return
     */
    private boolean autoFillByNeighbor(Table table,
                                       int rowIndex,
                                       int colIndex,
                                       int neighborRowDelta,
                                       int neighborColDelta) {
        int originRowIndex = rowIndex + neighborRowDelta;
        int originColIndex = colIndex + neighborColDelta;
        Cell cell = table.getCell(originRowIndex, originColIndex);
        if (cell == null) {
            return false;
        }
        if (!canFill(cell, -neighborRowDelta, -neighborColDelta)) {
            return false;
        }
        doFill(table, originRowIndex, originColIndex, -neighborRowDelta, -neighborColDelta);
        return true;
    }

    private boolean canFill(Cell originCell, int rowDelta, int colDelta) {
        if (rowDelta == 0) {
            if (colDelta == 0) {
                return false;
            } else if (colDelta == -1) {
                return originCell.isFillLeft();
            } else if (colDelta == 1) {
                return originCell.isFillRight();
            }
        } else if (rowDelta == -1) {
            return originCell.isFillUp();
        } else if (rowDelta == 1) {
            return originCell.isFillDown();
        }
        return false; // may be IllegalArgument.
    }

    public void fillUp(Table table, int rowIndex, int colIndex, int nCells) {
        doFill(table, rowIndex, colIndex, -nCells, 0);
    }

    public void fillDown(Table table, int rowIndex, int colIndex, int nCells) {
        doFill(table, rowIndex, colIndex, nCells, 0);
    }

    public void fillLeft(Table table, int rowIndex, int colIndex, int nCells) {
        doFill(table, rowIndex, colIndex, 0, -nCells);
    }

    public void fillRight(Table table, int rowIndex, int colIndex, int nCells) {
        doFill(table, rowIndex, colIndex, 0, nCells);
    }

    void doFill(Table table, int originRowIndex, int originColIndex, int nRows, int nCols) {
        Cell cell = table.getCell(originRowIndex, originColIndex);

        final int startRow = originRowIndex;
        final int startCol = originColIndex;
        final int endRow = startRow + nRows;
        final int endCol = startCol + nCols;
        final int rowIncr = nRows > 0 ? 1 : nRows < 0 ? -1 : 0;
        final int colIncr = nCols > 0 ? 1 : nCols < 0 ? -1 : 0;

        Formula formula = null;
        if (cell != null && cell.getData().isFormula()) {
            formula = new Formula(cell.getData().getFormulaString());
        }

        for (int rowIndex = Math.min(startRow, endRow);
             rowIndex <= Math.max(startRow, endRow); rowIndex++) {
            for (int colIndex = Math.min(startCol, endCol);
                 colIndex <= Math.max(startCol, endCol); colIndex++) {

                if (rowIncr == 1) {
                    table.setCellFillDown(rowIndex, colIndex, true);
                } else if (rowIncr == -1) {
                    table.setCellFillUp(rowIndex, colIndex, true);
                }

                if (colIncr == 1) {
                    table.setCellFillRight(rowIndex, colIndex, true);
                } else if (colIncr == -1) {
                    table.setCellFillLeft(rowIndex, colIndex, true);
                }

                if (rowIndex == startRow && colIndex == startCol) {
                    continue; // skip origin cell

                } else if (formula != null) {
                    String shiftedFormula = FormulaParser.assemble(formula,
                            rowIndex - startRow,
                            colIndex - startCol);
                    table.setCellFormula(rowIndex, colIndex, shiftedFormula);

                } else {
                    table.setCellFormula(rowIndex, colIndex, null);
                    table.setCellValue(rowIndex, colIndex, cell == null ? null : cell.getData());
                }
            }
        }
    }
}
