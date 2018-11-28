import QueryAutomaton from "./QueryAutomaton";
import PivotAutomaton from "./PivotAutomaton";

class TableAutomaton {
    public queryAutomaton?: QueryAutomaton;
    public pivotAutomaton?: PivotAutomaton;

    public static deserialize(input: any): TableAutomaton {
        const queryAutomaton = typeof input.queryAutomaton !== 'undefined' ?
            QueryAutomaton.deserialize(input.queryAutomaton) : undefined;
        const pivotAutomaton = typeof input.pivotAutomaton !== 'undefined' ?
            PivotAutomaton.deserialize(input.pivotAutomaton) : undefined;
        return new TableAutomaton(queryAutomaton, pivotAutomaton);
    }

    constructor(
        queryAutomaton?: QueryAutomaton,
        pivotAutomaton?: PivotAutomaton) {

        this.queryAutomaton = queryAutomaton;
        this.pivotAutomaton = pivotAutomaton;
    }
}

export default TableAutomaton;
