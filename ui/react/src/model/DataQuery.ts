import Parameter from "./Parameter";

class DataQuery {
    public scheme: string;
    public params: Parameter[];

    public static deserialize(input: any): DataQuery {
        const scheme = input.scheme;
        const params = [];
        if (typeof input.params !== 'undefined') {
            for (const param of input.params) {
                params.push(Parameter.deserialize(param));
            }
        }
        return new DataQuery(scheme, params);
    }

    constructor(
        scheme: string = '',
        params: Parameter[] = []) {

        this.scheme = scheme;
        this.params = params;
    }
}

export default DataQuery;
