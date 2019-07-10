
class CellPosition {
    rowIndex: number;
    columnIndex: number;

    constructor(rowIndex: number, columnIndex: number) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    public equalsTo(another?: CellPosition | null) {
        if (!another) {
            return false;
        }
        return this.rowIndex === another.rowIndex &&
            this.columnIndex === another.columnIndex;
    }

    public moveUp(min: number) {
        if (this.rowIndex > min) {
            this.rowIndex--;
        }
    }

    public moveDown(max: number) {
        if (this.rowIndex < max) {
            this.rowIndex++;
        }
    }

    public moveLeft(min: number) {
        if (this.columnIndex > min) {
            this.columnIndex--;
        }
    }

    public moveRight(max: number) {
        if (this.columnIndex < max) {
            this.columnIndex++;
        }
    }

    public duplicate() {
        return new CellPosition(this.rowIndex, this.columnIndex);
    }
}

export default CellPosition;
