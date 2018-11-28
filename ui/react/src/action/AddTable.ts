import Table from '../model/Table';
import SheetAction from './SheetAction';
import Action from './Action';

class AddTable extends SheetAction {
    public table: Table;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const table: Table = Table.deserialize(input.table);
        return new AddTable(sheetName, table);
    }

    constructor(sheetName: string, table: Table) {
        super(sheetName);
        this.table = table;
    }

    public wrapper(): Action {
        const action = new Action();
        action.addTable = this;
        return action;
    }

}

export default AddTable;
