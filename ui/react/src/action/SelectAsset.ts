import Sheet from "../model/Sheet";
import SheetAsset from "../model/SheetAsset";
import ActionMeta from "./ActionMeta";
import Action from "./Action";

class SelectAsset extends ActionMeta {
    public sheet?: Sheet;
    public asset?: SheetAsset;

    public static deserialize(input: any) {
        if (typeof input === 'undefined') {
            return new SelectAsset();
        }
        const sheet = typeof input.sheet !== 'undefined' ?
            Sheet.deserialize(input.sheet) : undefined;
        const asset = typeof input.asset !== 'undefined' ?
            SheetAsset.deserialize(input.asset) : undefined;
        return new SelectAsset(sheet, asset);
    }

    constructor(sheet?: Sheet, asset?: SheetAsset) {
        super();
        this.sheet = sheet;
        this.asset = asset;
    }

    public isLocalAction(): boolean {
        return true;
    }

    public wrapper(): Action {
        const action = new Action();
        action.selectAsset = this;
        return action;
    }

}

export default SelectAsset;
