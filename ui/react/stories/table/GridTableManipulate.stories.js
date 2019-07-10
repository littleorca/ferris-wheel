import * as React from "react";
import GridTable from "../../src/table/GridTable";
import { zeroTable, smallTable, bigTable } from "./GridData.stories";

class GridTableManipulateStories extends React.Component {
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
                    <button onClick={() => {
                        this.dtRef.current.addColumnsBefore();
                    }}>insert col</button>
                    <button onClick={() => {
                        this.dtRef.current.addColumnsAfter();
                    }}>append col</button>
                    <button onClick={() => {
                        this.dtRef.current.removeColumns();
                    }}>remove col</button>
                    <button onClick={() => {
                        this.dtRef.current.addRowsAbove();
                    }}>insert row</button>
                    <button onClick={() => {
                        this.dtRef.current.addRowsBelow();
                    }}>append row</button>
                    <button onClick={() => {
                        this.dtRef.current.removeRows();
                    }}>remove row</button>
                </div>
                <GridTable
                    ref={this.dtRef}
                    style={{
                        width: 600,
                        height: 300
                    }}
                    data={this.table}
                    editable={true}
                    fromEditableString={s => s} />
            </div>
        )
    }
}

export default GridTableManipulateStories;