import UnionValue from "./UnionValue";
import Values from "./Values";

class Cell {
    public columnIndex: number;
    public value: UnionValue;

    public static deserialize(input: any): Cell {
        const columnIndex = input.columnIndex;
        const value = Values.deserialize(input.value);
        return new Cell(columnIndex, value);
    }

    constructor(
        columnIndex: number = 0,
        value: UnionValue = Values.blank()) {

        this.columnIndex = columnIndex;
        this.value = value;
    }
}

export default Cell;
