import TableAutomaton from '../model/TableAutomaton';
import TableAction from './TableAction';
import Action from './Action';

class AutomateTable extends TableAction {
    public automaton: TableAutomaton;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const automaton: TableAutomaton = TableAutomaton.deserialize(input.automaton);
        return new AutomateTable(sheetName, tableName, automaton);
    }

    constructor(sheetName: string, tableName: string, automaton: TableAutomaton) {
        super(sheetName, tableName);
        this.automaton = automaton;
    }

    public wrapper(): Action {
        const action = new Action();
        action.automateTable = this;
        return action;
    }

}

export default AutomateTable;
