import TableAction from "./TableAction";
import Action from "./Action";

class FillUp extends TableAction {
    public rowIndex: number;
    public firstColumn: number;
    public lastColumn: number;
    public nRows: number;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const rowIndex: number = input.rowIndex;
        const firstColumn: number = input.firstColumn;
        const lastColumn: number = input.lastColumn;
        const nRows: number = input.nRows;
        return new FillUp(sheetName, tableName, rowIndex, firstColumn, lastColumn, nRows);
    }

    constructor(
        sheetName: string,
        tableName: string,
        rowIndex: number,
        firstColumn: number,
        lastColumn: number,
        nRows: number) {

        super(sheetName, tableName);
        this.rowIndex = rowIndex;
        this.firstColumn = firstColumn;
        this.lastColumn = lastColumn;
        this.nRows = nRows;
    }

    public wrapper(): Action {
        const action = new Action();
        action.fillUp = this;
        return action;
    }

}

export default FillUp;
