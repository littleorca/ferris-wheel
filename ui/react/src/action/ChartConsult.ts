import SheetAction from "./SheetAction";
import Action from "./Action";

class ChartConsult extends SheetAction {
    public tableName: string;
    public type: string;
    public left: number;
    public top: number;
    public right: number;
    public bottom: number;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const type: string = input.type;
        const left: number = input.left;
        const top: number = input.top;
        const right: number = input.right;
        const bottom: number = input.bottom;
        return new ChartConsult(sheetName, tableName, type, left, top, right, bottom);
    }

    constructor(
        sheetName: string,
        tableName: string,
        type: string,
        left: number,
        top: number,
        right: number,
        bottom: number) {

        super(sheetName);
        this.sheetName = sheetName;
        this.tableName = tableName;
        this.type = type;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public wrapper(): Action {
        const action = new Action();
        action.chartConsult = this;
        return action;
    }

}

export default ChartConsult;
