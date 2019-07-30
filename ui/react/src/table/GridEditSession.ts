import CellPosition from "../model/CellPosition";
import GridCell from "../model/GridCell";

class GridEditSession<T> extends CellPosition {
    public gridCell: GridCell<T>;
    public initialInput?: string;

    constructor(rowIndex: number,
        columnIndex: number,
        gridCell: GridCell<T>,
        initialInput?: string) {

        super(rowIndex, columnIndex);
        this.gridCell = gridCell;
        this.initialInput = initialInput;
    }
}

export default GridEditSession;
