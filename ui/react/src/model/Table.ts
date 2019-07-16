import Header from "./Header";
import Row from "./Row";
import TableAutomaton from "./TableAutomaton";
import Layout from "./Layout";
import GridDataImpl from "./GridDataImpl";
import Cell from "./Cell";
import GridCell from "./GridCell";
import GridCellImpl from "./GridCellImpl";
import { VariantType } from "./Variant";

class Table extends GridDataImpl<Cell> {
    public name: string;
    public automaton: TableAutomaton;
    public layout: Layout;

    public static deserialize(input: any): Table {
        if (input.hasOwnProperty("toJSON") && typeof input.toJSON === "function") {
            input = input.toJSON();
        }
        const name = input.name;
        const rowHeaders: Header[] = [];
        if (Array.isArray(input.rowHeaders)) {
            input.rowHeaders.forEach((h: any) => rowHeaders.push(Header.deserialize(h)));
        }
        const columnHeaders: Header[] = [];
        if (Array.isArray(input.columnHeaders)) {
            input.columnHeaders.forEach((h: any) => columnHeaders.push(Header.deserialize(h)));
        }

        const rows: Array<Array<GridCell<Cell>>> = [];
        rowHeaders.forEach((rowHeader, rowIndex) => {
            rows[rowIndex] = [];
            columnHeaders.forEach((columnHeader, columnIndex) => {
                rows[rowIndex][columnIndex] = new GridCellImpl<Cell>();
            });
        });
        if (Array.isArray(input.rows)) {
            input.rows.forEach((r: any) => {
                const row = Row.deserialize(r);
                row.cells.forEach(c => {
                    const gridCell = rows[row.rowIndex][c.columnIndex];
                    gridCell.setData(c);
                    if (c.value.valueType() === VariantType.DECIMAL) {
                        gridCell.setAlign("right");
                    }
                });
            });
        }

        const automaton = typeof input.automaton !== 'undefined' ?
            TableAutomaton.deserialize(input.automaton) : undefined;
        const layout = typeof input.layout !== 'undefined' ?
            Layout.deserialize(input.layout) : undefined;
        return new Table(name, rows, columnHeaders, rowHeaders, automaton, layout);
    }

    constructor(
        name: string,
        rows: Array<Array<GridCell<Cell>>> = [],
        columnHeaders?: Header[],
        rowHeaders?: Header[],
        automaton: TableAutomaton = new TableAutomaton(),
        layout: Layout = new Layout()) {

        super(rows, columnHeaders, rowHeaders);

        this.name = name;
        this.automaton = automaton;
        this.layout = layout;
    }

    public clone() {
        return Table.deserialize(this.toJSON());
    }

    public toJSON() {
        const tmpObject = {
            name: this.name,
            rows: new Array(),
            automaton: this.automaton,
            layout: this.layout,
            columnHeaders: this.getColumnHeaders(),
            rowHeaders: this.getRowHeaders(),
        };
        this.forEachRow((rowHeader, rowIndex) => {
            const row = new Row(rowIndex, []);
            this.forEachColumn((columnHeader, columnIndex) => {
                const cell = this.getCellValue(rowIndex, columnIndex);
                if (typeof cell !== "undefined") {
                    cell.columnIndex = columnIndex;
                    row.cells.push(cell);
                }
            });
            if (row.cells.length > 0) {
                tmpObject.rows.push(row);
            }
        });
        return tmpObject;
    }
}

export default Table;
