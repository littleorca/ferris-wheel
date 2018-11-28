import TableAction from "./TableAction";
import Action from "./Action";

class EraseRows extends TableAction {
    public rowIndex: number;
    public nRows: number;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const rowIndex: number = input.rowIndex;
        const nRows: number = input.nRows;
        return new EraseRows(sheetName, tableName, rowIndex, nRows);
    }

    constructor(
        sheetName: string,
        tableName: string,
        rowIndex: number,
        nRows: number) {

        super(sheetName, tableName);
        this.rowIndex = rowIndex;
        this.nRows = nRows;
    }

    public wrapper(): Action {
        const action = new Action();
        action.eraseRows = this;
        return action;
    }

}

export default EraseRows;
