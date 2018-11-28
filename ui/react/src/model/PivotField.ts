class PivotField {
    public field: string;
    // public label: string;

    public static deserialize(input: any): PivotField {
        const field = input.field;
        // const label = input.label;
        return new PivotField(field/*, label*/);
    }

    constructor(
        field: string = '',
        label: string = '') {

        this.field = field;
        // this.label = label;
    }

}

export default PivotField;
