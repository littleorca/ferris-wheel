import Parameter from "./Parameter";

class QueryTemplate {
    public scheme: string;
    public builtinParams: Parameter[];

    public static deserialize(input: any): QueryTemplate {
        const scheme = input.scheme;
        const builtinParams = [];
        if (typeof input.builtinParams !== 'undefined' && input.builtinParams !== null) {
            for (const param of input.builtinParams) {
                builtinParams.push(Parameter.deserialize(param));
            }
        }
        return new QueryTemplate(scheme, builtinParams);
    }

    constructor(
        scheme: string = '',
        builtinParams: Parameter[] = []) {

        this.scheme = scheme;
        this.builtinParams = builtinParams;
    }
}

export default QueryTemplate;
