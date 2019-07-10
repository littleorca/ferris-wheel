
type CellAlign = "left" | "center" | "right";

interface GridCell<T> {
    getId(): number;

    getData(): T | undefined;

    setData(data?: T): void;

    getAlign(): CellAlign;

    setAlign(align: CellAlign): void;
}

export default GridCell;
export { CellAlign };
