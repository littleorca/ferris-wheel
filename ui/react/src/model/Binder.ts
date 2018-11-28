import UnionValue from './UnionValue';
import Orientation from './Orientation';
import Placement from './Placement';
import Values from './Values';

class Binder {
    public data: UnionValue;
    public orientation: Orientation;
    public categoriesPlacement: Placement;
    public seriesNamePlacement: Placement;

    public static deserialize(input: any): Binder {
        const data = typeof input.data !== 'undefined' ?
            Values.deserialize(input.data) : undefined;
        const orientation = input.orientation as Orientation || Orientation.UNSET;
        const categoriesPlacement = input.categoriesPlacement as Placement || Placement.UNSET;
        const seriesNamePlacement = input.seriesNamePlacement as Placement || Placement.UNSET;

        return new Binder(
            data,
            orientation,
            categoriesPlacement,
            seriesNamePlacement,
        );
    }

    constructor(
        data: UnionValue = Values.blank(),
        orientation: Orientation = Orientation.HORIZONTAL,
        categoriesPlacement: Placement = Placement.TOP,
        seriesNamePlacement: Placement = Placement.LEFT) {

        this.data = data;
        this.orientation = orientation;
        this.categoriesPlacement = categoriesPlacement;
        this.seriesNamePlacement = seriesNamePlacement;
    }
}

export default Binder;
