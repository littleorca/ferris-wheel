import AggregateType from "./AggregateType";

class PivotValue {
    public field: string;
    public aggregateType: AggregateType;
    public label: string;

    public static deserialize(input: any): PivotValue {
        const field = input.field;
        const aggregateType = input.aggregateType;
        const label = input.label;
        return new PivotValue(field, aggregateType, label);
    }

    constructor(
        field: string = '',
        aggregateType: AggregateType = AggregateType.SUMMARY,
        label: string = '') {

        this.field = field;
        this.aggregateType = aggregateType;
        this.label = label;
    }

}

export default PivotValue;
