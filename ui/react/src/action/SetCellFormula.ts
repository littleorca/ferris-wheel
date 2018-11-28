import TableAction from "./TableAction";
import Action from "./Action";

class SetCellFormula extends TableAction {
    public rowIndex: number;
    public columnIndex: number;
    public formulaString: string;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const rowIndex: number = input.rowIndex;
        const columnIndex: number = input.columnIndex;
        const formulaString: string = input.formulaString;
        return new SetCellFormula(sheetName, tableName, rowIndex, columnIndex, formulaString)
    }

    constructor(
        sheetName: string,
        tableName: string,
        rowIndex: number,
        columnIndex: number,
        formulaString: string) {

        super(sheetName, tableName);
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.formulaString = formulaString;
    }

    public wrapper(): Action {
        const action = new Action();
        action.setCellFormula = this;
        return action;
    }

}

export default SetCellFormula;
