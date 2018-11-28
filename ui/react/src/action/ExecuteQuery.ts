import NamedValue from "../model/NamedValue";
import TableAction from "./TableAction";
import Action from "./Action";

class ExecuteQuery extends TableAction {
    public params: NamedValue[];

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const params = [];
        if (typeof input.params !== 'undefined') {
            for (const param of input.params) {
                params.push(NamedValue.deserialize(param));
            }
        }
        return new ExecuteQuery(sheetName, tableName, params);
    }

    constructor(sheetName: string, tableName: string, params: NamedValue[]) {
        super(sheetName, tableName);
        this.params = params;
    }

    public wrapper(): Action {
        const action = new Action();
        action.executeQuery = this;
        return action;
    }

}

export default ExecuteQuery;
