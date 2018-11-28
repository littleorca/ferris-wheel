import ActionMeta from "./ActionMeta";

abstract class SheetAction extends ActionMeta {
    public sheetName: string;

    constructor(sheetName: string) {
        super();
        this.sheetName = sheetName;
    }

    public isSheetAction(): boolean {
        return true;
    }

    public targetSheet(): string {
        return this.sheetName;
    }
}

export default SheetAction;
