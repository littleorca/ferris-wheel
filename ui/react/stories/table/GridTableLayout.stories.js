import * as React from "react";
import GridTable from "../../src/table/GridTable";
import {
    zeroTable, bigTable,
    exampleColumnHeaders, exampleRowHeaders
} from "./GridData.stories";
import GridDataImpl from "../../src/model/GridDataImpl";
import GridCellImpl from "../../src/model/GridCellImpl";

const alignmentDemoTable = new GridDataImpl(
    [[
        new GridCellImpl("hello world", "left"),
        new GridCellImpl("the beautiful", "left"),
        new GridCellImpl("world !!!!", "left"),
    ], [
        new GridCellImpl("left", "left"),
        new GridCellImpl("center", "center"),
        new GridCellImpl("right", "right"),
    ], [
        new GridCellImpl("center", "center"),
        new GridCellImpl("right", "right"),
        new GridCellImpl("left", "left"),
    ]]
);

class TableLayoutStories extends React.Component {
    render() {
        return (
            <>
                <div>
                    <h3>zero</h3>
                    <GridTable
                        style={{
                            width: 100,
                            height: 60
                        }}
                        data={zeroTable}
                        fromEditableString={s => s} />
                </div>
                <div>
                    <h3>small, alignment</h3>
                    <GridTable
                        style={{
                            width: 500,
                            height: 200
                        }}
                        data={alignmentDemoTable}
                        fromEditableString={s => s} />
                </div>
                <div>
                    <h3>big</h3>
                    <GridTable
                        style={{
                            width: 600,
                            height: 300
                        }}
                        data={bigTable}
                        fromEditableString={s => s} />
                </div>
            </>
        )
    }
}

export default TableLayoutStories;