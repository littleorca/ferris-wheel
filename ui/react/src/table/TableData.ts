interface LayoutInfo {
    top?: number;
    left?: number;
    width?: number;
    height?: number;
}

interface HeaderInfo extends LayoutInfo {
    key: string;
    name: string;
}

interface CellData<T> {
    value: string;
    align: "left" | "center" | "right";
    data?: T;
}

interface TableData<T> extends LayoutInfo {
    columnHeaders: HeaderInfo[];
    rowHeaders: HeaderInfo[];
    rows: CellData<T>[][];
    cornerWidth?: number;
    cornerHeight?: number;
}

export default TableData;
export { HeaderInfo, CellData }
