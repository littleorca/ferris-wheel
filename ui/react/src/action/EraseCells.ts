import TableAction from "./TableAction";
import Action from "./Action";

class EraseCells extends TableAction {
    public top: number;
    public right: number;
    public bottom: number;
    public left: number;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const top: number = input.top;
        const right: number = input.right;
        const bottom: number = input.bottom;
        const left: number = input.left;
        return new EraseCells(sheetName, tableName, top, right, bottom, left);
    }

    constructor(
        sheetName: string,
        tableName: string,
        top: number,
        right: number,
        bottom: number,
        left: number) {

        super(sheetName, tableName);
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public wrapper(): Action {
        const action = new Action();
        action.eraseCells = this;
        return action;
    }

}

export default EraseCells;
