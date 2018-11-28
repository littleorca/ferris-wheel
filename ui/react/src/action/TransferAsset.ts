import ActionMeta from "./ActionMeta";
import Action from "./Action";

class TransferAsset extends ActionMeta {
    public fromSheetName: string;
    public fromAssetName: string;
    public toSheetName: string;
    public toAssetName: string;

    public static deserialize(input: any) {
        const fromSheetName: string = input.fromSheetName;
        const fromAssetName: string = input.fromAssetName;
        const toSheetName: string = input.toSheetName;
        const toAssetName: string = input.toAssetName;
        return new TransferAsset(fromSheetName, fromAssetName, toSheetName, toAssetName);
    }

    constructor(
        fromSheetName: string,
        fromAssetName: string,
        toSheetName: string,
        toAssetName: string) {

        super();
        this.fromSheetName = fromSheetName;
        this.fromAssetName = fromAssetName;
        this.toSheetName = toSheetName;
        this.toAssetName = toAssetName;
    }

    public wrapper(): Action {
        const action = new Action();
        action.transferAsset = this;
        return action;
    }

}

export default TransferAsset;
