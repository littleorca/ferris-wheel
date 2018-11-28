import ActionMeta from "./ActionMeta";
import Action from "./Action";

class RenameSheet extends ActionMeta {
    public oldSheetName: string;
    public newSheetName: string;

    public static deserialize(input: any) {
        const oldSheetName: string = input.oldSheetName;
        const newSheetName: string = input.newSheetName;
        return new RenameSheet(oldSheetName, newSheetName);
    }

    constructor(oldSheetName: string, newSheetName: string) {
        super();
        this.oldSheetName = oldSheetName;
        this.newSheetName = newSheetName;
    }

    public wrapper(): Action {
        const action = new Action();
        action.renameSheet = this;
        return action;
    }

}

export default RenameSheet;
