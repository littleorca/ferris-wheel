import Action from "./Action";
import TableAction from "./TableAction";

class SetCellsFormat extends TableAction {
    public rowIndex: number;
    public columnIndex: number;
    public nRows: number;
    public nColumns: number;
    public format: string;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const rowIndex: number = input.rowIndex;
        const columnIndex: number = input.columnIndex;
        const nRows: number = input.nRows;
        const nColumns: number = input.nColumns;
        const format: string = input.format;
        return new SetCellsFormat(
            sheetName,
            tableName,
            rowIndex,
            columnIndex,
            nRows,
            nColumns,
            format
        );
    }

    constructor(
        sheetName: string,
        tableName: string,
        rowIndex: number,
        columnIndex: number,
        nRows: number,
        nColumns: number,
        format: string
    ) {
        super(sheetName, tableName);
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.nRows = nRows;
        this.nColumns = nColumns;
        this.format = format;
    }

    public wrapper(): Action {
        const action = new Action();
        action.setCellsFormat = this;
        return action;
    }
}

export default SetCellsFormat;
