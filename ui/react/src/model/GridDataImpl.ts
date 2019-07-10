import Rectangle from "./Rectangle";
import GridCell from "./GridCell";
import Header from "./Header";
import CellPosition from "./CellPosition";
import RectangleImpl from "./RectangleImpl";
import EscapeHelper from "../util/EscapeHelper";
import GridCellImpl from "./GridCellImpl";
import GridData from "./GridData";

class GridDataImpl<T> extends RectangleImpl implements GridData<T> {
    private defaultColumnWidth: number = 100;
    private defaultRowHeaderWidth: number = 46;
    private defaultRowHeight: number = 25;
    private columnHeaders: Header[];
    private rowHeaders: Header[];
    private rows: GridCell<T>[][];
    private arranged: boolean;

    constructor(rows: GridCell<T>[][] = [],
        columnHeaders?: Header[],
        rowHeaders?: Header[],
        left: number = 0,
        top: number = 0,
        width: number = 0,
        height: number = 0) {

        super(left, top, width, height);

        if (typeof rowHeaders === "undefined") {
            rowHeaders = rows.map((r, i) => new Header());
        }
        if (typeof columnHeaders === "undefined") {
            columnHeaders = [];
            let columnCount = 0;
            rows.map(r => {
                if (r.length > columnCount) {
                    columnCount = r.length;
                }
            });
            for (let i = 0; i < columnCount; i++) {
                columnHeaders.push(new Header());
            }
        }

        this.columnHeaders = columnHeaders;
        this.rowHeaders = rowHeaders;
        this.rows = rows;

        this.arranged = false;
    }

    public getDefaultColumnWidth() {
        return this.defaultColumnWidth;
    }

    public setDefaultColumnWidth(width: number) {
        this.defaultColumnWidth = width;
    }

    public getDefaultRowHeight() {
        return this.defaultRowHeight;
    }

    public setDefaultRowHeight(height: number) {
        this.defaultRowHeight = height;
    }

    public getColumnHeader(columnIndex: number): Header {
        this.assertColumnIndex(columnIndex);
        return this.columnHeaders[columnIndex];
    }

    public getFirstColumnIndex() {
        if (!this.isValidColumnIndex(0)) {
            throw new Error("No column.");
        }
        return 0;
    }

    public getFirstColumnHeader() {
        return this.getColumnHeader(this.getFirstColumnIndex());
    }

    public getLastColumnIndex() {
        const index = this.getColumnCount() - 1;
        if (!this.isValidColumnIndex(index)) {
            throw new Error("No column.");
        }
        return index;
    }

    public getLastColumnHeader() {
        return this.getColumnHeader(this.getLastColumnIndex());
    }

    public forEachColumn(callbackfn: (columnHeader: Header, index: number) => void) {
        this.columnHeaders.forEach(callbackfn);
    }

    public mapColumn<U>(callbackfn: (columnHeader: Header, index: number) => U): U[] {
        return this.columnHeaders.map<U>(callbackfn);
    }

    protected getColumnHeaders() {
        return this.columnHeaders;
    }

    public getRowHeader(rowIndex: number): Header {
        this.assertRowIndex(rowIndex);
        return this.rowHeaders[rowIndex];
    }

    public getFirstRowIndex() {
        if (!this.isValidRowIndex(0)) {
            throw new Error("No row.");
        }
        return 0;
    }

    public getFirstRowHeader() {
        return this.getRowHeader(this.getFirstRowIndex());
    }

    public getLastRowIndex() {
        const index = this.getRowCount() - 1;
        if (!this.isValidRowIndex(index)) {
            throw new Error("No row.");
        }
        return index;
    }

    public getLastRowHeader() {
        return this.getRowHeader(this.getLastRowIndex());
    }

    public forEachRow(callbackfn: (rowHeader: Header, index: number) => void) {
        this.rowHeaders.forEach(callbackfn);
    }

    public mapRow<U>(callbackfn: (rowHeader: Header, index: number) => U): U[] {
        return this.rowHeaders.map<U>(callbackfn);
    }

    protected getRowHeaders() {
        return this.rowHeaders;
    }

    public getCellData(rowIndex: number, columnIndex: number): GridCell<T> {
        this.assertRowIndex(rowIndex);
        this.assertColumnIndex(columnIndex);
        return this.rows[rowIndex][columnIndex];
    }

    public getCellValue(rowIndex: number, columnIndex: number): T | undefined {
        const cellData = this.getCellData(rowIndex, columnIndex);
        return cellData.getData();
    }

    public setCellValue(rowIndex: number, columnIndex: number, value: T) {
        const cellData = this.getCellData(rowIndex, columnIndex);
        cellData.setData(value);
    }

    public getCellOffsetRect(rowIndex: number, columnIndex: number): Rectangle {
        const rowHeader = this.getRowHeader(rowIndex);
        const columnHeader = this.getColumnHeader(columnIndex);
        return new RectangleImpl(
            this.getLeft() + columnHeader.getLeft(),
            this.getTop() + rowHeader.getTop(),
            columnHeader.getWidth(),
            rowHeader.getHeight(),
        );
    }

    public getColumnCount() {
        return this.columnHeaders.length;
    }

    public getRowCount() {
        return this.rowHeaders.length;
    }

    public isArranged(): boolean {
        return this.arranged;
    }

    public setArranged(arranged: boolean) {
        this.arranged = arranged;
    }

    public assertColumnIndex(...columnIndices: number[]) {
        if (!this.isValidColumnIndex(...columnIndices)) {
            throw new Error("Column index out of range.");
        }
    }

    public isValidColumnIndex(...columnIndices: number[]) {
        let valid = true;
        columnIndices.forEach(i => {
            valid = valid &&
                !isNaN(i) &&
                i >= 0 &&
                i < this.getColumnCount();
        });
        return valid;
    }

    public assertRowIndex(...rowIndices: number[]) {
        if (!this.isValidRowIndex(...rowIndices)) {
            throw new Error("Row index out of range.");
        }
    }

    public isValidRowIndex(...rowIndices: number[]) {
        let valid = true;
        rowIndices.forEach(i => {
            valid = valid &&
                !isNaN(i) &&
                i >= 0 &&
                i < this.getRowCount();
        });
        return valid;
    }

    public getCellPosByCoordinate(x: number, y: number): CellPosition | null {
        const columnIndex = this.getColumnPosByCoordinate(x);
        const rowIndex = this.getRowPosByCoordinate(y);
        if (!this.isValidColumnIndex(columnIndex) || !this.isValidRowIndex(rowIndex)) {
            return null;
        }
        return new CellPosition(rowIndex, columnIndex);
    }

    public getNearestCellPosByCoordinate(x: number, y: number): CellPosition {
        let columnIndex = this.getColumnPosByCoordinate(x);
        let rowIndex = this.getRowPosByCoordinate(y);

        if (columnIndex >= this.columnHeaders.length) {
            columnIndex = this.columnHeaders.length - 1;
        }
        if (columnIndex < 0) {
            columnIndex = 0;
        }

        if (rowIndex >= this.rowHeaders.length) {
            rowIndex = this.rowHeaders.length - 1;
        }
        if (rowIndex < 0) {
            rowIndex = 0;
        }

        return new CellPosition(rowIndex, columnIndex);
    }

    public getColumnPosByCoordinate(x: number): number {
        x -= this.getLeft();
        let columnPos = Number.NEGATIVE_INFINITY;

        if (x >= 0) {
            for (let i = 0; i < this.columnHeaders.length; i++) {
                const columnHeader = this.columnHeaders[i];
                if (columnHeader.isXInside(x)) {
                    columnPos = i;
                    break;
                }
            }
            if (columnPos === Number.NEGATIVE_INFINITY) {
                columnPos = Number.POSITIVE_INFINITY;
            }
        }

        return columnPos;
    }

    public getRowPosByCoordinate(y: number): number {
        y -= this.getTop();
        let rowPos = Number.NEGATIVE_INFINITY;

        if (y >= 0) {
            for (let i = 0; i < this.rowHeaders.length; i++) {
                const rowHeader = this.rowHeaders[i];
                if (rowHeader.isYInside(y)) {
                    rowPos = i;
                    break;
                }
            }
            if (rowPos === Number.NEGATIVE_INFINITY) {
                rowPos = Number.POSITIVE_INFINITY;
            }
        }

        return rowPos;
    }

    public addRows(rowIndex: number, rowCount: number = 1) {
        this.addRowHeaders(rowIndex, rowCount);

        const createEmptyRow = () => {
            const row: GridCell<T>[] = [];
            for (let i = 0; i < this.getColumnCount(); i++) {
                row.push(new GridCellImpl<T>());
            }
            return row;
        };

        if (rowIndex < this.rows.length) {
            const args: (number | GridCell<T>[])[] = [rowIndex, 0];
            for (let i = 0; i < rowCount; i++) {
                args.push(createEmptyRow());
            }
            Array.prototype.splice.apply(this.rows, args);

        } else {
            const endRowIndex = rowIndex + rowCount;
            for (let i = rowIndex; i < endRowIndex; i++) {
                this.rows[i] = createEmptyRow();
            }
        }
    }

    private addRowHeaders(rowIndex: number, rowCount: number) {
        if (rowIndex < 0 || rowIndex > this.rowHeaders.length || rowCount < 1) {
            throw new Error("Invalid argument(s)");
        }

        let width = this.defaultRowHeaderWidth,
            height = this.defaultRowHeight;
        let refRowHeader;

        if (rowIndex < this.getRowCount()) {
            refRowHeader = this.getRowHeader(rowIndex);
        } else if (this.getRowCount() > 0) {
            refRowHeader = this.getRowHeader(this.getRowCount() - 1);
        }
        if (refRowHeader) {
            if (refRowHeader.getWidth() > 0) {
                width = refRowHeader.getWidth();
            }
            if (refRowHeader.getHeight() > 0) {
                height = refRowHeader.getHeight();
            }
        }

        const args: (number | Header)[] = [rowIndex, 0];
        for (let i = 0; i < rowCount; i++) {
            args.push(new Header(undefined, undefined, width, height));
        }
        Array.prototype.splice.apply(this.rowHeaders, args);

        this.rearrangeRows(rowIndex);
    }

    public eraseRows(rowIndex: number, rowCount: number = 1) {
        const endRowIndex = rowIndex + rowCount - 1;
        this.assertRowIndex(rowIndex);
        this.assertRowIndex(endRowIndex);
        if (rowIndex > endRowIndex) {
            throw new Error("Illegal argument(s).");
        }
        for (let i = rowIndex; i < endRowIndex; i++) {
            const row = this.rows[i];
            if (!row) {
                continue;
            }
            row.forEach(cell => {
                if (cell) {
                    cell.setData(undefined);
                }
            });
        }
    }

    public removeRows(rowIndex: number, rowCount: number = 1) {
        const endRowIndex = rowIndex + rowCount - 1;
        this.assertRowIndex(rowIndex);
        this.assertRowIndex(endRowIndex);
        if (rowIndex > endRowIndex) {
            throw new Error("Illegal argument(s).");
        }

        this.rows.splice(rowIndex, rowCount);
        this.rowHeaders.splice(rowIndex, rowCount);

        this.rearrangeRows(rowIndex);
    }

    protected rearrangeRows(startRowIndex: number = 0) {
        let offset = (startRowIndex === 0) ? 0 :
            this.getRowHeader(startRowIndex - 1).getBottom();

        for (let i = startRowIndex; i < this.getRowCount(); i++) {
            const rowHeader = this.getRowHeader(i);
            rowHeader.moveToY(offset);
            offset = rowHeader.getBottom();
        }

        this.setHeight(offset);
    }

    public addColumns(columnIndex: number, columnCount: number = 1) {
        this.addColumnHeaders(columnIndex, columnCount);

        const endColumnIndex = columnIndex + columnCount;
        this.rows.forEach(row => {
            if (row.length >= columnIndex) {
                const args: (number | GridCell<T>)[] = [columnIndex, 0];
                for (let i = 0; i < columnCount; i++) {
                    args.push(new GridCellImpl<T>());
                }
                Array.prototype.splice.apply(row, args);

            } else {
                for (let i = columnIndex; i < endColumnIndex; i++) {
                    row[i] = new GridCellImpl<T>();
                }
            }
        });
    }

    private addColumnHeaders(columnIndex: number, columnCount: number) {
        if (columnIndex < 0 || columnIndex > this.columnHeaders.length || columnCount < 1) {
            throw new Error("Invalid argument(s)");
        }

        let width = this.defaultColumnWidth,
            height = this.defaultRowHeight;
        if (this.getColumnCount() > 0) {
            const refColumnHeader = this.getColumnHeader(0);
            if (refColumnHeader.getHeight() > 0) {
                height = refColumnHeader.getHeight();
            }
        }

        const args: (number | Header)[] = [columnIndex, 0];
        for (let i = 0; i < columnCount; i++) {
            args.push(new Header(undefined, undefined, width, height));
        }
        Array.prototype.splice.apply(this.columnHeaders, args);

        this.rearrangeColumns(columnIndex);
    }

    public eraseColumns(columnIndex: number, columnCount: number = 1) {
        const endColumnIndex = columnIndex + columnCount - 1;
        this.assertColumnIndex(columnIndex);
        this.assertColumnIndex(endColumnIndex);
        if (columnIndex > endColumnIndex) {
            throw new Error("Illegal argument(s).");
        }
        this.rows.forEach(row => {
            for (let i = columnIndex; i < endColumnIndex && i < row.length; i++) {
                const cell = row[i];
                cell.setData(undefined);
            }
        });
    }

    public removeColumns(columnIndex: number, columnCount: number = 1) {
        const endColumnIndex = columnIndex + columnCount - 1;
        this.assertColumnIndex(columnIndex);
        this.assertColumnIndex(endColumnIndex);
        if (columnIndex > endColumnIndex) {
            throw new Error("Illegal argument(s).");
        }

        this.columnHeaders.splice(columnIndex, columnCount);
        this.rows.forEach(row => {
            row.splice(columnIndex, columnCount);
        });

        this.rearrangeColumns(columnIndex);
    }

    protected rearrangeColumns(startColumnIndex: number = 0) {
        let offset = (startColumnIndex === 0) ? 0 :
            this.getColumnHeader(startColumnIndex - 1).getRight();

        for (let i = startColumnIndex; i < this.getColumnCount(); i++) {
            const columnHeader = this.getColumnHeader(i);
            columnHeader.moveToX(offset);
            offset = columnHeader.getRight();
        }

        this.setWidth(offset);
    }

    public toCsv(topRow: number,
        rightColumn: number,
        bottomRow: number,
        leftColumn: number,
        getString: (cellData: GridCell<T>) => string) {

        let csv = "";
        for (let rowIndex = topRow; rowIndex <= bottomRow; rowIndex++) {
            if (rowIndex > topRow) {
                csv += "\r\n";
            }

            let line = "";
            for (let columnIndex = leftColumn; columnIndex <= rightColumn; columnIndex++) {
                if (columnIndex > leftColumn) {
                    line += ",";
                }
                const value = getString(this.getCellData(rowIndex, columnIndex));
                line += EscapeHelper.escapeForCSV(value);
            }
            csv += line;
        }
        return csv;
    }

    public toHtml(topRow: number,
        rightColumn: number,
        bottomRow: number,
        leftColumn: number,
        getString: (cellData: GridCell<T>) => string) {

        let html = "<!doctype html><html><head>\
<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\
</head><body><table><tbody>\n";
        for (let rowIndex = topRow; rowIndex <= bottomRow; rowIndex++) {
            html += "<tr>";
            for (let columnIndex = leftColumn; columnIndex <= rightColumn; columnIndex++) {
                const value = getString(this.getCellData(rowIndex, columnIndex));
                html += "<td>" + EscapeHelper.escapeForHtml(value) + "</td>";
            }
            html += "</tr>\n";
        }
        html += "</tbody></table></body></html>";
        return html;
    }
}

export default GridDataImpl;
