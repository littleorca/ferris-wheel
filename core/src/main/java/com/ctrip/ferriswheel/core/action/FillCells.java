package com.ctrip.ferriswheel.core.action;

import com.ctrip.ferriswheel.api.action.Action;

public abstract class FillCells extends TableAction implements Action {

    private FillCells() {
    }

    private FillCells(String sheetName, String tableName) {
        super(sheetName, tableName);
    }

    public abstract int getLeftBoundary();

    public abstract int getTopBoundary();

    public abstract int getRightBoundary();

    public abstract int getBottomBoundary();

    static abstract class FillVertically extends FillCells {
        private int rowIndex;
        private int firstColumn;
        private int lastColumn;
        private int nRows;

        private FillVertically() {
        }

        private FillVertically(String sheetName, String tableName, int rowIndex, int columnIndex, int nRows) {
            this(sheetName, tableName, rowIndex, columnIndex, columnIndex, nRows);
        }

        private FillVertically(String sheetName, String tableName, int rowIndex, int firstColumn, int lastColumn, int nRows) {
            super(sheetName, tableName);
            this.rowIndex = rowIndex;
            this.firstColumn = firstColumn;
            this.lastColumn = lastColumn;
            this.nRows = nRows;
        }

        @Override
        public int getLeftBoundary() {
            return firstColumn;
        }

        @Override
        public int getRightBoundary() {
            return lastColumn;
        }

        public int getRowIndex() {
            return rowIndex;
        }

        public void setRowIndex(int rowIndex) {
            this.rowIndex = rowIndex;
        }

        public int getFirstColumn() {
            return firstColumn;
        }

        public void setFirstColumn(int firstColumn) {
            this.firstColumn = firstColumn;
        }

        public int getLastColumn() {
            return lastColumn;
        }

        public void setLastColumn(int lastColumn) {
            this.lastColumn = lastColumn;
        }

        public int getnRows() {
            return nRows;
        }

        public void setnRows(int nRows) {
            this.nRows = nRows;
        }
    }

    static abstract class FillHorizontally extends FillCells {
        private int firstRow;
        private int lastRow;
        private int columnIndex;
        private int nColumns;

        private FillHorizontally() {
        }

        private FillHorizontally(String sheetName, String tableName, int rowIndex, int columnIndex, int nColumns) {
            this(sheetName, tableName, rowIndex, rowIndex, columnIndex, nColumns);
        }

        private FillHorizontally(String sheetName, String tableName, int firstRow, int lastRow, int columnIndex, int nColumns) {
            super(sheetName, tableName);
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.columnIndex = columnIndex;
            this.nColumns = nColumns;
        }

        @Override
        public int getTopBoundary() {
            return firstRow;
        }

        @Override
        public int getBottomBoundary() {
            return lastRow;
        }

        public int getFirstRow() {
            return firstRow;
        }

        public void setFirstRow(int firstRow) {
            this.firstRow = firstRow;
        }

        public int getLastRow() {
            return lastRow;
        }

        public void setLastRow(int lastRow) {
            this.lastRow = lastRow;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public void setColumnIndex(int columnIndex) {
            this.columnIndex = columnIndex;
        }

        public int getnColumns() {
            return nColumns;
        }

        public void setnColumns(int nColumns) {
            this.nColumns = nColumns;
        }
    }

    public static class FillUp extends FillVertically {
        public FillUp() {
        }

        public FillUp(String sheetName, String tableName, int rowIndex, int columnIndex, int nRows) {
            super(sheetName, tableName, rowIndex, columnIndex, nRows);
        }

        public FillUp(String sheetName, String tableName, int rowIndex, int firstColumn, int lastColumn, int nRows) {
            super(sheetName, tableName, rowIndex, firstColumn, lastColumn, nRows);
        }

        @Override
        public int getTopBoundary() {
            return getBottomBoundary() - getnRows();
        }

        @Override
        public int getBottomBoundary() {
            return getRowIndex();
        }

    }

    public static class FillRight extends FillHorizontally {
        public FillRight() {
        }

        public FillRight(String sheetName, String tableName, int rowIndex, int columnIndex, int nColumns) {
            super(sheetName, tableName, rowIndex, columnIndex, nColumns);
        }

        public FillRight(String sheetName, String tableName, int firstRow, int lastRow, int columnIndex, int nColumns) {
            super(sheetName, tableName, firstRow, lastRow, columnIndex, nColumns);
        }

        @Override
        public int getLeftBoundary() {
            return getColumnIndex();
        }

        @Override
        public int getRightBoundary() {
            return getLeftBoundary() + getnColumns();
        }

    }

    public static class FillDown extends FillVertically {
        public FillDown() {
        }

        public FillDown(String sheetName, String tableName, int rowIndex, int columnIndex, int nRows) {
            super(sheetName, tableName, rowIndex, columnIndex, nRows);
        }

        public FillDown(String sheetName, String tableName, int rowIndex, int firstColumn, int lastColumn, int nRows) {
            super(sheetName, tableName, rowIndex, firstColumn, lastColumn, nRows);
        }

        @Override
        public int getTopBoundary() {
            return getRowIndex();
        }

        @Override
        public int getBottomBoundary() {
            return getTopBoundary() + getnRows();
        }

    }

    public static class FillLeft extends FillHorizontally {
        public FillLeft() {
        }

        public FillLeft(String sheetName, String tableName, int rowIndex, int columnIndex, int nColumns) {
            super(sheetName, tableName, rowIndex, columnIndex, nColumns);
        }

        public FillLeft(String sheetName, String tableName, int firstRow, int lastRow, int columnIndex, int nColumns) {
            super(sheetName, tableName, firstRow, lastRow, columnIndex, nColumns);
        }

        @Override
        public int getLeftBoundary() {
            return getRightBoundary() - getnColumns();
        }

        @Override
        public int getRightBoundary() {
            return getColumnIndex();
        }

    }
}
