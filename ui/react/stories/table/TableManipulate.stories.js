import * as React from "react";
import { DataTable, Table, Row, Cell, Values } from "../../src";

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

for (let i = 0; i < 2000; i++) {
    const row = new Row(i, []);
    for (let j = 0; j < 30; j++) {
        const cell = new Cell(j, Values.str("foobar " + i + ":" + j));
        row.cells.push(cell);
    }
    bigTable.rows.push(row);
}

class TableManipulateStories extends React.Component {
    dtRef = React.createRef();
    table = bigTable;

    render() {
        return (
            <div>
                <div>
                    <button onClick={() => {
                        if (this.table === smallTable) {
                            this.table = bigTable;
                        } else {
                            this.table = smallTable;
                        }
                        this.forceUpdate();
                    }}>切换数据</button>
                    <button onClick={() => {
                        this.dtRef.current.refreshLayout();
                    }}>refreshLayout</button>
                </div>
                <DataTable
                    ref={this.dtRef}
                    style={{
                        width: 600,
                        height: 300
                    }}
                    data={this.table} />
            </div>
        )
    }
}

export default TableManipulateStories;