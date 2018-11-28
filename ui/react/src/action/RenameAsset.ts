import SheetAction from "./SheetAction";
import Action from "./Action";

class RenameAsset extends SheetAction {
    public oldAssetName: string;
    public newAssetName: string;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const oldAssetName: string = input.oldAssetName;
        const newAssetName: string = input.newAssetName;
        return new RenameAsset(sheetName, oldAssetName, newAssetName);
    }

    constructor(
        sheetName: string,
        oldAssetName: string,
        newAssetName: string) {

        super(sheetName);
        this.oldAssetName = oldAssetName;
        this.newAssetName = newAssetName;
    }

    public isAssetAction() {
        return true;
    }

    public targetAsset() {
        return this.oldAssetName;
    }

    public wrapper(): Action {
        const action = new Action();
        action.renameAsset = this;
        return action;
    }

}

export default RenameAsset;
