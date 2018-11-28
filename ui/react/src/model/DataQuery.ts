import NamedValue from "./NamedValue";

class DataQuery {
    public scheme: string;
    public params: NamedValue[];

    public static deserialize(input: any): DataQuery {
        const scheme = input.scheme;
        const params = [];
        if (typeof input.params !== 'undefined') {
            for (const param of input.params) {
                params.push(NamedValue.deserialize(param));
            }
        }
        return new DataQuery(scheme, params);
    }

    constructor(
        scheme: string = '',
        params: NamedValue[] = []) {

        this.scheme = scheme;
        this.params = params;
    }
}

export default DataQuery;
