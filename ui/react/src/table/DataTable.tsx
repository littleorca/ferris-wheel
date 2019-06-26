import * as React from "react";
import Table from "../model/Table";
import Cell from "../model/Cell";
import TableData, { HeaderInfo, CellData } from "./TableData";
import { toColumnCode } from "../util/ColumnCode";
import { VariantType } from "../model/Variant";
import Formatter from "../util/Formatter";
import EscapeHelper from "../util/EscapeHelper";
import classnames from "classnames";
import "./DataTable.css";

interface DataTableProps extends React.ClassAttributes<DataTable> {
    className?: string;
    style?: React.CSSProperties;
    data: Table;
}

interface ClientPosition {
    top: number;
    right: number;
    bottom: number;
    left: number;
}

class BoundingRect {
    top: number;
    right: number;
    bottom: number;
    left: number;

    constructor(top: number, right: number, bottom: number, left: number) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    width() {
        return this.right - this.left;
    }

    height() {
        return this.bottom - this.top;
    }
};

type SelectMode = "cell" | "column" | "row" | "all";

class Selection {
    mode: SelectMode;
    firstColumn: number;
    lastColumn: number;
    firstRow: number;
    lastRow: number;

    constructor(mode: SelectMode,
        firstColumn: number,
        lastColumn: number,
        firstRow: number,
        lastRow: number) {

        this.mode = mode;
        this.firstColumn = firstColumn;
        this.lastColumn = lastColumn;
        this.firstRow = firstRow;
        this.lastRow = lastRow;
    }

    public equalsTo(another: Selection) {
        return this.mode === another.mode &&
            this.firstColumn === another.firstColumn &&
            this.lastColumn === another.lastColumn &&
            this.firstRow === another.firstRow &&
            this.lastRow === another.lastRow;
    }
}

interface Coordinate {
    x: number;
    y: number;
}

interface DataTableState {
    originData: Table;
    tableData: TableData<Cell>;
    clientPosition: ClientPosition;
    visibleRect: BoundingRect;
    focused: boolean;
    selection?: Selection;
}

const containerStyle: React.CSSProperties = {
    display: "block",
    margin: 0,
    padding: 0,
    boxSizing: "border-box",
    direction: "ltr",
};

const overlayStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    overflow: "hidden",
};

const cornerOverlayStyle: React.CSSProperties = {
    ...overlayStyle,
    zIndex: 2,
};

const headersOverlayStyle: React.CSSProperties = {
    ...overlayStyle,
    zIndex: 1,
};

const scrollableContainerStyle: React.CSSProperties = {
    ...containerStyle,
    position: "relative",
    width: "100%",
    height: "100%",
    overflow: "scroll",
};

const rowStyle: React.CSSProperties = {
    boxSizing: "border-box",
};

const cellStyle: React.CSSProperties = {
    boxSizing: "border-box",
    overflow: "hidden",
};

const helperContainerStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    zIndex: 3,
    left: 0,
    top: 0,
    width: 0,
    height: 0,
    border: "0 none",
    overflow: "visible",
};

const resizeHelperStyle: React.CSSProperties = {
    ...containerStyle,
    display: "none",
    position: "absolute",
};

const columnResizeHandleStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    top: 0,
    right: 0,
    bottom: 0,
    width: 4,
    cursor: "col-resize"
};

// const rowResizeHandleStyle: React.CSSProperties = {
//     ...containerStyle,
//     position: "absolute",
//     right: 0,
//     bottom: 0,
//     left: 0,
//     height: 4,
//     cursor: "row-resize"
// };

const selectionBorderStyle: React.CSSProperties = {
    ...containerStyle,
    position: "absolute",
    width: 0,
    height: 0,
};

class DataTable extends React.Component<DataTableProps, DataTableState> {
    private masterContainerRef = React.createRef<HTMLDivElement>();
    private scrollableContainerRef = React.createRef<HTMLDivElement>();
    private shadowTableRef = React.createRef<HTMLTableElement>();
    private cornerOverlayRef = React.createRef<HTMLDivElement>();
    private topOverlayRef = React.createRef<HTMLDivElement>();
    private leftOverlayRef = React.createRef<HTMLDivElement>();
    private helperContainerRef = React.createRef<HTMLDivElement>();
    private invisibleTextAreaRef = React.createRef<HTMLTextAreaElement>();
    private resizeHelperRef = React.createRef<HTMLDivElement>();

    // private setStateTimer: number | null;

    private dragTriggerTimer: number | null;
    private dragStart?: { x: number, y: number, target: HTMLElement };

    private scrollIntoViewTimer: number | null;

    static getDerivedStateFromProps(nextProps: DataTableProps, prevState: DataTableState) {
        if (nextProps.data === prevState.originData) {
            return null;
        }

        return {
            ...prevState,
            originData: nextProps.data,
            tableData: DataTable.createTableData(nextProps.data),
            selection: undefined,
        };
    }

    constructor(props: DataTableProps) {
        super(props);

        this.state = {
            originData: props.data,
            tableData: DataTable.createTableData(props.data),
            clientPosition: { top: 0, right: 0, bottom: 0, left: 0 },
            visibleRect: new BoundingRect(0, window.innerWidth, window.innerHeight, 0),
            focused: false,
        };

        this.handleScroll = this.handleScroll.bind(this);
        this.handleClick = this.handleClick.bind(this);
        this.handleMouseDown = this.handleMouseDown.bind(this);
        this.handleMouseMove = this.handleMouseMove.bind(this);
        this.handleMouseUp = this.handleMouseUp.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.handleFocus = this.handleFocus.bind(this);
        this.handleBlur = this.handleBlur.bind(this);
        this.handleKeyDown = this.handleKeyDown.bind(this);
        this.handleSelect = this.handleSelect.bind(this);
        this.handleCopy = this.handleCopy.bind(this);
        this.handleCut = this.handleCut.bind(this);
        this.handlePaste = this.handlePaste.bind(this);
    }

    static createTableData(data: Table): TableData<Cell> {
        const columnHeaders: HeaderInfo[] = [];
        const rowHeaders: HeaderInfo[] = [];
        const rows: CellData<Cell>[][] = [];

        let columnCount = 0;
        data.rows.forEach(tableRow => {
            const dataRow: CellData<Cell>[] = [];
            tableRow.cells.forEach(tableCell => {
                dataRow[tableCell.columnIndex] = {
                    value: Formatter.format(tableCell.value, tableCell.format),
                    align: tableCell.value.valueType() === VariantType.DECIMAL ? "right" : "left",
                    data: tableCell,
                };
                columnCount = Math.max(columnCount, tableCell.columnIndex + 1);
            })
            rows[tableRow.rowIndex] = dataRow;
        });

        for (let i = 0; i < columnCount; i++) {
            columnHeaders.push({
                key: String(i), // TODO we need a better key
                name: toColumnCode(i),
                // width: 100
            });
        }

        for (let i = 0; i < rows.length; i++) {
            rowHeaders.push({
                key: String(i), // TODO we need a better key
                name: String(i + 1),
                // height: 22
            })
        }

        return { columnHeaders, rowHeaders, rows };
    }

    componentDidUpdate(prevProps: DataTableProps) {
        if (prevProps.data !== this.props.data) {
            this.fixLayoutIfNeeded(() => this.refreshLayout());
        }
    }

    componentDidMount() {
        this.fixLayoutIfNeeded(() => this.refreshLayout());

        // document.addEventListener("click", this.handleClickHeader);
        // document.addEventListener("mousedown", this.handleMouseDown);
        document.addEventListener("mousemove", this.handleMouseMove);
        document.addEventListener("mouseup", this.handleMouseUp);
        document.addEventListener("resize", this.handleResize);
    }

    componentWillUnmount() {
        // document.removeEventListener("click", this.handleClickHeader);
        // document.removeEventListener("mousedown", this.handleMouseDown);
        document.removeEventListener("mousemove", this.handleMouseMove);
        document.removeEventListener("mouseup", this.handleMouseUp);
        document.removeEventListener("resize", this.handleResize);
    }

    public refreshLayout() {
        this.syncOverlays();
        this.updateVisibleRect();
    }

    private fixLayoutIfNeeded(callback?: () => void) {
        const table = this.shadowTableRef.current;
        if (!table) {
            return;
        }
        const thead = table.tHead;
        if (!thead) {
            return; // should not happen.
        }
        const cells = thead.rows[0].cells;
        const tableData = this.state.tableData;
        tableData.cornerWidth = cells[0].offsetWidth;;
        tableData.cornerHeight = cells[0].offsetHeight;;
        let left = 0;
        for (let j = 1; j < cells.length; j++) {
            const cell = cells[j];
            const columnHeader = tableData.columnHeaders[j - 1];
            columnHeader.left = left;
            columnHeader.width = columnHeader.width || cell.offsetWidth;
            columnHeader.height = tableData.cornerHeight;
            left += columnHeader.width;
        }
        let top = 0;
        for (let i = 0; i < tableData.rowHeaders.length; i++) {
            const rowHeader = tableData.rowHeaders[i];
            rowHeader.top = top;
            rowHeader.width = tableData.cornerWidth;
            rowHeader.height = tableData.cornerHeight;
            top += rowHeader.height;
        }

        this.setState({ tableData }, callback);
    }

    private syncOverlays() {
        const scrollableContainer = this.scrollableContainerRef.current;
        const cornerOverlay = this.cornerOverlayRef.current;
        const topOverlay = this.topOverlayRef.current;
        const leftOverlay = this.leftOverlayRef.current;

        if (!scrollableContainer ||
            !cornerOverlay ||
            !topOverlay ||
            !leftOverlay) {
            return; // References not ready to use.
        }

        topOverlay.scrollLeft = scrollableContainer.scrollLeft;
        leftOverlay.scrollTop = scrollableContainer.scrollTop;
    }

    private updateVisibleRect() {
        if (this.scrollableContainerRef.current) {
            const container = this.scrollableContainerRef.current;

            const clientPosition = this.state.clientPosition;
            clientPosition.top = container.clientTop;
            clientPosition.right = container.offsetWidth - container.clientLeft - container.clientWidth;
            clientPosition.bottom = container.offsetHeight - container.clientTop - container.clientHeight;
            clientPosition.left = container.clientLeft;

            const visibleRect = this.state.visibleRect;
            visibleRect.top = container.scrollTop;
            visibleRect.right = container.scrollLeft + container.clientWidth;
            visibleRect.bottom = container.scrollTop + container.clientHeight;
            visibleRect.left = container.scrollLeft;

            this.setState({
                clientPosition,
                visibleRect
            });
        }
    }

    protected handleScroll(event: React.UIEvent<HTMLDivElement>) {
        this.syncOverlays();
        this.updateVisibleRect();
    }

    protected handleClick(e: React.MouseEvent<HTMLDivElement>) {
        // dummy
    }

    protected onClickColumnHeader(columnIndex: number) {
        this.selectCells(columnIndex, columnIndex);
    }

    protected onClickRowHeader(rowIndex: number) {
        this.selectCells(undefined, undefined, rowIndex, rowIndex);
    }

    protected onClickDataCell(columnIndex: number, rowIndex: number) {
        this.selectCells(columnIndex, columnIndex, rowIndex, rowIndex);
    }

    public selectCells(firstColumn: number = NaN,
        lastColumn: number = NaN,
        firstRow: number = NaN,
        lastRow: number = NaN,
        callback?: () => void) {

        const { columnHeaders, rowHeaders } = this.state.tableData;

        let columnMode = false;
        let rowMode = false;

        if (isNaN(firstColumn)) {
            firstColumn = 0;
            rowMode = true;
        }
        if (isNaN(lastColumn)) {
            lastColumn = columnHeaders.length - 1;
            rowMode = true;
        }
        if (isNaN(firstRow)) {
            firstRow = 0;
            columnMode = true;
        }
        if (isNaN(lastRow)) {
            lastRow = rowHeaders.length - 1;
            columnMode = true;
        }

        const mode = (columnMode && rowMode) ? "all" :
            columnMode ? "column" :
                rowMode ? "row" :
                    "cell";

        const selection = new Selection(
            mode,
            firstColumn,
            lastColumn,
            firstRow,
            lastRow,
        );

        if (this.state.selection && this.state.selection.equalsTo(selection)) {
            if (typeof callback === "function") {
                callback();
            }
            return;
        }

        this.setState({ selection }, () => {
            if (typeof callback === "function") {
                callback();
            }
        });
    }

    public setFocused() {
        const textarea = this.invisibleTextAreaRef.current;
        if (textarea) {
            textarea.focus();
        }
    }

    protected handleMouseDown(e: React.MouseEvent<HTMLDivElement>) {
        e.preventDefault();
        this.setFocused();

        const target = e.target as HTMLElement;
        const dragStart = {
            x: e.pageX,
            y: e.pageY,
            target,
        };
        this.dragTriggerTimer = window.setTimeout(() => {
            this.dragStart = dragStart;
            if (this.dragTriggerTimer !== null) {
                clearTimeout(this.dragTriggerTimer);
            }
        }, 100);


        if (target.matches(".header-cell.column-header")) {
            const columnIndex = Number(target.getAttribute("data-column-index"));
            this.onClickColumnHeader(columnIndex);

        } else if (target.matches(".header-cell.row-header")) {
            const rowIndex = Number(target.getAttribute("data-row-index"));
            this.onClickRowHeader(rowIndex);

        } else if (target.matches(".data-cell")) {
            const columnIndex = Number(target.getAttribute("data-column-index"));
            const rowIndex = Number(target.getAttribute("data-row-index"));
            this.onClickDataCell(columnIndex, rowIndex);
        }

    }

    protected handleMouseMove(e: MouseEvent) {
        if (!this.dragStart) {
            return;
        }

        const originTarget = this.dragStart.target;
        const masterContainer = this.masterContainerRef.current;
        const scrollableContainer = this.scrollableContainerRef.current;
        const helperContainer = this.helperContainerRef.current;
        const resizeHelper = this.resizeHelperRef.current;

        if (!masterContainer || !scrollableContainer || !helperContainer || !resizeHelper) {
            return;
        }

        if (originTarget.matches(".column-resize-handle")) {
            const height = scrollableContainer.clientHeight;
            const left = e.clientX - 2 - helperContainer.getBoundingClientRect().left;

            Object.assign(resizeHelper.style, {
                display: "block",
                top: "0",
                left: left + "px",
                width: "0",
                height: height + "px",
            });

        } else if (originTarget.matches(".data-cell")) {
            const startCellPosition = {
                x: Number(originTarget.getAttribute("data-column-index")),
                y: Number(originTarget.getAttribute("data-row-index")),
            };
            const coordinate = this.getCoordinateRelativeToScrollableContainer(e);
            const endCellPosition = this.getNearestCellByCoordinate(coordinate);

            const firstColumn = Math.min(startCellPosition.x, endCellPosition.x);
            const lastColumn = Math.max(startCellPosition.x, endCellPosition.x);
            const firstRow = Math.min(startCellPosition.y, endCellPosition.y);
            const lastRow = Math.max(startCellPosition.y, endCellPosition.y);

            if (!this.state.selection ||
                firstColumn !== this.state.selection.firstColumn ||
                lastColumn !== this.state.selection.lastColumn ||
                firstRow !== this.state.selection.firstRow ||
                lastRow !== this.state.selection.lastRow) {

                const hFlag = endCellPosition.x - startCellPosition.x;
                const vFlag = endCellPosition.y - startCellPosition.y;
                this.selectCells(firstColumn, lastColumn, firstRow, lastRow, () => {
                    this.scrollCellIntoView(endCellPosition.x, endCellPosition.y,
                        hFlag < 0 ? "left" : hFlag === 0 ? "none" : "right",
                        vFlag < 0 ? "top" : vFlag === 0 ? "none" : "bottom");
                });
            }

        } else if (originTarget.matches(".header-cell.column-header")) {
            const startIndex = Number(originTarget.getAttribute("data-column-index"));
            const coordinate = this.getCoordinateRelativeToScrollableContainer(e);
            const endCellPosition = this.getNearestCellByCoordinate(coordinate);

            const firstColumn = Math.min(startIndex, endCellPosition.x);
            const lastColumn = Math.max(startIndex, endCellPosition.x);

            if (!this.state.selection ||
                this.state.selection.mode !== "column" ||
                firstColumn !== this.state.selection.firstColumn ||
                lastColumn !== this.state.selection.lastColumn) {

                const flag = endCellPosition.x - startIndex;
                this.selectCells(firstColumn, lastColumn, undefined, undefined, () => {
                    this.scrollCellIntoView(endCellPosition.x, endCellPosition.y,
                        flag < 0 ? "left" : flag === 0 ? "none" : "right",
                        "none");
                });
            }

        } else if (originTarget.matches(".header-cell.row-header")) {
            const startIndex = Number(originTarget.getAttribute("data-row-index"));
            const coordinate = this.getCoordinateRelativeToScrollableContainer(e);
            const endCellPosition = this.getNearestCellByCoordinate(coordinate);

            const firstRow = Math.min(startIndex, endCellPosition.y);
            const lastRow = Math.max(startIndex, endCellPosition.y);

            if (!this.state.selection ||
                this.state.selection.mode !== "column" ||
                firstRow !== this.state.selection.firstRow ||
                lastRow !== this.state.selection.lastRow) {

                const flag = endCellPosition.y - startIndex;
                this.selectCells(undefined, undefined, firstRow, lastRow, () => {
                    this.scrollCellIntoView(endCellPosition.x, endCellPosition.y,
                        "none",
                        flag < 0 ? "top" : flag === 0 ? "none" : "bottom");
                });
            }

        }
    }

    protected getCoordinateRelativeToScrollableContainer(e: MouseEvent): Coordinate {
        const scrollableContainer = this.scrollableContainerRef.current;
        if (!scrollableContainer) {
            throw Error();
        }
        const rect = scrollableContainer.getBoundingClientRect();
        const x = e.clientX - rect.left + scrollableContainer.scrollLeft;
        const y = e.clientY - rect.top + scrollableContainer.scrollTop;
        return { x, y };
    }

    protected getNearestCellByCoordinate(coordinate: Coordinate): Coordinate {
        const tableData = this.state.tableData;
        const x = coordinate.x - (tableData.cornerWidth || 0);
        const y = coordinate.y - (tableData.cornerHeight || 0);

        let columnIndex = 0;
        let rowIndex = 0;

        if (x >= 0) {
            columnIndex = tableData.columnHeaders.length - 1;
            for (let i = 0; i < tableData.columnHeaders.length; i++) {
                const left = tableData.columnHeaders[i].left || 0;
                const right = left + (tableData.columnHeaders[i].width || 0);
                if (x >= left && x < right) {
                    columnIndex = i;
                    break;
                }
            }
        }

        if (y >= 0) {
            rowIndex = tableData.rowHeaders.length - 1;
            for (let i = 0; i < tableData.rowHeaders.length; i++) {
                const top = tableData.rowHeaders[i].top || 0;
                const bottom = top + (tableData.rowHeaders[i].height || 0);
                if (y >= top && y < bottom) {
                    rowIndex = i;
                    break;
                }
            }
        }

        return { x: columnIndex, y: rowIndex };
    }

    protected scrollCellIntoView(columnIndex: number, rowIndex: number,
        xAnchor: "left" | "right" | "none",
        yAnchor: "top" | "bottom" | "none") {

        const container = this.scrollableContainerRef.current;
        if (!container) {
            return;
        }
        const cellRect = this.getRelativeCellBoundingRect(columnIndex, rowIndex);
        const cornerWidth = this.state.tableData.cornerWidth || 0;
        const cornerHeight = this.state.tableData.cornerHeight || 0;

        let delay = 200;
        if (this.scrollIntoViewTimer !== null) {
            clearTimeout(this.scrollIntoViewTimer);
            delay = 10;
        }

        this.scrollIntoViewTimer = window.setTimeout(() => {
            if (xAnchor === "left" && cellRect.left < container.scrollLeft + cornerWidth) {
                container.scrollLeft = cellRect.left - cornerWidth;
            } else if (xAnchor === "right" && cellRect.right > container.scrollLeft + container.clientWidth) {
                container.scrollLeft = cellRect.right - container.clientWidth;
            }

            if (yAnchor === "top" && cellRect.top < container.scrollTop + cornerHeight) {
                container.scrollTop = cellRect.top - cornerHeight;
            } else if (yAnchor === "bottom" && cellRect.bottom > container.scrollTop + container.clientHeight) {
                container.scrollTop = cellRect.bottom - container.clientHeight;
            }

            if (this.scrollIntoViewTimer !== null) {
                clearTimeout(this.scrollIntoViewTimer);
            }

        }, delay);
    }

    protected getRelativeCellBoundingRect(columnIndex: number, rowIndex: number) {
        const tableData = this.state.tableData;
        const columnHeader = tableData.columnHeaders[columnIndex];
        const rowHeader = tableData.rowHeaders[rowIndex];
        const top = (tableData.cornerHeight || 0) + (rowHeader.top || 0);
        const bottom = top + (rowHeader.height || 0);
        const left = (tableData.cornerWidth || 0) + (columnHeader.left || 0);
        const right = left + (columnHeader.width || 0);
        return new BoundingRect(top, right, bottom, left);
    }

    protected handleMouseUp(e: MouseEvent) {
        if (this.dragTriggerTimer !== null) {
            clearTimeout(this.dragTriggerTimer);
        }

        if (!this.dragStart) {
            return;
        }

        const dragStart = this.dragStart;
        this.dragStart = undefined;

        const masterContainer = this.masterContainerRef.current;
        const scrollableContainer = this.scrollableContainerRef.current;
        const resizeHelper = this.resizeHelperRef.current;

        if (resizeHelper) {
            resizeHelper.style.display = "none";
        }

        const originTarget = dragStart.target;
        if (masterContainer &&
            scrollableContainer &&
            masterContainer.contains(originTarget) &&
            originTarget.matches(".column-resize-handle") &&
            originTarget.parentElement) {

            const columnHeaderElement = originTarget.parentElement;
            const w = e.pageX - columnHeaderElement.getBoundingClientRect().left;
            if (w > 0) {
                const index = Number(columnHeaderElement.getAttribute("data-column-index"));
                const tableData = this.state.tableData;
                let columnHeader = tableData.columnHeaders[index];
                columnHeader.width = w;
                let left = (columnHeader.left || 0) + w;
                for (let j = index + 1; j < tableData.columnHeaders.length; j++) {
                    columnHeader = tableData.columnHeaders[j];
                    columnHeader.left = left;
                    left += columnHeader.width || 0;
                }
                this.setState({ tableData }, () => this.refreshLayout());
            }
        }
    }

    protected handleResize(e: Event) {
        this.updateVisibleRect();
    }

    protected handleFocus() {
        // TODO
        this.setState({ focused: true });
    }

    protected handleBlur() {
        // TODO
        this.setState({ focused: false });
    }

    protected handleKeyDown(e: React.KeyboardEvent) {
        // TODO
    }

    protected handleSelect(e: React.SyntheticEvent<HTMLTextAreaElement>) {
        const textarea = this.invisibleTextAreaRef.current;
        if (!textarea) {
            return;
        }
        const value = textarea.value;
        const length = Math.abs(textarea.selectionEnd - textarea.selectionStart);
        if (length === value.length) {
            this.selectAll();
        }
        textarea.setSelectionRange(0, 0);
    }

    public selectAll() {
        const tableData = this.state.tableData;
        this.selectCells(0, tableData.columnHeaders.length - 1, 0, tableData.rowHeaders.length - 1);
    }

    protected handleCopy(e: React.ClipboardEvent) {
        e.preventDefault();

        const selection = this.state.selection;
        if (!selection) {
            return;
        }
        const data = this.state.tableData.rows;
        let text = "",
            html = "<!doctype html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /></head><body><table><tbody>\n";
        for (let i = selection.firstRow; i <= selection.lastRow; i++) {
            if (text.length > 0) {
                text += "\r\n";
            }
            html += "<tr>";

            let line = "";
            for (let j = selection.firstColumn; j <= selection.lastColumn; j++) {
                if (line.length > 0) {
                    line += ",";
                }
                let value = data[i] && data[i][j] && data[i][j].value;
                if (typeof value === "undefined" || value === null) {
                    value = "";
                }
                line += EscapeHelper.escapeForCSV(value);
                html += "<td>" + EscapeHelper.escapeForHtml(value) + "</td>";
            }
            text += line;
            html += "</tr>\n";
        }
        html += "</tbody></table></body></html>";

        e.clipboardData.setData("text/plain", text);
        e.clipboardData.setData("text/html", html);
    }

    protected handleCut(e: React.ClipboardEvent) {
        e.preventDefault();
        // TODO
    }

    protected handlePaste(e: React.ClipboardEvent) {
        e.preventDefault();
        // TODO
    }

    render() {
        const focused = this.state.focused;
        const className = classnames("fw-data-table", this.props.className,
            { "focused": focused });
        const tableData = this.state.tableData;
        const clientPosition = this.state.clientPosition;
        const visibleRect = this.state.visibleRect;
        const renderRect = new BoundingRect(
            visibleRect.top - visibleRect.height(),
            visibleRect.right + visibleRect.width(),
            visibleRect.bottom + visibleRect.height(),
            visibleRect.left - visibleRect.width(),
        );

        const cornerWidth = tableData.cornerWidth || 0;
        const cornerHeight = tableData.cornerHeight || 0;

        const selection = this.state.selection;

        let dataCellSelectionBoundingRect = undefined;

        if (selection) {
            const firstColumn = tableData.columnHeaders[selection.firstColumn]
            const lastColumn = tableData.columnHeaders[selection.lastColumn];
            const firstRow = tableData.rowHeaders[selection.firstRow];
            const lastRow = tableData.rowHeaders[selection.lastRow];

            const top = cornerHeight + (firstRow.top || 0);
            const right = cornerWidth + (lastColumn.left || 0) + (lastColumn.width || 0);
            const bottom = cornerHeight + (lastRow.top || 0) + (lastRow.height || 0);
            const left = cornerWidth + (firstColumn.left || 0);

            dataCellSelectionBoundingRect = new BoundingRect(top, right, bottom, left);
        }

        return (
            <div
                ref={this.masterContainerRef}
                className={className}
                style={{
                    ...this.props.style,
                    ...containerStyle,
                    position: "relative",
                    overflow: "hidden",
                }}
                onClick={this.handleClick}
                onMouseDown={this.handleMouseDown}>
                <div
                    ref={this.scrollableContainerRef}
                    className="scrollable-container"
                    style={scrollableContainerStyle}
                    onScroll={this.handleScroll}>
                    {tableData.cornerWidth ?
                        this.renderTableInWindow(false, false, renderRect) :
                        this.renderShadowTable()}
                    {dataCellSelectionBoundingRect && (
                        <div style={containerStyle}>
                            <div
                                className="selection-border selection-top-border"
                                style={{
                                    ...selectionBorderStyle,
                                    top: dataCellSelectionBoundingRect.top,
                                    left: dataCellSelectionBoundingRect.left,
                                    width: dataCellSelectionBoundingRect.width()
                                }}></div>
                            <div
                                className="selection-border selection-right-border"
                                style={{
                                    ...selectionBorderStyle,
                                    top: dataCellSelectionBoundingRect.top,
                                    left: dataCellSelectionBoundingRect.right,
                                    height: dataCellSelectionBoundingRect.height()
                                }}></div>
                            <div
                                className="selection-border selection-bottom-border"
                                style={{
                                    ...selectionBorderStyle,
                                    top: dataCellSelectionBoundingRect.bottom,
                                    left: dataCellSelectionBoundingRect.left,
                                    width: dataCellSelectionBoundingRect.width()
                                }}></div>
                            <div
                                className="selection-border selection-left-border"
                                style={{
                                    ...selectionBorderStyle,
                                    top: dataCellSelectionBoundingRect.top,
                                    left: dataCellSelectionBoundingRect.left,
                                    height: dataCellSelectionBoundingRect.height()
                                }}></div>
                        </div>
                    )}
                </div>
                <div
                    ref={this.cornerOverlayRef}
                    className={"corner-overlay"}
                    style={{
                        ...cornerOverlayStyle,
                        top: clientPosition.top,
                        left: clientPosition.left,
                        width: tableData.cornerWidth,
                        height: tableData.cornerHeight
                    }}>
                    {this.renderTableInWindow(true, true, new BoundingRect(0, tableData.cornerWidth || 0, tableData.cornerHeight || 0, 0))}
                </div>
                <div
                    ref={this.topOverlayRef}
                    className="top-overlay"
                    style={{
                        ...headersOverlayStyle,
                        top: clientPosition.top,
                        right: clientPosition.right,
                        left: clientPosition.left,
                        height: tableData.cornerHeight
                    }}>
                    {this.renderTableInWindow(true, false, new BoundingRect(0, renderRect.right, tableData.cornerHeight || 0, renderRect.left))}
                </div>
                <div
                    ref={this.leftOverlayRef}
                    className="left-overlay"
                    style={{
                        ...headersOverlayStyle,
                        top: clientPosition.top,
                        bottom: clientPosition.bottom,
                        left: clientPosition.left,
                        width: tableData.cornerWidth,
                    }}>
                    {this.renderTableInWindow(false, true, new BoundingRect(renderRect.top, tableData.cornerWidth || 0, renderRect.bottom, 0))}
                </div>
                <div
                    ref={this.helperContainerRef}
                    className="fw-data-table-helpers"
                    style={helperContainerStyle}>
                    <div
                        ref={this.resizeHelperRef}
                        className="resize-helper column-resize-helper"
                        style={resizeHelperStyle}></div>
                    {/* <div
                        ref={this.selectionHelperRef}
                        style={{
                            ...selectionHelperStyle,
                        }}></div> */}
                </div>
                <textarea
                    ref={this.invisibleTextAreaRef}
                    style={{
                        ...containerStyle,
                        position: "absolute",
                        zIndex: -1,
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%",
                        outline: "none",
                    }}
                    defaultValue={"ferris wheel"}
                    readOnly
                    tabIndex={-1}
                    onFocus={this.handleFocus}
                    onBlur={this.handleBlur}
                    onKeyDown={this.handleKeyDown}
                    onSelect={this.handleSelect}
                    onCopy={this.handleCopy}
                    onCut={this.handleCut}
                    onPaste={this.handlePaste} />
            </div>
        );
    }

    private renderTableInWindow(
        withColumnHeader: boolean = true,
        withRowHeader: boolean = true,
        renderRect: BoundingRect = new BoundingRect(
            Number.NEGATIVE_INFINITY,
            Number.POSITIVE_INFINITY,
            Number.POSITIVE_INFINITY,
            Number.NEGATIVE_INFINITY
        )) {

        const tableData = this.state.tableData;

        const offsetX = tableData.cornerWidth || 0;
        const offsetY = tableData.cornerHeight || 0;

        const lastColumnHeader = tableData.columnHeaders[tableData.columnHeaders.length - 1];
        const totalWidth = offsetX +
            (lastColumnHeader && lastColumnHeader.left || 0) +
            (lastColumnHeader && lastColumnHeader.width || 0);

        const lastRowHeader = tableData.rowHeaders[tableData.rowHeaders.length - 1];
        const totalHeight = offsetY +
            (lastRowHeader && lastRowHeader.top || 0) +
            (lastRowHeader && lastRowHeader.height || 0);

        const showCorner = withColumnHeader && withRowHeader &&
            offsetX >= renderRect.left && offsetY >= renderRect.top;

        return (
            <div
                className="data-section"
                style={{
                    position: "relative",
                    width: totalWidth,
                    height: totalHeight,
                }}>
                {showCorner && (
                    <div
                        className="cell header-cell corner"
                        style={{
                            ...cellStyle,
                            position: "absolute",
                            top: 0,
                            left: 0,
                            width: tableData.cornerWidth,
                            height: tableData.cornerHeight,
                        }}></div>
                )}
                {withColumnHeader && offsetY >= renderRect.top && tableData.columnHeaders.map((columnHeader, columnIndex) => {
                    const left = offsetX + (columnHeader.left || 0);
                    if (left >= renderRect.right || (left + (columnHeader.width || 0)) < left) {
                        return null;
                    }
                    return (
                        <div
                            key={columnHeader.key}
                            className={classnames(
                                "cell",
                                "header-cell",
                                "column-header",
                                "column-index-" + columnIndex,
                                {
                                    "highlight": this.isColumnHighlighted(columnIndex),
                                    "selected": this.isColumnSelected(columnIndex)
                                },
                            )}
                            style={{
                                ...cellStyle,
                                position: "absolute",
                                left,
                                width: columnHeader.width,
                                height: offsetY,
                            }}
                            data-column-index={columnIndex}>
                            {columnHeader.name}
                            <div
                                className="resize-handle column-resize-handle"
                                style={columnResizeHandleStyle}></div>
                        </div>
                    );
                })}
                {tableData.rowHeaders.map((rowHeader, rowIndex) => {
                    const top = offsetY + (rowHeader.top || 0);
                    const bottom = top + (rowHeader.height || 0);
                    if (top >= renderRect.bottom || bottom <= renderRect.top) {
                        return null;
                    }

                    const row = tableData.rows[rowIndex];
                    return (
                        <React.Fragment key={rowHeader.key}>
                            {withRowHeader && offsetX >= renderRect.left && (
                                <div
                                    className={classnames(
                                        "cell",
                                        "header-cell",
                                        "row-header",
                                        "row-index-" + rowIndex,
                                        {
                                            "highlight": this.isRowHighlighted(rowIndex),
                                            "selected": this.isRowSelected(rowIndex)
                                        },
                                    )}
                                    style={{
                                        ...cellStyle,
                                        position: "absolute",
                                        top,
                                        left: 0,
                                        width: offsetX,
                                        height: rowHeader.height,
                                    }}
                                    data-row-index={rowIndex}>
                                    {rowHeader.name}
                                    {/* <div
                                     className="resize-handle row-resize-handle" 
                                     style={rowResizeHandleStyle}></div> */}
                                </div>
                            )}
                            {tableData.columnHeaders.map((columnHeader, columnIndex) => {
                                const left = offsetX + (columnHeader.left || 0);
                                if (left >= renderRect.right || (left + (columnHeader.width || 0)) < renderRect.left) {
                                    return null;
                                }
                                const cell = row && row[columnIndex];
                                return (
                                    <div
                                        key={columnHeader.key}
                                        className={classnames(
                                            "cell",
                                            "data-cell",
                                            "row-index-" + rowIndex,
                                            "column-index-" + columnIndex,
                                            {
                                                "align-right": cell && cell.align === "right",
                                                "align-center": cell && cell.align === "center",
                                            }
                                        )}
                                        style={{
                                            ...cellStyle,
                                            position: "absolute",
                                            top,
                                            left,
                                            width: columnHeader.width,
                                            height: rowHeader.height,
                                        }}
                                        data-row-index={rowIndex}
                                        data-column-index={columnIndex}>
                                        {cell && cell.value}
                                    </div>
                                );
                            })}
                        </React.Fragment>
                    );
                })}
            </div>
        );
    }

    private isColumnHighlighted(columnIndex: number) {
        const selection = this.state.selection;
        return selection && (
            selection.mode === "cell" &&
            columnIndex >= selection.firstColumn &&
            columnIndex <= selection.lastColumn ||
            selection.mode === "row"
        );
    }

    private isColumnSelected(columnIndex: number) {
        const selection = this.state.selection;
        return selection &&
            (selection.mode === "column" || selection.mode === "all") &&
            columnIndex >= selection.firstColumn &&
            columnIndex <= selection.lastColumn;
    }

    private isRowHighlighted(rowIndex: number) {
        const selection = this.state.selection;
        return selection && (
            selection.mode === "cell" &&
            rowIndex >= selection.firstRow &&
            rowIndex <= selection.lastRow ||
            selection.mode === "column"
        );
    }

    private isRowSelected(rowIndex: number) {
        const selection = this.state.selection;
        return selection &&
            (selection.mode === "row" || selection.mode === "all") &&
            rowIndex >= selection.firstRow &&
            rowIndex <= selection.lastRow;
    }

    private renderShadowTable() {
        const { columnHeaders, rowHeaders, rows } = this.state.tableData;
        let longestRowHeaderName = "";
        rowHeaders.forEach(rowHeader => {
            if (rowHeader.name.length > longestRowHeaderName.length) {
                longestRowHeaderName = rowHeader.name;
            }
        });

        const limitedRows = Math.max(3, 1000 / (columnHeaders.length || 1));

        return (
            <table
                ref={this.shadowTableRef}
                cellPadding={0}
                cellSpacing={0}
                style={{
                    position: "absolute",
                    whiteSpace: "nowrap",
                    visibility: "visible",
                    opacity: .5,
                }}>
                <thead>
                    <tr style={rowStyle}>
                        <th className="cell header-cell corner"
                            style={cellStyle}>
                            &nbsp;
                        </th>
                        {columnHeaders.map((columnHeader, columnIndex) => (
                            <th
                                key={columnHeader.key}
                                className={"cell header-cell column-header column-index-" + columnIndex}
                                style={cellStyle}
                                data-column-index={columnIndex}>
                                {columnHeader.name}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {rowHeaders.map((rowHeader, rowIndex) => {
                        if (rowIndex > limitedRows) {
                            return null;
                        }
                        return (
                            <tr key={rowHeader.key}
                                className={"row row-index-" + rowIndex}
                                style={rowStyle}
                                data-row-index={rowIndex}>
                                <th className={"cell header-cell row-header row-index-" + rowIndex}
                                    style={cellStyle}>
                                    {longestRowHeaderName}
                                </th>
                                {columnHeaders.map((columnHeader, columnIndex) => {
                                    const c = rows[rowIndex] && rows[rowIndex][columnIndex];
                                    return (
                                        <td
                                            key={columnHeader.key}
                                            className={classnames(
                                                "cell",
                                                "data-cell",
                                                "row-index-" + rowIndex,
                                                "column-index-" + columnIndex,
                                                {
                                                    "align-right": c && c.align === "right",
                                                    "align-center": c && c.align === "center"
                                                }
                                            )}
                                            style={cellStyle}
                                            data-column-index={columnIndex}>
                                            {c && c.value}
                                        </td>
                                    );
                                })}
                            </tr>
                        )
                    })}
                </tbody>
            </table>
        );
    }
}

export default DataTable;