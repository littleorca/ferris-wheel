import Display from "./Display";
import Placement from "./Placement";
import Grid from "./Grid";

class Layout {
    public display: Display;
    public width: number;
    public height: number;
    public align: Placement;
    public verticalAlign: Placement;
    public grid: Grid;

    public static deserialize(input: any): Layout {
        const display = input.display;
        const width = input.width;
        const height = input.height;
        const align = input.align;
        const verticalAlign = input.verticalAlign;
        const grid = typeof input.grid !== 'undefined' ?
            Grid.deserialize(input.grid) : undefined;
        return new Layout(display, width, height, align, verticalAlign, grid);
    }

    constructor(
        display: Display = Display.UNSET,
        width: number = 0,
        height: number = 0,
        align: Placement = Placement.UNSET,
        verticalAlign: Placement = Placement.UNSET,
        grid: Grid = new Grid()) {

        this.display = display;
        this.width = width;
        this.height = height;
        this.align = align;
        this.verticalAlign = verticalAlign;
        this.grid = grid;
    }

    public clone() {
        return new Layout(this.display,
            this.width,
            this.height,
            this.align,
            this.verticalAlign,
            this.grid.clone());
    }
}

export default Layout;
