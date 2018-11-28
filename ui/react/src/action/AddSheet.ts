import ActionMeta from "./ActionMeta";
import Action from "./Action";

class AddSheet extends ActionMeta {
    public sheetName: string;
    public index: number;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const index: number = input.index;
        return new AddSheet(sheetName, index);
    }

    constructor(sheetName: string, index: number) {
        super();
        this.sheetName = sheetName;
        this.index = index;
    }

    public wrapper(): Action {
        const action = new Action();
        action.addSheet = this;
        return action;
    }

}

export default AddSheet;
