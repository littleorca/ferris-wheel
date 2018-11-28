import UnionValue from "../model/UnionValue";
import Values from "../model/Values";
import TableAction from "./TableAction";
import Action from "./Action";

class SetCellValue extends TableAction {
    public rowIndex: number;
    public columnIndex: number;
    public value: UnionValue;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const rowIndex: number = input.rowIndex;
        const columnIndex: number = input.columnIndex;
        const value: UnionValue = Values.deserialize(input.value);
        return new SetCellValue(sheetName, tableName, rowIndex, columnIndex, value);
    }

    constructor(sheetName: string,
        tableName: string,
        rowIndex: number,
        columnIndex: number,
        value: UnionValue) {

        super(sheetName, tableName);
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.value = value;
    }

    public wrapper(): Action {
        const action = new Action();
        action.setCellValue = this;
        return action;
    }

}

export default SetCellValue;
