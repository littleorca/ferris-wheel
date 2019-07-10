import GridDataImpl from "../../src/model/GridDataImpl";
import Header from "../../src/model/Header";
import GridCellImpl from "../../src/model/GridCellImpl";

export function exampleColumnHeaders(count) {
    const headers = [];
    for (let j = 0; j < count; j++) {
        headers.push(new Header());
    }
    return headers;
}

export function exampleRowHeaders(count) {
    const headers = [];
    for (let i = 0; i < count; i++) {
        headers.push(new Header());
    }
    return headers;
}

export function exampleTableData(str, rowCount, columnCount) {
    const table = new GridDataImpl(
        [],
        exampleColumnHeaders(columnCount),
        exampleRowHeaders(rowCount),
    );

    for (let i = 0; i < rowCount; i++) {
        const row = [];
        for (let j = 0; j < columnCount; j++) {
            row.push(new GridCellImpl(
                str + i + ":" + j,
                "left",
            ));
        }
        table.rows.push(row);
    }

    return table;
}

export const zeroTable = exampleTableData("", 0, 0);
export const smallTable = exampleTableData("small ", 5, 3);
export const bigTable = exampleTableData("foobar ", 1000, 30);
