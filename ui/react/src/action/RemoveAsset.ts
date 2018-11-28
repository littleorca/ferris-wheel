import SheetAction from "./SheetAction";
import Action from "./Action";

class RemoveAsset extends SheetAction {
    public assetName: string;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const assetName: string = input.assetName;
        return new RemoveAsset(sheetName, assetName);
    }

    constructor(sheetName: string, assetName: string) {
        super(sheetName);
        this.assetName = assetName;
    }

    public wrapper(): Action {
        const action = new Action();
        action.removeAsset = this;
        return action;
    }

}

export default RemoveAsset;
