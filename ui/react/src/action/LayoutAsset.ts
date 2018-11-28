import Layout from "../model/Layout";
import SheetAction from "./SheetAction";
import Action from "./Action";

class LayoutAsset extends SheetAction {
    public assetName: string;
    public layout: Layout;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const assetName: string = input.assetName;
        const layout = Layout.deserialize(input.layout);
        return new LayoutAsset(sheetName, assetName, layout);
    }

    constructor(sheetName: string, assetName: string, layout: Layout) {
        super(sheetName);
        this.assetName = assetName;
        this.layout = layout;
    }

    public isAssetAction(): boolean {
        return true;
    }

    public targetAsset(): string {
        return this.assetName;
    }

    public wrapper(): Action {
        const action = new Action();
        action.layoutAsset = this;
        return action;
    }

}

export default LayoutAsset;
