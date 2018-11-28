class PivotFilter {
    public field: string;
    // TBD

    public static deserialize(input: any): PivotFilter {
        const field = input.field;
        return new PivotFilter(field);
    }

    constructor(
        field: string = '') {

        this.field = field;
    }

}

export default PivotFilter;
