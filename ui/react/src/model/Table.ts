import Row from "./Row";
import TableAutomaton from "./TableAutomaton";
import Layout from "./Layout";

class Table {
    public name: string;
    public rows: Row[];
    public automaton: TableAutomaton;
    public layout: Layout;

    public static deserialize(input: any): Table {
        const name = input.name;
        const rows = [];
        if (typeof input.rows !== 'undefined') {
            for (const row of input.rows) {
                rows.push(Row.deserialize(row));
            }
        }
        const automaton = typeof input.automaton !== 'undefined' ?
            TableAutomaton.deserialize(input.automaton) : undefined;
        const layout = typeof input.layout !== 'undefined' ?
            Layout.deserialize(input.layout) : undefined;
        return new Table(name, rows, automaton, layout);
    }

    constructor(
        name: string,
        rows: Row[] = [],
        automaton: TableAutomaton = new TableAutomaton(),
        layout: Layout = new Layout()) {

        this.name = name;
        this.rows = rows
        this.automaton = automaton;
        this.layout = layout;
    }

    // TODO review this!
    // for TableView to assign this method and implement real function.
    public getSelectedRange(): any {
        return null;
    }
}

export default Table;
