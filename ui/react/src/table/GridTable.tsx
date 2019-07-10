import * as React from "react";
import Rectangle from "../model/Rectangle";
import CellPosition from "../model/CellPosition";
import GridCell, { CellAlign } from "../model/GridCell";
import GridData from "../model/GridData";
import Header from "../model/Header";
import Selection from "../model/Selection";
import RectangleImpl from "../model/RectangleImpl";
import {
    containerStyle,
    masterContainerStyle,
    scrollableContainerStyle,
    rowStyle,
    cellStyle,
    cornerOverlayStyle,
    headersOverlayStyle,
    helperContainerStyle,
    resizeHelperStyle,
    columnResizeHandleStyle,
    selectionBorderStyle,
    editContainerStyle,
    defaultEditStyle,
} from "./Styles";
import EditBox from "../ctrl/EditBox";
import ValueChange from "../ctrl/ValueChange";
import { toColumnCode } from "../util/ColumnCode";
import classnames from "classnames";
import "./GridTable.css";

interface CustomRendererProps<T> extends React.ClassAttributes<any> {
    columnIndex: number;
    rowIndex: number;
    data?: T;
    align: CellAlign;
    width?: number;
    height?: number;
}

interface CustomEditorProps<T> extends CustomRendererProps<T> {
    initialInput?: string;
    onOk: (value: T) => void;
    onCancel: () => void;
}

interface GridTableProps<T> extends React.ClassAttributes<GridTable<T>> {
    className?: string;
    style?: React.CSSProperties;
    data: GridData<T>;
    editable?: boolean;
    disableCopy?: boolean;
    newValue?: () => T;
    forDisplay?: (value?: T) => string;
    forEdit?: (value?: T) => string;
    fromEditableString?: (str: string) => T;
    forCopy?: (value?: T) => string;
    customRenderer?: React.SFC<CustomRendererProps<T>>;
    customEditor?: React.SFC<CustomEditorProps<T>>;
    beforeAddRows?: (rowIndex: number, rowCount: number) => boolean;
    afterAddRows?: (rowIndex: number, rowCount: number) => void;
    beforeAddColumns?: (columnIndex: number, columnCount: number) => boolean;
    afterAddColumns?: (columnIndex: number, columnCount: number) => void;
    beforeEraseCells?: (top: number, right: number, bottom: number, left: number) => boolean;
    afterEraseCells?: (top: number, right: number, bottom: number, left: number) => void;
    beforeRemoveRows?: (rowIndex: number, rowCount: number) => boolean;
    afterRemoveRows?: (rowIndex: number, rowCount: number) => void;
    beforeRemoveColumns?: (columnIndex: number, columnCount: number) => boolean;
    afterRemoveColumns?: (columnIndex: number, columnCount: number) => void;
    beforeSetCellValue?: (valueChange: ValueChange<T>, rowIndex: number, columnIndex: number) => boolean;
    afterSetCellValue?: (gridCell: GridCell<T>, rowIndex: number, columnIndex: number) => void;
}

interface ClientPosition {
    top: number;
    right: number;
    bottom: number;
    left: number;
}

interface Coordinate {
    x: number;
    y: number;
}

class EditSession<T> extends CellPosition {
    gridCell: GridCell<T>;
    initialInput?: string;

    constructor(rowIndex: number,
        columnIndex: number,
        gridCell: GridCell<T>,
        initialInput?: string) {

        super(rowIndex, columnIndex);
        this.gridCell = gridCell;
        this.initialInput = initialInput;
    }
}

interface GridTableState<T> {
    gridData: GridData<T>;
    clientPosition: ClientPosition;
    visibleRect: Rectangle;
    focused: boolean;
    selection?: Selection;
    editSession?: EditSession<T>;
}

class GridTable<T> extends React.Component<GridTableProps<T>, GridTableState<T>> {
    private static readonly MIN_COLUMN_WIDTH = 32;

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

    static getDerivedStateFromProps<T>(nextProps: GridTableProps<T>,
        prevState: GridTableState<T>) {
        if (nextProps.data === prevState.gridData) {
            return null;
        }

        return {
            ...prevState,
            gridData: nextProps.data,
            selection: undefined,
            editingPosition: undefined,
        };
    }

    constructor(props: GridTableProps<T>) {
        super(props);

        this.state = {
            gridData: props.data,
            clientPosition: { top: 0, right: 0, bottom: 0, left: 0 },
            visibleRect: new RectangleImpl(0, 0, window.innerWidth, window.innerHeight),
            focused: false,
        };

        this.handleScroll = this.handleScroll.bind(this);
        this.handleClick = this.handleClick.bind(this);
        this.handleDoubleClick = this.handleDoubleClick.bind(this);
        this.handleMouseDown = this.handleMouseDown.bind(this);
        this.handleMouseMove = this.handleMouseMove.bind(this);
        this.handleMouseUp = this.handleMouseUp.bind(this);
        this.handleResize = this.handleResize.bind(this);
        this.handleFocus = this.handleFocus.bind(this);
        this.handleBlur = this.handleBlur.bind(this);
        this.handleKeyDown = this.handleKeyDown.bind(this);
        this.handleKeyPress = this.handleKeyPress.bind(this);
        this.handleSelect = this.handleSelect.bind(this);
        this.handleCopy = this.handleCopy.bind(this);
        this.handleCut = this.handleCut.bind(this);
        this.handlePaste = this.handlePaste.bind(this);
        this.handleDefaultEditChange = this.handleDefaultEditChange.bind(this);
        this.handleEndEdit = this.handleEndEdit.bind(this);
        this.handleCustomEditorSubmit = this.handleCustomEditorSubmit.bind(this);
        this.handleCustomEditorCancel = this.handleCustomEditorCancel.bind(this);
    }

    componentDidUpdate(prevProps: GridTableProps<T>) {
        this.fixLayoutIfNeeded(() => this.refreshLayout());
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

    public getSelection(): Selection | null {
        if (this.state.selection) {
            return this.state.selection.duplicate();
        } else {
            return null;
        }
    }

    public addRowsAbove() {
        this.assertEditable();
        const selection = this.state.selection;
        const [rowIndex, rowCount] = selection ?
            [selection.topRow(), selection.rowCount()] : [0, 1];
        if (this.addRows(rowIndex, rowCount)) {
            this.forceUpdate();
        }
    }

    public addRowsBelow() {
        this.assertEditable();
        let selection = this.state.selection;
        const [rowIndex, rowCount] = selection ?
            [selection.bottomRow() + 1, selection.rowCount()] : [0, 1];

        if (this.addRows(rowIndex, rowCount)) {
            if (selection) {
                if (selection.end.rowIndex === rowIndex - 1) {
                    selection.start.rowIndex = rowIndex;
                    selection.end.rowIndex = rowIndex + rowCount - 1;
                } else {
                    selection.end.rowIndex = rowIndex;
                    selection.start.rowIndex = rowIndex + rowCount - 1;
                }
            }
            this.forceUpdate();
        }
    }

    protected addRows(rowIndex: number, rowCount: number) {
        const gridData = this.state.gridData;
        if (typeof this.props.beforeAddRows === "function") {
            if (this.props.beforeAddRows(rowIndex, rowCount) === false) {
                return false;
            }
        }
        gridData.addRows(rowIndex, rowCount);
        if (typeof this.props.afterAddRows === "function") {
            this.props.afterAddRows(rowIndex, rowCount);
        }
        return true;
    }

    public removeRows() {
        this.assertEditable();
        let selection: Selection | undefined = this.assertAndGetSelection();
        let rowIndex = selection.topRow();
        const rowCount = selection.rowCount();

        if (typeof this.props.beforeRemoveRows === "function") {
            if (this.props.beforeRemoveRows(rowIndex, rowCount) === false) {
                return;
            }
        }

        const gridData = this.state.gridData;
        gridData.removeRows(rowIndex, rowCount);

        if (typeof this.props.afterRemoveRows === "function") {
            this.props.afterRemoveRows(rowIndex, rowCount);
        }

        if (rowIndex >= gridData.getRowCount()) {
            rowIndex = gridData.getRowCount() - 1;
        }
        if (rowIndex >= 0) {
            selection = new Selection(
                "row",
                new CellPosition(rowIndex, 0),
                new CellPosition(rowIndex, gridData.getLastColumnIndex()),
            );
        } else {
            selection = undefined;
        }
        this.setState({ gridData: gridData, selection });
    }

    public addColumnsBefore() {
        this.assertEditable();
        const selection = this.state.selection;
        const [columnIndex, columnCount] = selection ?
            [selection.leftColumn(), selection.columnCount()] : [0, 1];
        if (this.addColumns(columnIndex, columnCount)) {
            this.forceUpdate();
        }
    }

    public addColumnsAfter() {
        this.assertEditable();
        let selection = this.state.selection;
        const [columnIndex, columnCount] = selection ?
            [selection.rightColumn() + 1, selection.columnCount()] : [0, 1];

        if (this.addColumns(columnIndex, columnCount)) {
            if (selection) {
                if (selection.end.columnIndex === columnIndex - 1) {
                    selection.start.columnIndex = columnIndex;
                    selection.end.columnIndex = columnIndex + columnCount - 1;
                } else {
                    selection.end.columnIndex = columnIndex;
                    selection.start.columnIndex = columnIndex + columnCount - 1;
                }
            }
            this.forceUpdate();
        }
    }

    protected addColumns(columnIndex: number, columnCount: number) {
        const gridData = this.state.gridData;
        if (typeof this.props.beforeAddColumns === "function") {
            if (this.props.beforeAddColumns(columnIndex, columnCount) === false) {
                return false;
            }
        }
        gridData.addColumns(columnIndex, columnCount);
        if (typeof this.props.afterAddColumns === "function") {
            this.props.afterAddColumns(columnIndex, columnCount);
        }
        return true;
    }

    public removeColumns() {
        this.assertEditable();
        let selection: Selection | undefined = this.assertAndGetSelection();
        let columnIndex = selection.leftColumn();
        const columnCount = selection.columnCount();
        const gridData = this.state.gridData;

        if (typeof this.props.beforeRemoveColumns === "function") {
            if (this.props.beforeRemoveColumns(columnIndex, columnCount) === false) {
                return;
            }
        }

        gridData.removeColumns(columnIndex, columnCount);

        if (typeof this.props.afterRemoveColumns === "function") {
            this.props.afterRemoveColumns(columnIndex, columnCount);
        }

        if (columnIndex >= gridData.getColumnCount()) {
            columnIndex = gridData.getColumnCount() - 1;
        }
        if (columnIndex >= 0) {
            selection = new Selection(
                "column",
                new CellPosition(0, columnIndex),
                new CellPosition(gridData.getLastRowIndex(), columnIndex),
            );
        } else {
            selection = undefined;
        }
        this.setState({ gridData: gridData, selection });
    }

    public eraseSelectedCells() {
        this.assertEditable();
        const sel = this.state.selection;
        if (!sel) {
            return;
        }

        const top = sel.topRow(),
            right = sel.rightColumn(),
            bottom = sel.bottomRow(),
            left = sel.leftColumn();

        if (typeof this.props.beforeEraseCells === "function") {
            if (this.props.beforeEraseCells(top, right, bottom, left) === false) {
                return;
            }
        }

        const gridData = this.state.gridData;
        for (let rowIndex = top; rowIndex <= bottom; rowIndex++) {
            for (let colIndex = left; colIndex <= right; colIndex++) {
                const cellData = gridData.getCellData(rowIndex, colIndex);
                if (cellData) {
                    this.doEraseCellData(cellData);
                }
            }
        }

        if (typeof this.props.afterEraseCells === "function") {
            this.props.afterEraseCells(top, right, bottom, left);
        }

        this.setState({ gridData: gridData });
    }

    private doEraseCellData(gridCell: GridCell<T>) {
        gridCell.setData(undefined);
    }

    public refreshLayout() {
        this.updateVisibleRect(() => this.syncOverlays());
    }

    private fixLayoutIfNeeded(callbackAfterFixed?: () => void) {
        const table = this.shadowTableRef.current;
        if (!table) {
            return;
        }
        const thead = table.tHead;
        if (!thead) {
            throw new Error(); // should not happen.
        }
        const cells = thead.rows[0].cells;
        const cornerBoundingRect = cells[0].getBoundingClientRect();
        const gridData = this.state.gridData;
        gridData.setLeft(Math.ceil(cornerBoundingRect.width));
        gridData.setTop(Math.ceil(cornerBoundingRect.height));
        let offsetX = 0;
        for (let j = 1; j < cells.length; j++) {
            const cell = cells[j];
            const columnHeader = gridData.getColumnHeader(j - 1);
            columnHeader.setRectangle(
                /*   left */ offsetX,
                /*    top */ 0,
                /*  width */ columnHeader.getWidth() || Math.ceil(cell.getBoundingClientRect().width),
                /* height */ gridData.getTop()
            );
            offsetX = columnHeader.getRight();
        }
        let offsetY = 0;
        for (let i = 0; i < gridData.getRowCount(); i++) {
            const rowHeader = gridData.getRowHeader(i);
            rowHeader.setRectangle(
                /*   left */ 0,
                /*    top */ offsetY,
                /*  width */ gridData.getLeft(),
                /* height */ gridData.getTop()
            );
            offsetY = rowHeader.getBottom();
        }
        gridData.setWidth(offsetX);
        gridData.setHeight(offsetY);
        gridData.setArranged(true);

        this.setState({ gridData: gridData }, callbackAfterFixed);
    }

    private syncOverlays() {
        const scrollableContainer = this.assertAndGetScrollableContainer();
        const topOverlay = this.assertAndGetTopOverlay();
        const leftOverlay = this.assertAndGetLeftOverlay();

        topOverlay.scrollLeft = scrollableContainer.scrollLeft;
        leftOverlay.scrollTop = scrollableContainer.scrollTop;
    }

    private updateVisibleRect(callback?: () => void) {
        const sc = this.assertAndGetScrollableContainer();

        const clientPosition = this.state.clientPosition;
        clientPosition.top = sc.clientTop;
        clientPosition.right = sc.offsetWidth - sc.clientLeft - sc.clientWidth;
        clientPosition.bottom = sc.offsetHeight - sc.clientTop - sc.clientHeight;
        clientPosition.left = sc.clientLeft;

        const visibleRect = new RectangleImpl(
            sc.scrollLeft,
            sc.scrollTop,
            sc.clientWidth,
            sc.clientHeight
        );

        this.setState({
            clientPosition,
            visibleRect
        }, callback);
    }

    protected handleScroll(event: React.UIEvent<HTMLDivElement>) {
        this.syncOverlays();
        this.updateVisibleRect();
    }

    protected handleClick(e: React.MouseEvent<HTMLDivElement>) {
        // dummy
    }

    protected handleDoubleClick(e: React.MouseEvent<HTMLDivElement>) {
        if (!this.props.editable) {
            return;
        }

        // const coordinate = this.getCoordinateRelativeToScrollableContainer(e);
        const gridData = this.state.gridData;
        // const pos = tableData.getCellPosByCoordinate(coordinate.x, coordinate.y);
        // if (pos === null) {
        //     return;
        // }
        const target = e.target as Element;
        const dataCell = this.tryDataCellElement(target);

        if (dataCell) {
            const pos = new CellPosition(
                Number(dataCell.getAttribute("data-row-index")),
                Number(dataCell.getAttribute("data-column-index")),
            );

            if (pos.equalsTo(this.state.editSession)) {
                return;
            }

            const cellData = gridData.getCellData(pos.rowIndex, pos.columnIndex);
            const editSession = new EditSession<T>(pos.rowIndex, pos.columnIndex, cellData);
            this.setState({ editSession });
        }
    }

    protected onClickColumnHeader(columnIndex: number, e: React.MouseEvent) {
        const selection = this.state.selection;
        const startCellPos = new CellPosition(
            0,
            (e.shiftKey && selection) ? selection.start.columnIndex : columnIndex,
        );
        const endCellPos = new CellPosition(this.state.gridData.getLastRowIndex(), columnIndex);
        this.setSelection(new Selection("column", startCellPos, endCellPos));
    }

    protected onClickRowHeader(rowIndex: number, e: React.MouseEvent) {
        const selection = this.state.selection;
        const startCellPos = new CellPosition(
            (e.shiftKey && selection) ? selection.start.rowIndex : rowIndex,
            0,
        );
        const endCellPos = new CellPosition(rowIndex, this.state.gridData.getLastColumnIndex());
        this.setSelection(new Selection("row", startCellPos, endCellPos));
    }

    protected onClickDataCell(rowIndex: number, columnIndex: number, e: React.MouseEvent) {
        const endCellPos = new CellPosition(rowIndex, columnIndex);
        const startCellPos = (e.shiftKey && this.state.selection) ?
            this.state.selection.start : endCellPos.duplicate();
        this.setSelection(new Selection("cell", startCellPos, endCellPos)/*, true*/);
    }

    public setSelection(selection?: Selection, scrollIntoView: boolean = false) {
        if (!selection) {
            if (this.state.selection) {
                this.setState({ selection });
            }
            return;
        }

        if (!selection.equalsTo(this.state.selection)) {
            this.setState({ selection }, () => {
                if (scrollIntoView) {
                    if (selection.mode === "cell") {
                        this.scrollCellIntoView(selection.end.rowIndex, selection.end.columnIndex);
                    } else if (selection.mode === "column") {
                        this.scrollCellIntoView(NaN, selection.end.columnIndex);
                    } else if (selection.mode === "row") {
                        this.scrollCellIntoView(selection.end.rowIndex, NaN);
                    }
                }
            });
        }
    }

    public validSelection() {
        const selection = this.state.selection;
        if (!selection) {
            return;
        }
        const rowCount = this.state.gridData.getRowCount();
        const columnCount = this.state.gridData.getColumnCount();
        if (selection.start.rowIndex >= rowCount ||
            selection.start.columnIndex >= columnCount ||
            selection.end.rowIndex >= rowCount ||
            selection.end.columnIndex >= columnCount) {
            this.setSelection(undefined);
        }
    }

    public setFocused() {
        const textarea = this.assertAndGetInvisibleTextArea();
        textarea.focus();
    }

    protected handleMouseDown(e: React.MouseEvent<HTMLDivElement>) {
        if (this.state.editSession) {
            return;
        }

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

        const columnResizeHandle = this.tryColumnResizeHandleElement(target);
        const columnHeader = this.tryColumnHeaderElement(target);
        const rowHeader = this.tryRowHeaderElement(target);
        const dataCell = this.tryDataCellElement(target);

        if (columnResizeHandle) {
            // nothing to do

        } else if (columnHeader) {
            const columnIndex = Number(columnHeader.getAttribute("data-column-index"));
            this.onClickColumnHeader(columnIndex, e);

        } else if (rowHeader) {
            const rowIndex = Number(rowHeader.getAttribute("data-row-index"));
            this.onClickRowHeader(rowIndex, e);

        } else if (dataCell) {
            const rowIndex = Number(dataCell.getAttribute("data-row-index"));
            const columnIndex = Number(dataCell.getAttribute("data-column-index"));
            this.onClickDataCell(rowIndex, columnIndex, e);
        }
    }

    private tryRowHeaderElement(target: Element) {
        return this.getClosestElement(target, ".header-cell.row-header");
    }

    private tryColumnHeaderElement(target: Element) {
        return this.getClosestElement(target, ".header-cell.column-header");
    }

    private tryDataCellElement(target: Element) {
        return this.getClosestElement(target, ".data-cell");
    }

    private getClosestElement(target: Element, selector: string) {
        const masterContainer = this.assertAndGetMasterContainer();
        const dataCell = target.closest(selector);
        return (dataCell !== null && masterContainer.contains(dataCell)) ?
            dataCell : null;
    }

    protected handleMouseMove(e: MouseEvent) {
        if (!this.dragStart) {
            return;
        }

        const originTarget = this.dragStart.target;
        const scrollableContainer = this.assertAndGetScrollableContainer();
        const helperContainer = this.assertAndGetHelperContainer();
        const resizeHelper = this.assertAndGetResizeHelper();

        const gridData = this.state.gridData;

        const resizeHandle = this.tryColumnResizeHandleElement(originTarget);
        const dataCell = this.tryDataCellElement(originTarget);
        const rowHeader = this.tryRowHeaderElement(originTarget);
        const columnHeader = this.tryColumnHeaderElement(originTarget);

        if (resizeHandle) {
            const height = scrollableContainer.clientHeight;
            const left = e.clientX - 2 - helperContainer.getBoundingClientRect().left;

            Object.assign(resizeHelper.style, {
                display: "block",
                top: "0",
                left: left + "px",
                width: "0",
                height: height + "px",
            });

        } else if (dataCell) {
            const startCellPos = new CellPosition(
                Number(dataCell.getAttribute("data-row-index")),
                Number(dataCell.getAttribute("data-column-index")),
            );
            const coordinate = this.getCoordinateRelativeToScrollableContainer(e);
            const endCellPos = this.state.gridData.getNearestCellPosByCoordinate(coordinate.x, coordinate.y);

            this.setSelection(new Selection("cell", startCellPos, endCellPos), true);

        } else if (columnHeader) {
            const startColumn = Number(columnHeader.getAttribute("data-column-index"));
            const coordinate = this.getCoordinateRelativeToScrollableContainer(e);
            const endCellPos = gridData.getNearestCellPosByCoordinate(coordinate.x, coordinate.y);
            endCellPos.rowIndex = gridData.getLastRowIndex();

            this.setSelection(new Selection(
                "column",
                new CellPosition(0, startColumn),
                endCellPos,
            ), true);

        } else if (rowHeader) {
            const startRow = Number(rowHeader.getAttribute("data-row-index"));
            const coordinate = this.getCoordinateRelativeToScrollableContainer(e);
            const endCellPos = gridData.getNearestCellPosByCoordinate(coordinate.x, coordinate.y);
            endCellPos.columnIndex = this.state.gridData.getLastColumnIndex();

            this.setSelection(new Selection(
                "row",
                new CellPosition(startRow, 0),
                endCellPos
            ), true);

        }
    }

    protected tryColumnResizeHandleElement(target: Element) {
        return this.getClosestElement(target, ".resize-handle.column-resize-handle");
    }

    protected getCoordinateRelativeToScrollableContainer(e: MouseEvent | React.MouseEvent): Coordinate {
        const scrollableContainer = this.assertAndGetScrollableContainer();
        const rect = scrollableContainer.getBoundingClientRect();
        const x = e.clientX - rect.left + scrollableContainer.scrollLeft;
        const y = e.clientY - rect.top + scrollableContainer.scrollTop;
        return { x, y };
    }

    protected scrollCellIntoView(rowIndex: number, columnIndex: number) {
        const container = this.assertAndGetScrollableContainer();
        const gridData = this.state.gridData;
        let leftAnchor: number = NaN,
            rightAnchor: number = NaN,
            topAnchor: number = NaN,
            bottomAnchor: number = NaN;

        if (!isNaN(columnIndex)) {
            const columnHeader = gridData.getColumnHeader(columnIndex);
            leftAnchor = gridData.getLeft() + columnHeader.getLeft();
            rightAnchor = gridData.getLeft() + columnHeader.getRight();
        }
        if (!isNaN(rowIndex)) {
            const rowHeader = gridData.getRowHeader(rowIndex);
            topAnchor = gridData.getTop() + rowHeader.getTop();
            bottomAnchor = gridData.getTop() + rowHeader.getBottom();
        }

        let delay = 200;
        if (this.scrollIntoViewTimer !== null) {
            clearTimeout(this.scrollIntoViewTimer);
            delay = 10;
        }

        this.scrollIntoViewTimer = window.setTimeout(() => {
            const leftBound = container.scrollLeft + gridData.getLeft();
            const rightBound = container.scrollLeft + container.clientWidth;
            const topBound = container.scrollTop + gridData.getTop();
            const bottomBound = container.scrollTop + container.clientHeight;

            if (!isNaN(leftAnchor) && !isNaN(rightAnchor)) {
                if (leftAnchor < leftBound && rightAnchor < rightBound) {
                    container.scrollLeft = leftAnchor - gridData.getLeft();
                } else if (rightAnchor > rightBound && leftAnchor > leftBound) {
                    container.scrollLeft = rightAnchor - container.clientWidth;
                }
            }
            if (!isNaN(topAnchor) && !isNaN(bottomAnchor)) {
                if (topAnchor < topBound && bottomAnchor < bottomBound) {
                    container.scrollTop = topAnchor - gridData.getTop();
                } else if (bottomAnchor > bottomBound && topAnchor > topBound) {
                    container.scrollTop = bottomAnchor - container.clientHeight;
                }
            }

            if (this.scrollIntoViewTimer !== null) {
                clearTimeout(this.scrollIntoViewTimer);
            }

        }, delay);
    }

    protected getRelativeCellBoundingRect(rowIndex: number, columnIndex: number) {
        const gridData = this.state.gridData;
        const rowHeader = gridData.getRowHeader(rowIndex);
        const columnHeader = gridData.getColumnHeader(columnIndex);
        const top = gridData.getTop() + rowHeader.getTop();
        const left = gridData.getLeft() + columnHeader.getLeft();
        return new RectangleImpl(left, top, columnHeader.getWidth(), rowHeader.getHeight());
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

        const resizeHelper = this.assertAndGetResizeHelper();

        resizeHelper.style.display = "none";

        const originTarget = dragStart.target;
        const columnResizeHandle = this.tryColumnResizeHandleElement(originTarget);
        const columnHeaderElement = columnResizeHandle && this.tryColumnHeaderElement(columnResizeHandle);
        if (columnHeaderElement) {
            let w = e.pageX - columnHeaderElement.getBoundingClientRect().left;
            if (w < GridTable.MIN_COLUMN_WIDTH) {
                w = GridTable.MIN_COLUMN_WIDTH;
            }
            const index = Number(columnHeaderElement.getAttribute("data-column-index"));
            const gridData = this.state.gridData;
            let columnHeader = gridData.getColumnHeader(index);
            columnHeader.setWidth(w);
            let left = columnHeader.getLeft() + w;
            for (let j = index + 1; j < gridData.getColumnCount(); j++) {
                columnHeader = gridData.getColumnHeader(j);
                columnHeader.setLeft(left);
                left = columnHeader.getRight();
            }
            this.setState({ gridData: gridData }, () => this.refreshLayout());
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
        // console.log("Down: Key", e.key,
        //     "Code", e.keyCode,
        //     "meta", e.metaKey,
        //     "shift", e.shiftKey,
        //     "type", e.type,
        //     "which", e.which,
        //     "charCode", e.charCode,
        //     "repeat", e.repeat,
        //     "ms", e.getModifierState(e.key));

        if (this.state.editSession) {
            return;
        }

        if (e.key === "Enter") {
            this.onEnterKeyDown(e);

        } else if (e.key === "ArrowUp" || e.key === "ArrowDown" ||
            e.key === "ArrowLeft" || e.key === "ArrowRight") {
            this.onArrowKeyDown(e);

        } else if (e.key === "Home") {
            this.onHomeKeyDown(e);

        } else if (e.key === "End") {
            this.onEndKeyDown(e);

        } else if (e.key === "Delete" || e.key === "Backspace") {
            this.onEraseSelectedCells(e);
        }
    }

    private onEnterKeyDown(e: React.KeyboardEvent) {
        if (!this.state.selection) {
            return;
        }

        e.preventDefault();

        const pos = this.state.selection.start;
        const gridData = this.state.gridData;

        if (e.shiftKey) {
            const start = pos.duplicate();
            start.moveUp(0);
            const selection = new Selection("cell", start, start.duplicate());
            this.setSelection(selection, true);

        } else if (e.altKey && this.props.editable) {
            const cellData = gridData.getCellData(pos.rowIndex, pos.columnIndex);
            this.enterEditMode(pos.rowIndex, pos.columnIndex, cellData);
            return;

        } else {
            const start = pos.duplicate();
            start.moveDown(this.state.gridData.getLastRowIndex());
            const selection = new Selection(
                "cell",
                start,
                start.duplicate()
            );
            this.setSelection(selection, true);
        }
    }

    private onArrowKeyDown(e: React.KeyboardEvent) {
        const gridData = this.state.gridData;
        const selection = this.state.selection;

        if (!selection) {
            return;
        }

        e.preventDefault();

        // TODO
        // when metaKey/ctrlKey is on, move to head/tail instead
        // when altKey is down, apple's Number do row/column insert.

        const newSelection = selection.duplicate();
        const target = (e.shiftKey) ? newSelection.end : newSelection.start;

        if (!e.shiftKey || selection.mode !== "row") {
            if (e.key === "ArrowLeft") {
                if (e.ctrlKey || e.metaKey) {
                    target.columnIndex = 0;
                } else {
                    target.moveLeft(0);
                }
            } else if (e.key === "ArrowRight") {
                const maxColumnIndex = gridData.getLastColumnIndex();
                if (e.ctrlKey || e.metaKey) {
                    target.columnIndex = maxColumnIndex;
                } else {
                    target.moveRight(maxColumnIndex);
                }
            }
        }

        if (!e.shiftKey || selection.mode !== "column") {
            if (e.key === "ArrowUp") {
                if (e.ctrlKey || e.metaKey) {
                    target.rowIndex = 0;
                } else {
                    target.moveUp(0);
                }
            } else if (e.key === "ArrowDown") {
                const maxRowIndex = gridData.getLastRowIndex();
                if (e.ctrlKey || e.metaKey) {
                    target.rowIndex = maxRowIndex;
                } else {
                    target.moveDown(maxRowIndex);
                }
            }
        }

        if (!e.shiftKey) {
            newSelection.mode = "cell";
            newSelection.end = newSelection.start.duplicate();
        }

        this.setSelection(newSelection, true);
    }

    private onHomeKeyDown(e: React.KeyboardEvent) {
        let selection = this.state.selection;
        if (!selection) {
            return;
        }
        e.preventDefault();

        const cellPos = new CellPosition(selection.start.rowIndex, 0);
        selection = new Selection("cell", cellPos, cellPos);
        this.setSelection(selection, true);
    }

    private onEndKeyDown(e: React.KeyboardEvent) {
        let selection = this.state.selection;
        if (!selection) {
            return;
        }
        e.preventDefault();

        const gridData = this.state.gridData;
        const cellPos = new CellPosition(selection.start.rowIndex, gridData.getLastColumnIndex());
        selection = new Selection("cell", cellPos, cellPos);
        this.setSelection(selection, true);
    }

    private onEraseSelectedCells(e: React.KeyboardEvent) {
        const sel = this.state.selection;
        if (!this.props.editable || !sel) {
            return;
        }

        e.preventDefault();

        this.eraseSelectedCells();
    }

    protected handleKeyPress(e: React.KeyboardEvent) {
        // console.log("Press: Key", e.key,
        //     "Code", e.keyCode,
        //     "meta", e.metaKey,
        //     "shift", e.shiftKey,
        //     "type", e.type,
        //     "which", e.which,
        //     "charCode", e.charCode,
        //     "repeat", e.repeat,
        //     "ms", e.getModifierState(e.key));

        if (!this.props.editable || this.state.editSession || !this.state.selection) {
            return;
        }
        const pos = this.state.selection.start;
        const cellData = this.state.gridData.getCellData(pos.rowIndex, pos.columnIndex);
        const initialInput = String.fromCharCode(e.charCode);
        this.enterEditMode(pos.rowIndex, pos.columnIndex, cellData, initialInput);
    }

    protected handleSelect(e: React.SyntheticEvent<HTMLTextAreaElement>) {
        const textarea = this.assertAndGetInvisibleTextArea();
        const value = textarea.value;
        const length = Math.abs(textarea.selectionEnd - textarea.selectionStart);
        if (length === value.length) {
            this.selectAll();
        }
        textarea.setSelectionRange(0, 0);
    }

    public selectAll() {
        const gridData = this.state.gridData;
        if (gridData.getRowCount() === 0 || gridData.getColumnCount() === 0) {
            this.setSelection();

        } else {
            this.setSelection(new Selection(
                "cell",
                new CellPosition(0, 0),
                new CellPosition(
                    gridData.getLastRowIndex(),
                    gridData.getLastColumnIndex(),
                )
            ));
        }
    }

    protected handleCopy(e: React.ClipboardEvent) {
        e.preventDefault();

        const selection = this.state.selection;
        if (!selection) {
            return;
        }
        const gridData = this.state.gridData;
        //         let text = "",
        //             html = "<!doctype html><html><head>\
        // <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\
        // </head><body><table><tbody>\n";
        //         for (let i = selection.topRow(); i <= selection.bottomRow(); i++) {
        //             if (text.length > 0) {
        //                 text += "\r\n";
        //             }
        //             html += "<tr>";

        //             let line = "";
        //             for (let j = selection.leftColumn(); j <= selection.rightColumn(); j++) {
        //                 if (line.length > 0) {
        //                     line += ",";
        //                 }
        //                 const value = this.cellDataToCopyableString(tableData.getCellData(j, i));
        //                 line += EscapeHelper.escapeForCSV(value);
        //                 html += "<td>" + EscapeHelper.escapeForHtml(value) + "</td>";
        //             }
        //             text += line;
        //             html += "</tr>\n";
        //         }
        //         html += "</tbody></table></body></html>";

        const csv = gridData.toCsv(selection.topRow(),
            selection.rightColumn(),
            selection.bottomRow(),
            selection.leftColumn(),
            data => this.cellDataToCopyableString(data));

        const html = gridData.toHtml(selection.topRow(),
            selection.rightColumn(),
            selection.bottomRow(),
            selection.leftColumn(),
            data => this.cellDataToCopyableString(data));

        e.clipboardData.setData("text/plain", csv);
        e.clipboardData.setData("text/html", html);
    }

    protected handleCut(e: React.ClipboardEvent) {
        e.preventDefault();
        // TODO
    }

    protected handlePaste(e: React.ClipboardEvent) {
        e.preventDefault();
        // TODO
        // console.log("CD: html:", e.clipboardData.getData("text/html"),
        //     "text:", e.clipboardData.getData("text/plain"));
    }

    protected handleDefaultEditChange(change: ValueChange<string>) {
        if (change.type === "commit") {
            const editSession = this.assertAndGetEditSession();
            const newValue = this.editableStringToCellValue(change.toValue);
            this.setCellValue(editSession.rowIndex, editSession.columnIndex, newValue, false);

            this.exitEditMode();

        } else if (change.type === "rollback") {
            this.exitEditMode();
        }
    }

    protected setCellValue(rowIndex: number, columnIndex: number, newValue: T, forceUpdate: boolean = true) {
        const cellData = this.state.gridData.getCellData(rowIndex, columnIndex);
        const valueChange: ValueChange<T> = {
            type: "commit",
            fromValue: cellData.getData(),
            toValue: newValue
        };
        if (!this.beforeSetCellValue(valueChange, rowIndex, columnIndex)) {
            return;
        }
        this.doSetCellValue(rowIndex, columnIndex, valueChange.toValue);
        this.afterSetCellValue(cellData, rowIndex, columnIndex);

        if (forceUpdate) {
            this.forceUpdate();
        }
    }

    protected beforeSetCellValue(valueChange: ValueChange<T>, rowIndex: number, columnIndex: number) {
        if (typeof this.props.beforeSetCellValue === "function") {
            return this.props.beforeSetCellValue(valueChange, rowIndex, columnIndex);
        }
        return true;
    }

    protected doSetCellValue(rowIndex: number, columnIndex: number, newValue: T) {
        const cellData = this.state.gridData.getCellData(rowIndex, columnIndex);
        cellData.setData(newValue);
    }

    protected afterSetCellValue(cellData: GridCell<T>, rowIndex: number, columnIndex: number) {
        if (typeof this.props.afterSetCellValue === "function") {
            this.props.afterSetCellValue(cellData, rowIndex, columnIndex);
        }
    }

    protected handleEndEdit() {
        this.exitEditMode();
    }

    protected enterEditMode(rowIndex: number,
        columnIndex: number,
        cellData: GridCell<T>,
        initialInput?: string) {

        this.assertEditable();
        const editSession = new EditSession<T>(
            rowIndex,
            columnIndex,
            cellData,
            initialInput
        );
        this.setState({ editSession });
    }

    protected exitEditMode() {
        this.setState({
            editSession: undefined
        }, () => {
            this.setFocused()
        });
    }

    protected handleCustomEditorSubmit(value: T) {
        const editSession = this.assertAndGetEditSession();
        this.setCellValue(editSession.rowIndex, editSession.columnIndex, value, false);
        this.exitEditMode();
    }

    protected handleCustomEditorCancel() {
        this.exitEditMode();
    }

    protected cellDataToDisplayableString(cellData: GridCell<T>) {
        if (typeof this.props.forDisplay === "function") {
            return this.props.forDisplay(cellData.getData());
        } else {
            return cellData.getData() && String(cellData.getData()) || "";
        }
    }

    protected cellDataToEditableString(cellData: GridCell<T>) {
        if (typeof this.props.forEdit === "function") {
            return this.props.forEdit(cellData.getData());
        } else {
            return this.cellDataToDisplayableString(cellData);
        }
    }

    protected cellDataToCopyableString(cellData: GridCell<T>) {
        if (typeof this.props.forCopy === "function") {
            return this.props.forCopy(cellData.getData());
        } else {
            return this.cellDataToDisplayableString(cellData);
        }
    }

    protected editableStringToCellValue(editableString: string) {
        if (typeof this.props.fromEditableString === "function") {
            return this.props.fromEditableString(editableString);
        } else {
            throw new Error("Cannot convert editable string to cell value.");
        }
    }

    protected getColumnName(columnIndex: number, columnHeader?: Header) {
        return toColumnCode(columnIndex);
    }

    protected getRowName(rowIndex: number, rowHeader?: Header) {
        return String(rowIndex + 1);
    }

    render() {
        const focused = this.state.focused;
        const className = classnames(
            "fw-grid-table",
            this.props.className,
            {
                "editable": this.props.editable,
                "focused": focused
            }
        );
        const gridData = this.state.gridData;
        const clientPosition = this.state.clientPosition;
        const visibleRect = this.state.visibleRect;
        const renderRect = new RectangleImpl(
            visibleRect.getLeft() - visibleRect.getWidth(),
            visibleRect.getTop() - visibleRect.getHeight(),
            visibleRect.getWidth() * 3,
            visibleRect.getHeight() * 3
        );

        const cornerWidth = gridData.getLeft();
        const cornerHeight = gridData.getTop();

        const selection = this.state.selection;

        let dataCellSelectionRect = undefined;

        if (selection) {
            const firstColumn = gridData.getColumnHeader(selection.leftColumn());
            const lastColumn = gridData.getColumnHeader(selection.rightColumn());
            const firstRow = gridData.getRowHeader(selection.topRow());
            const lastRow = gridData.getRowHeader(selection.bottomRow());

            const left = cornerWidth + firstColumn.getLeft();
            const top = cornerHeight + firstRow.getTop();
            const width = lastColumn.getRight() - firstColumn.getLeft();
            const height = lastRow.getBottom() - firstRow.getTop();

            dataCellSelectionRect = new RectangleImpl(left, top, width, height);
        }

        const editSession = this.state.editSession;
        let editingRect = undefined;
        if (editSession) {
            editingRect = gridData.getCellOffsetRect(
                editSession.rowIndex,
                editSession.columnIndex,
            );
        }

        return (
            <div
                ref={this.masterContainerRef}
                className={className}
                style={{
                    ...this.props.style,
                    ...masterContainerStyle,
                }}
                onClick={this.handleClick}
                onDoubleClick={this.handleDoubleClick}
                onMouseDown={this.handleMouseDown}>
                <div
                    ref={this.scrollableContainerRef}
                    className="scrollable-container"
                    style={scrollableContainerStyle}
                    onScroll={this.handleScroll}>
                    {gridData.isArranged() ?
                        this.renderTableInWindow(false, false, renderRect) :
                        this.renderShadowTable()}
                    {dataCellSelectionRect && (
                        <div style={containerStyle}>
                            <div
                                className="selection-border selection-top-border"
                                style={{
                                    ...selectionBorderStyle,
                                    top: dataCellSelectionRect.getTop(),
                                    left: dataCellSelectionRect.getLeft(),
                                    width: dataCellSelectionRect.getWidth()
                                }}></div>
                            <div
                                className="selection-border selection-right-border"
                                style={{
                                    ...selectionBorderStyle,
                                    top: dataCellSelectionRect.getTop(),
                                    left: dataCellSelectionRect.getRight(),
                                    height: dataCellSelectionRect.getHeight()
                                }}></div>
                            <div
                                className="selection-border selection-bottom-border"
                                style={{
                                    ...selectionBorderStyle,
                                    top: dataCellSelectionRect.getBottom(),
                                    left: dataCellSelectionRect.getLeft(),
                                    width: dataCellSelectionRect.getWidth()
                                }}></div>
                            <div
                                className="selection-border selection-left-border"
                                style={{
                                    ...selectionBorderStyle,
                                    top: dataCellSelectionRect.getTop(),
                                    left: dataCellSelectionRect.getLeft(),
                                    height: dataCellSelectionRect.getHeight()
                                }}></div>
                        </div>
                    )}
                    {editingRect && (
                        <div
                            className="cell-edit-container"
                            style={{
                                ...editContainerStyle,
                                left: editingRect.getLeft(),
                                top: editingRect.getTop(),
                                width: editingRect.getWidth(),
                                height: editingRect.getHeight(),
                            }}>
                            {this.renderEditor()}
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
                        width: gridData.getLeft(),
                        height: gridData.getTop()
                    }}>
                    {this.renderTableInWindow(
                        true,
                        true,
                        new RectangleImpl(
                            0,
                            0,
                            gridData.getLeft(),
                            gridData.getTop(),
                        )
                    )}
                </div>
                <div
                    ref={this.topOverlayRef}
                    className="top-overlay"
                    style={{
                        ...headersOverlayStyle,
                        top: clientPosition.top,
                        right: clientPosition.right,
                        left: clientPosition.left,
                        height: gridData.getTop()
                    }}>
                    {this.renderTableInWindow(
                        true,
                        false,
                        new RectangleImpl(
                            renderRect.getLeft(),
                            0,
                            renderRect.getWidth(),
                            gridData.getTop(),
                        )
                    )}
                </div>
                <div
                    ref={this.leftOverlayRef}
                    className="left-overlay"
                    style={{
                        ...headersOverlayStyle,
                        top: clientPosition.top,
                        bottom: clientPosition.bottom,
                        left: clientPosition.left,
                        width: gridData.getLeft(),
                    }}>
                    {this.renderTableInWindow(
                        false,
                        true,
                        new RectangleImpl(
                            0,
                            renderRect.getTop(),
                            gridData.getLeft(),
                            renderRect.getHeight(),
                        )
                    )}
                </div>
                <div
                    ref={this.helperContainerRef}
                    className="fw-grid-table-helpers"
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
                        resize: "none",
                    }}
                    defaultValue={"ferris wheel"}
                    readOnly
                    tabIndex={-1}
                    onFocus={this.handleFocus}
                    onBlur={this.handleBlur}
                    onKeyDown={this.handleKeyDown}
                    onKeyPress={this.handleKeyPress}
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
        renderRect: Rectangle = new RectangleImpl(
            Number.NEGATIVE_INFINITY,
            Number.POSITIVE_INFINITY,
            Number.POSITIVE_INFINITY,
            Number.POSITIVE_INFINITY
        )) {

        const gridData = this.state.gridData;

        const offsetX = gridData.getLeft();
        const offsetY = gridData.getTop();

        const totalWidth = gridData.getRight();
        const totalHeight = gridData.getBottom();

        const showCorner = withColumnHeader && withRowHeader &&
            offsetX >= renderRect.getLeft() && offsetY >= renderRect.getTop();

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
                            width: gridData.getLeft(),
                            height: gridData.getTop(),
                        }}></div>
                )}
                {withColumnHeader && offsetY >= renderRect.getTop() && gridData.mapColumn((columnHeader, columnIndex) => {
                    const left = offsetX + columnHeader.getLeft();
                    if (left >= renderRect.getRight() || left + columnHeader.getWidth() < left) {
                        return null;
                    }
                    return (
                        <div
                            key={columnHeader.getId()}
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
                                width: columnHeader.getWidth(),
                                height: offsetY,
                            }}
                            data-column-index={columnIndex}>
                            {this.getColumnName(columnIndex, columnHeader)}
                            <div
                                className="resize-handle column-resize-handle"
                                style={columnResizeHandleStyle}></div>
                        </div>
                    );
                })}
                {gridData.mapRow((rowHeader, rowIndex) => {
                    const top = offsetY + rowHeader.getTop();
                    const bottom = top + rowHeader.getHeight();
                    if (top >= renderRect.getBottom() || bottom <= renderRect.getTop()) {
                        return null;
                    }

                    return (
                        <React.Fragment key={rowHeader.getId()}>
                            {withRowHeader && offsetX >= renderRect.getLeft() && (
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
                                        height: rowHeader.getHeight(),
                                    }}
                                    data-row-index={rowIndex}>
                                    {this.getRowName(rowIndex, rowHeader)}
                                    {/* <div
                                     className="resize-handle row-resize-handle" 
                                     style={rowResizeHandleStyle}></div> */}
                                </div>
                            )}
                            {gridData.mapColumn((columnHeader, columnIndex) => {
                                const left = offsetX + columnHeader.getLeft();
                                if (left >= renderRect.getRight() ||
                                    left + columnHeader.getWidth() < renderRect.getLeft()) {
                                    return null;
                                }
                                const cell = gridData.getCellData(rowIndex, columnIndex);
                                return this.renderCell(
                                    rowIndex,
                                    columnIndex,
                                    cell,
                                    left,
                                    top,
                                    columnHeader.getWidth(),
                                    rowHeader.getHeight());
                            })}
                        </React.Fragment>
                    );
                })}
            </div>
        );
    }

    private renderCell(
        rowIndex: number,
        columnIndex: number,
        cell: GridCell<T>,
        left: number,
        top: number,
        width: number,
        height: number) {

        return (
            <div
                key={cell.getId()}
                className={classnames(
                    "cell",
                    "data-cell",
                    "row-index-" + rowIndex,
                    "column-index-" + columnIndex,
                    "align-" + cell.getAlign(),
                )}
                style={{
                    ...cellStyle,
                    position: "absolute",
                    top,
                    left,
                    width,
                    height,
                }}
                data-row-index={rowIndex}
                data-column-index={columnIndex}>
                {this.renderCellValue(rowIndex, columnIndex, cell, width, height)}
            </div>
        );
    }

    private renderCellValue(
        rowIndex: number,
        columnIndex: number,
        cell: GridCell<T>,
        width?: number,
        height?: number) {

        if (this.props.customRenderer) {
            return (
                <this.props.customRenderer
                    rowIndex={rowIndex}
                    columnIndex={columnIndex}
                    data={cell.getData()}
                    align={cell.getAlign()}
                    width={width}
                    height={height} />
            );

        } else if (this.props.forDisplay) {
            return this.props.forDisplay(cell.getData());

        } else if (typeof cell.getData() !== "undefined" && cell.getData() !== null) {
            return String(cell.getData());

        } else {
            return "";
        }
    }

    private isColumnHighlighted(columnIndex: number) {
        const selection = this.state.selection;
        return selection && selection.isColumnCovered(columnIndex);
    }

    private isColumnSelected(columnIndex: number) {
        const selection = this.state.selection;
        return selection &&
            selection.mode === "column" &&
            selection.isColumnCovered(columnIndex);
    }

    private isRowHighlighted(rowIndex: number) {
        const selection = this.state.selection;
        return selection && selection.isRowCovered(rowIndex);
    }

    private isRowSelected(rowIndex: number) {
        const selection = this.state.selection;
        return selection &&
            selection.mode === "row" &&
            selection.isRowCovered(rowIndex);
    }

    private renderShadowTable() {
        const gridData = this.state.gridData;
        let longestRowHeaderName = "";
        gridData.forEachRow((rowHeader, index) => {
            const name = String(index + 1);
            if (typeof name === "string" && name.length > longestRowHeaderName.length) {
                longestRowHeaderName = name;
            }
        });

        const limitedRows = Math.max(3, 1000 / (gridData.getColumnCount() || 1));

        return (
            <table
                ref={this.shadowTableRef}
                cellPadding={0}
                cellSpacing={0}
                style={{
                    position: "absolute",
                    whiteSpace: "nowrap",
                    visibility: "visible",
                    borderCollapse: "separate",
                }}>
                <thead>
                    <tr style={rowStyle}>
                        <th className="cell header-cell corner"
                            style={cellStyle}>
                            &nbsp;
                        </th>
                        {gridData.mapColumn((columnHeader, columnIndex) => (
                            <th
                                key={columnHeader.getId()}
                                className={"cell header-cell column-header column-index-" + columnIndex}
                                style={cellStyle}
                                data-column-index={columnIndex}>
                                {this.getColumnName(columnIndex, columnHeader)}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {gridData.mapRow((rowHeader, rowIndex) => {
                        if (rowIndex > limitedRows) {
                            return null;
                        }
                        return (
                            <tr key={rowHeader.getId()}
                                className={"row row-index-" + rowIndex}
                                style={rowStyle}
                                data-row-index={rowIndex}>
                                <th className={"cell header-cell row-header row-index-" + rowIndex}
                                    style={cellStyle}>
                                    {longestRowHeaderName}
                                </th>
                                {gridData.mapColumn((columnHeader, columnIndex) => {
                                    const cell = gridData.getCellData(rowIndex, columnIndex);
                                    return (
                                        <td
                                            key={columnHeader.getId()}
                                            className={classnames(
                                                "cell",
                                                "data-cell",
                                                "row-index-" + rowIndex,
                                                "column-index-" + columnIndex,
                                                {
                                                    "align-right": cell.getAlign() === "right",
                                                    "align-center": cell.getAlign() === "center"
                                                }
                                            )}
                                            style={cellStyle}
                                            data-column-index={columnIndex}>
                                            {this.renderCellValue(rowIndex, columnIndex, cell)}
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

    private renderEditor() {
        const editSession = this.assertAndGetEditSession();
        const cellData = editSession.gridCell;
        const gridData = this.state.gridData;
        const width = gridData.getColumnHeader(editSession.columnIndex).getWidth();
        const height = gridData.getRowHeader(editSession.rowIndex).getHeight();

        if (this.props.customEditor) {
            return (
                <this.props.customEditor
                    rowIndex={editSession.rowIndex}
                    columnIndex={editSession.columnIndex}
                    data={cellData.getData()}
                    initialInput={editSession.initialInput}
                    align={cellData.getAlign()}
                    width={width}
                    height={height}
                    onOk={this.handleCustomEditorSubmit}
                    onCancel={this.handleCustomEditorCancel} />
            );
        }

        return (
            <EditBox
                style={defaultEditStyle}
                value={this.cellDataToEditableString(cellData)}
                initialUpdate={editSession.initialInput}
                multiline={true}
                forceCommitOnEnter={true}
                focusByDefault={true}
                afterChange={this.handleDefaultEditChange}
                afterEndEdit={this.handleEndEdit} />
        );
    }

    protected assertEditable() {
        if (!this.props.editable) {
            throw new Error("This table is not editable!");
        }
    }

    protected assertAndGetSelection() {
        const selection = this.state.selection;
        if (!selection) {
            throw new Error("No selection.");
        }
        return selection;
    }

    protected assertAndGetEditSession() {
        const editSession = this.state.editSession;
        if (!editSession) {
            throw new Error("No edit session.");
        }
        return editSession;
    }

    protected assertAndGetMasterContainer() {
        return this.assertAndGetRef(this.masterContainerRef);
    }

    protected assertAndGetInvisibleTextArea() {
        return this.assertAndGetRef(this.invisibleTextAreaRef);
    }

    protected assertAndGetScrollableContainer() {
        return this.assertAndGetRef(this.scrollableContainerRef);
    }

    protected assertAndGetCornerOverlay() {
        return this.assertAndGetRef(this.cornerOverlayRef);
    }

    protected assertAndGetTopOverlay() {
        return this.assertAndGetRef(this.topOverlayRef);
    }

    protected assertAndGetLeftOverlay() {
        return this.assertAndGetRef(this.leftOverlayRef);
    }

    protected assertAndGetHelperContainer() {
        return this.assertAndGetRef(this.helperContainerRef);
    }

    protected assertAndGetResizeHelper() {
        return this.assertAndGetRef(this.resizeHelperRef);
    }

    protected assertAndGetRef<T>(ref: React.RefObject<T>): T {
        const obj = ref.current;
        if (obj === null) {
            throw new Error("Referred object not available.");
        }
        return obj;
    }

}

export default GridTable;
export { GridTableProps, CustomRendererProps, CustomEditorProps };
