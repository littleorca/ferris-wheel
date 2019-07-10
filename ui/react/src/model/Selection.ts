import CellPosition from "./CellPosition";

type SelectMode = "cell" | "column" | "row";
type HorizontalOrientation = "left" | "right" | "N/A";
type VerticalOrientation = "up" | "down" | "N/A";

class Selection {
    mode: SelectMode;
    start: CellPosition;
    end: CellPosition;

    constructor(mode: SelectMode, start: CellPosition, end: CellPosition) {
        this.mode = mode;
        this.start = start;
        this.end = end;
    }

    public equalsTo(another?: Selection | null) {
        if (!another) {
            return false;
        }
        return this.mode === another.mode &&
            this.start.equalsTo(another.start) &&
            this.end.equalsTo(another.end);
    }

    public leftColumn() {
        return Math.min(this.start.columnIndex, this.end.columnIndex);
    }

    public rightColumn() {
        return Math.max(this.start.columnIndex, this.end.columnIndex);
    }

    public topRow() {
        return Math.min(this.start.rowIndex, this.end.rowIndex);
    }

    public bottomRow() {
        return Math.max(this.start.rowIndex, this.end.rowIndex);
    }

    public columnCount() {
        return this.rightColumn() + 1 - this.leftColumn();
    }

    public isColumnCovered(columnIndex: number) {
        return columnIndex >= this.leftColumn() && columnIndex <= this.rightColumn();
    }

    public rowCount() {
        return this.bottomRow() + 1 - this.topRow();
    }

    public isRowCovered(rowIndex: number) {
        return rowIndex >= this.topRow() && rowIndex <= this.bottomRow();
    }

    public isCellCovered(rowIndex: number, columnIndex: number) {
        return this.isRowCovered(rowIndex) && this.isColumnCovered(columnIndex);
    }

    public horizontalOrientation(): HorizontalOrientation {
        if (this.mode === "row") {
            return "N/A";
        }
        const flag = this.start.columnIndex - this.end.columnIndex;
        return flag < 0 ? "right" : flag > 0 ? "left" : "N/A";
    }

    public verticalOrientation(): VerticalOrientation {
        if (this.mode === "column") {
            return "N/A";
        }
        const flag = this.start.rowIndex - this.end.rowIndex;
        return flag < 0 ? "down" : flag > 0 ? "up" : "N/A";
    }

    public duplicate() {
        return new Selection(
            this.mode,
            this.start.duplicate(),
            this.end.duplicate()
        );
    }
}

export default Selection;
