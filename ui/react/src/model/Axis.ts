import Placement from "./Placement";
import Interval from "./Interval";
import AxisBand from "./AxisBand";
import Stacking from "./Stacking";

class Axis {
    public title: string;
    public label: string;
    public placement: Placement;
    public reversed: boolean;
    public interval: Interval;
    public bands: AxisBand[];
    public stacking: Stacking;
    public format: string;

    public static deserialize(input: any): Axis {
        const title = input.title;
        const label = input.label;
        const placement = input.placement;
        const reversed = input.reversed;
        const interval = typeof input.interval !== 'undefined' ?
            Interval.deserialize(input.interval) : undefined;
        const bands = [];
        if (typeof input.bands !== 'undefined') {
            for (const band of input.bands) {
                bands.push(AxisBand.deserialize(band));
            }
        }
        const stacking = input.stacking;
        const format = typeof input.format === 'string' ? input.format : '';
        return new Axis(
            title,
            label,
            placement,
            reversed,
            interval,
            bands,
            stacking,
            format
        );
    }

    constructor(
        title: string = '',
        label: string = '',
        placement: Placement = Placement.UNSET,
        reversed: boolean = false,
        interval: Interval = new Interval(),
        bands: AxisBand[] = [],
        stacking: Stacking = Stacking.UNSET,
        format: string = '') {

        this.title = title;
        this.label = label;
        this.placement = placement;
        this.reversed = reversed;
        this.interval = interval;
        this.bands = bands;
        this.stacking = stacking;
        this.format = format;
    }
}

export default Axis;
