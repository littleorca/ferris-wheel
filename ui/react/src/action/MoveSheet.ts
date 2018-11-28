import ActionMeta from "./ActionMeta";
import Action from "./Action";

class MoveSheet extends ActionMeta {
    public sheetName: string;
    public targetIndex: number;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const targetIndex: number = input.targetIndex;
        return new MoveSheet(sheetName, targetIndex);
    }

    constructor(sheetName: string, targetIndex: number) {
        super();
        this.sheetName = sheetName;
        this.targetIndex = targetIndex;
    }

    public wrapper(): Action {
        const action = new Action();
        action.moveSheet = this;
        return action;
    }

}

export default MoveSheet;
