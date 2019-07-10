import Header from "./Header";
import GridCell from "./GridCell";
import Rectangle from "./Rectangle";
import CellPosition from "./CellPosition";

interface GridData<T> extends Rectangle {

    getDefaultColumnWidth(): number;

    setDefaultColumnWidth(width: number): void;

    getDefaultRowHeight(): number;

    setDefaultRowHeight(height: number): void;

    getColumnCount(): number;

    getRowCount(): number;

    getColumnHeader(columnIndex: number): Header;

    getFirstColumnIndex(): number;

    getFirstColumnHeader(): Header;

    getLastColumnIndex(): number;

    getLastColumnHeader(): Header;

    getRowHeader(rowIndex: number): Header;

    getFirstRowIndex(): number;

    getFirstRowHeader(): Header;

    getLastRowIndex(): number;

    getLastRowHeader(): Header;

    forEachRow(callbackfn: (rowHeader: Header, index: number) => void): void;

    mapRow<U>(callbackfn: (rowHeader: Header, index: number) => U): U[];

    forEachColumn(callbackfn: (columnHeader: Header, index: number) => void): void;

    mapColumn<U>(callbackfn: (columnHeader: Header, index: number) => U): U[];

    getCellData(rowIndex: number, columnIndex: number): GridCell<T>;

    getCellValue(rowIndex: number, columnIndex: number): T | undefined;

    setCellValue(rowIndex: number, columnIndex: number, value: T): void;

    isArranged(): boolean;

    setArranged(arranged: boolean): void;

    getCellOffsetRect(rowIndex: number, columnIndex: number): Rectangle;

    getCellPosByCoordinate(x: number, y: number): CellPosition | null;

    getNearestCellPosByCoordinate(x: number, y: number): CellPosition;

    getColumnPosByCoordinate(x: number): number;

    getRowPosByCoordinate(y: number): number;

    addRows(rowIndex: number, rowCount?: number/* = 1*/): void;

    eraseRows(rowIndex: number, rowCount?: number/* = 1*/): void;

    removeRows(rowIndex: number, rowCount?: number/* = 1*/): void;

    addColumns(columnIndex: number, columnCount?: number/* = 1*/): void;

    eraseColumns(columnIndex: number, columnCount?: number/* = 1*/): void;

    removeColumns(columnIndex: number, columnCount?: number/* = 1*/): void;

    toCsv(topRow: number,
        rightColumn: number,
        bottomRow: number,
        leftColumn: number,
        getString: (gridCell: GridCell<T>) => string): string;

    toHtml(topRow: number,
        rightColumn: number,
        bottomRow: number,
        leftColumn: number,
        getString: (gridCell: GridCell<T>) => string): string;
}

export default GridData;
