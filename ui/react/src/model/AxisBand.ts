import Interval from "./Interval";
import Color from "./Color";

class AxisBand {
    public interval: Interval;
    public label: string;
    public color: Color;

    public static deserialize(input: any): AxisBand {
        const interval = Interval.deserialize(input.interval);
        const label = input.label;
        const color = typeof input.color !== 'undefined' ?
            Color.deserialize(input.color) : undefined;

        return new AxisBand(interval, label, color);
    }

    constructor(
        interval: Interval = new Interval(),
        label: string = '',
        color: Color = new Color()) {

        this.interval = interval;
        this.label = label;
        this.color = color;
    }
}

export default AxisBand;
