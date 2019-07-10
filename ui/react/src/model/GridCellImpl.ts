import GridCell, { CellAlign } from "./GridCell";

class GridCellImpl<T> implements GridCell<T> {
    private static sequence: number = 0;

    private id: number;
    private data?: T;
    private align: CellAlign;

    constructor(data?: T, align: CellAlign = "left") {
        this.id = GridCellImpl.sequence++;
        this.data = data;
        this.align = align;
    }

    public getId() {
        return this.id;
    }

    public getData(): T | undefined {
        return this.data;
    }

    public setData(data?: T) {
        this.data = data;
    }

    public getAlign(): CellAlign {
        return this.align;
    }

    public setAlign(align: CellAlign) {
        this.align = align;
    }
}

export default GridCellImpl;
