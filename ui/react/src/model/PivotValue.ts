import AggregateType from "./AggregateType";

class PivotValue {
    public field: string;
    public aggregateType: AggregateType;
    public label: string;
    public format: string;

    public static deserialize(input: any): PivotValue {
        const field = input.field;
        const aggregateType = input.aggregateType;
        const label = input.label;
        const format = input.format;
        return new PivotValue(field, aggregateType, label, format);
    }

    constructor(
        field: string = '',
        aggregateType: AggregateType = AggregateType.SUMMARY,
        label: string = '',
        format: string = '') {

        this.field = field;
        this.aggregateType = aggregateType;
        this.label = label;
        this.format = format;
    }

}

export default PivotValue;
