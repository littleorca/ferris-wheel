import UnionValue from "./UnionValue";
import Values from "./Values";

class Series {
    public name: UnionValue;
    public xValues: UnionValue;
    public yValues: UnionValue;
    public zValues: UnionValue;

    public static deserialize(input: any): Series {
        const name = typeof input.name !== 'undefined' ?
            Values.deserialize(input.name) : undefined;
        const xValues = typeof input.xValues !== 'undefined' ?
            Values.deserialize(input.xValues) : undefined;
        const yValues = typeof input.yValues !== 'undefined' ?
            Values.deserialize(input.yValues) : undefined;
        const zValues = typeof input.zValues !== 'undefined' ? Values.deserialize(input.zValues) : undefined;

        return new Series(
            name,
            xValues,
            yValues,
            zValues,
        );
    }

    constructor(
        name: UnionValue = Values.blank(),
        xValues: UnionValue = Values.blank(),
        yValues: UnionValue = Values.blank(),
        zValues: UnionValue = Values.blank()) {

        this.name = name;
        this.xValues = xValues;
        this.yValues = yValues;
        this.zValues = zValues;
    }

}

export default Series;
