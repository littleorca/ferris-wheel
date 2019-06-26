import * as React from "react";
import { DataTable, Table, Row, Cell, Values } from "../../src";

const zeroTable = new Table("Zero Table");

const smallTable = new Table("Big Table", []);

for (let i = 0; i < 5; i++) {
    const row = new Row(i, []);
    for (let j = 0; j < 3; j++) {
        const cell = new Cell(j, Values.str("foobar " + i + ":" + j));
        row.cells.push(cell);
    }
    smallTable.rows.push(row);
}

const bigTable = new Table("Big Table", []);

for (let i = 0; i < 1000; i++) {
    const row = new Row(i, []);
    for (let j = 0; j < 30; j++) {
        const cell = new Cell(j, Values.str("foobar " + i + ":" + j));
        row.cells.push(cell);
    }
    bigTable.rows.push(row);
}

class TableLayoutStories extends React.Component {
    render() {
        return (
            <>
                <div>
                    <h3>zero</h3>
                    <DataTable
                        style={{
                            width: 100,
                            height: 60
                        }}
                        data={zeroTable} />
                </div>
                <div>
                    <h3>small</h3>
                    <DataTable
                        style={{
                            width: 600,
                            height: 300
                        }}
                        data={smallTable} />
                </div>
                <div>
                    <h3>big</h3>
                    <DataTable
                        style={{
                            width: 600,
                            height: 300
                        }}
                        data={bigTable} />
                </div>
            </>
        )
    }
}

export default TableLayoutStories;