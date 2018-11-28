import TableAction from "./TableAction";
import Action from "./Action";

class InsertRows extends TableAction {
    public rowIndex: number;
    public nRows: number;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const rowIndex: number = input.rowIndex;
        const nRows: number = input.nRows;
        return new InsertRows(
            sheetName,
            tableName,
            rowIndex,
            nRows
        );
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
        action.insertRows = this;
        return action;
    }

}

export default InsertRows;
