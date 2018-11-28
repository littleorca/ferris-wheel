import ActionMeta from "./ActionMeta";
import Action from "./Action";

class RemoveSheet extends ActionMeta {
    public sheetName: string;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        return new RemoveSheet(sheetName);
    }

    constructor(sheetName: string) {
        super();
        this.sheetName = sheetName;
    }

    public wrapper(): Action {
        const action = new Action();
        action.removeSheet = this;
        return action;
    }
}

export default RemoveSheet;
