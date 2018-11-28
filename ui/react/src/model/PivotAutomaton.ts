import UnionValue from "./UnionValue";
import PivotFilter from "./PivotFilter";
import PivotField from "./PivotField";
import PivotValue from "./PivotValue";
import Values from "./Values";

class PivotAutomaton {
    public data: UnionValue;
    public filters: PivotFilter[];
    public rows: PivotField[];
    public columns: PivotField[];
    public values: PivotValue[];

    public static deserialize(input: any): PivotAutomaton {
        const data = typeof input.data !== 'undefined' ?
            Values.deserialize(input.data) : undefined;
        const filters = [];
        if (typeof input.filters !== 'undefined' && input.filters !== null) {
            for (const filter of input.filters) {
                filters.push(PivotFilter.deserialize(filter));
            }
        }
        const rows = [];
        if (typeof input.rows !== 'undefined' && input.rows !== null) {
            for (const row of input.rows) {
                rows.push(PivotField.deserialize(row));
            }
        }
        const columns = [];
        if (typeof input.columns !== 'undefined' && input.columns !== null) {
            for (const column of input.columns) {
                columns.push(PivotField.deserialize(column));
            }
        }
        const values = [];
        if (typeof input.values !== 'undefined' && input.values !== null) {
            for (const value of input.values) {
                values.push(PivotValue.deserialize(value));
            }
        }
        return new PivotAutomaton(data, filters, rows, columns, values);
    }

    constructor(
        data: UnionValue = Values.blank(),
        filters: PivotFilter[] = [],
        rows: PivotField[] = [],
        columns: PivotField[] = [],
        values: PivotValue[] = []) {

        this.data = data;
        this.filters = filters;
        this.rows = rows;
        this.columns = columns;
        this.values = values;
    }
}

export default PivotAutomaton;
