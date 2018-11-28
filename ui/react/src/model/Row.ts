import Cell from "./Cell";

class Row {
    public rowIndex: number;
    public cells: Cell[];

    public static deserialize(input: any): Row {
        const rowIndex = input.rowIndex;
        const cells = [];
        if (typeof input.cells !== 'undefined') {
            for (const cell of input.cells) {
                cells.push(Cell.deserialize(cell));
            }
        }
        return new Row(rowIndex, cells);
    }

    constructor(
        rowIndex: number = 0,
        cells: Cell[] = []) {

        this.rowIndex = rowIndex;
        this.cells = cells;
    }
}

export default Row;
