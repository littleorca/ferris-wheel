import UnionValue from "./UnionValue";
import Series from "./Series";
import Layout from "./Layout";
import Binder from "./Binder";
import Axis from "./Axis";
import Placement from "./Placement";
import Values from './Values';

class Chart {
    public name: string;
    public type: string;
    public title: UnionValue;
    public categories: UnionValue;
    public series: Series[];
    public layout: Layout;
    public binder: Binder;
    public xAxis: Axis;
    public yAxis: Axis;
    public zAxis: Axis;

    public static deserialize(input: any): Chart {
        const name = input.name;
        const type = input.type;
        const title = typeof input.title !== 'undefined' ?
            Values.deserialize(input.title) : undefined;
        const categories = typeof input.categories !== 'undefined' ?
            Values.deserialize(input.categories) : undefined;
        const series = [];
        if (typeof input.series !== 'undefined' && input.series !== null) {
            for (const ser of input.series) {
                series.push(Series.deserialize(ser));
            }
        }
        const layout = typeof input.layout !== 'undefined' ?
            Layout.deserialize(input.layout) : undefined;
        const binder = typeof input.binder !== 'undefined' ?
            Binder.deserialize(input.binder) : undefined;
        const xAxis = typeof input.xAxis !== 'undefined' ?
            Axis.deserialize(input.xAxis) : undefined;
        const yAxis = typeof input.yAxis !== 'undefined' ?
            Axis.deserialize(input.yAxis) : undefined;
        const zAxis = typeof input.zAxis !== 'undefined' ?
            Axis.deserialize(input.zAxis) : undefined;

        return new Chart(
            name,
            type,
            title,
            categories,
            series,
            layout,
            binder,
            xAxis,
            yAxis,
            zAxis,
        );
    }

    constructor(
        name: string = '',
        type: string = 'Line',
        title: UnionValue = Values.blank(),
        categories: UnionValue = Values.blank(),
        series: Series[] = [],
        layout: Layout = new Layout(),
        binder: Binder = new Binder(),
        xAxis: Axis = new Axis('', '', Placement.BOTTOM),
        yAxis: Axis = new Axis('', '', Placement.LEFT),
        zAxis: Axis = new Axis('', '', Placement.UNSET)) {

        this.name = name;
        this.type = type;
        this.title = title;
        this.categories = categories;
        this.series = series;
        this.layout = layout;
        this.binder = binder;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;
    }

    public clone() {
        return Chart.deserialize(this);
    }
}

export default Chart;
