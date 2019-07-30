import Span from "./Span";

class Grid {
    public columns: number;
    public rows: number;
    public column: Span;
    public row: Span;

    public static deserialize(input: any): Grid {
        const columns = input.columns;
        const rows = input.rows;
        const column = typeof input.column !== 'undefined' ?
            Span.deserialize(input.column) : undefined;
        const row = typeof input.row !== 'undefined' ?
            Span.deserialize(input.row) : undefined;
        return new Grid(columns, rows, column, row);
    }

    constructor(
        columns: number = 0,
        rows: number = 0,
        column: Span = new Span(),
        row: Span = new Span()) {

        this.columns = columns;
        this.rows = rows;
        this.column = column;
        this.row = row;
    }

    public clone() {
        return new Grid(this.columns,
            this.rows,
            this.column.clone(),
            this.row.clone());
    }
}

export default Grid;
