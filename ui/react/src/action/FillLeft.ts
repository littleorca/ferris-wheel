import TableAction from "./TableAction";
import Action from "./Action";

class FillLeft extends TableAction {
    public firstRow: number;
    public lastRow: number;
    public columnIndex: number;
    public nColumns: number;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const firstRow: number = input.firstRow;
        const lastRow: number = input.lastRow;
        const columnIndex: number = input.columnIndex;
        const nColumns: number = input.nColumns;
        return new FillLeft(sheetName, tableName, firstRow, lastRow, columnIndex, nColumns);
    }

    constructor(
        sheetName: string,
        tableName: string,
        firstRow: number,
        lastRow: number,
        columnIndex: number,
        nColumns: number) {

        super(sheetName, tableName);
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.columnIndex = columnIndex;
        this.nColumns = nColumns;
    }

    public wrapper(): Action {
        const action = new Action();
        action.fillLeft = this;
        return action;
    }

}

export default FillLeft;
