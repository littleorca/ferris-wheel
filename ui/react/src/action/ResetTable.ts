import SheetAction from "./SheetAction";
import Table from "../model/Table";

class ResetTable extends SheetAction {
    public table: Table;

    public static deserialize(input: any): ResetTable {
        const sheetName = input.sheetName;
        const table = Table.deserialize(input.table); // table cannot be empty!
        return new ResetTable(sheetName, table);
    }

    constructor(sheetName: string, table: Table) {
        super(sheetName);
        this.table = table;
    }

    public isAssetAction(): boolean {
        return true;
    }

    public targetAsset(): string | undefined {
        return this.table.name;
    }

}

export default ResetTable;