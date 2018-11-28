import TableAction from "./TableAction";
import Action from "./Action";

class RemoveColumns extends TableAction {
    public columnIndex: number;
    public nColumns: number;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const columnIndex: number = input.columnIndex;
        const nColumns: number = input.nColumns;
        return new RemoveColumns(
            sheetName,
            tableName,
            columnIndex,
            nColumns
        );
    }

    constructor(
        sheetName: string,
        tableName: string,
        columnIndex: number,
        nColumns: number) {

        super(sheetName, tableName);
        this.columnIndex = columnIndex;
        this.nColumns = nColumns;
    }

    public wrapper(): Action {
        const action = new Action();
        action.removeColumns = this;
        return action;
    }

}

export default RemoveColumns;
