import SheetAction from "./SheetAction";

abstract class TableAction extends SheetAction {
    public tableName: string;

    constructor(sheetName: string, tableName: string) {
        super(sheetName);
        this.tableName = tableName;
    }

    public isAssetAction(): boolean {
        return true;
    }

    public targetAsset(): string {
        return this.tableName;
    }
}

export default TableAction;
