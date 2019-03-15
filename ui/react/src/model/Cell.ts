import UnionValue from "./UnionValue";
import Values from "./Values";

class Cell {
    public columnIndex: number;
    public value: UnionValue;
    public format: string;

    public static deserialize(input: any): Cell {
        const columnIndex = input.columnIndex;
        const value = Values.deserialize(input.value);
        const format = typeof input.format === "string" ? input.format : "";
        return new Cell(columnIndex, value, format);
    }

    constructor(
        columnIndex: number = 0,
        value: UnionValue = Values.blank(),
        format: string = "") {

        this.columnIndex = columnIndex;
        this.value = value;
        this.format = format;
    }
}

export default Cell;
