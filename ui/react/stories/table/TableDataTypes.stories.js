import * as React from "react";
import { DataTable, Table, Row, Cell, Values } from "../../src";

const table = new Table("Big Table", [
    new Row(0, [
        new Cell(0, Values.str("hello")),
        new Cell(1, Values.str("the beautiful")),
        new Cell(2, Values.str("world"))
    ]),
    new Row(1, [
        new Cell(0, Values.date(new Date())),
        new Cell(1, Values.dec(1414), "#,##0.00"),
        new Cell(2, Values.blank())
    ]),
    new Row(2, [
        new Cell(0, Values.date(new Date(), "yyyy-MM-dd HH:mm:ss")),
        new Cell(1, Values.dec(1.414), "#,##0.00%"),
        new Cell(2, Values.bool(true))
    ]),
]);

class TableDataTypesStories extends React.Component {
    render() {
        return (
            <div>
                <DataTable
                    style={{
                        width: 500,
                        height: 200
                    }}
                    data={table} />
            </div>
        )
    }
}

export default TableDataTypesStories;