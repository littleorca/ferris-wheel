import NamedValue from "./NamedValue";
import ParamRule from "./ParamRule";

class QueryTemplate {
    public scheme: string;
    public builtinParams: NamedValue[];
    public userParamRules: ParamRule[];

    public static deserialize(input: any): QueryTemplate {
        const scheme = input.scheme;
        const builtinParams = [];
        if (typeof input.builtinParams !== 'undefined' && input.builtinParams !== null) {
            for (const param of input.builtinParams) {
                builtinParams.push(NamedValue.deserialize(param));
            }
        }
        const userParamRules = [];
        if (typeof input.userParamRules !== 'undefined' && input.userParamRules !== null) {
            for (const rule of input.userParamRules) {
                userParamRules.push(ParamRule.deserialize(rule));
            }
        }
        return new QueryTemplate(scheme, builtinParams, userParamRules);
    }

    constructor(
        scheme: string = '',
        builtinParams: NamedValue[] = [],
        userParamRules: ParamRule[] = []) {

        this.scheme = scheme;
        this.builtinParams = builtinParams;
        this.userParamRules = userParamRules;
    }
}

export default QueryTemplate;
