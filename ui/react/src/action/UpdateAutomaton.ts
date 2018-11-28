import TableAutomaton from "../model/TableAutomaton";
import TableAction from "./TableAction";
import Action from "./Action";

class UpdateAutomaton extends TableAction {
    public automaton: TableAutomaton;

    public static deserialize(input: any) {
        const sheetName: string = input.sheetName;
        const tableName: string = input.tableName;
        const automaton: TableAutomaton = TableAutomaton.deserialize(input.automaton);
        return new UpdateAutomaton(sheetName, tableName, automaton);
    }

    constructor(
        sheetName: string,
        tableName: string,
        automaton: TableAutomaton) {

        super(sheetName, tableName);
        this.automaton = automaton;
    }

    public wrapper(): Action {
        const action = new Action();
        action.updateAutomaton = this;
        return action;
    }

}

export default UpdateAutomaton;
