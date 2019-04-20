import UnionValue from "./UnionValue";
import Values from "./Values";
import { VariantType } from "./Variant";

class Parameter {
    public name: string;
    public value: UnionValue;
    public type: VariantType;
    public mandatory: boolean;
    public multiple: boolean;

    public static deserialize(input: any): Parameter {
        const name = input.name as string;
        const value = typeof input.value !== 'undefined' ?
            Values.deserialize(input.value) : undefined;
        const type = input.type as VariantType;
        const mandatory = !!input.mandatory
        const multiple = !!input.multiple;
        return new Parameter(name, value, type, mandatory, multiple);

    }

    constructor(name: string = "",
        value: UnionValue = Values.blank(),
        type: VariantType = VariantType.BLANK,
        mandatory: boolean = false,
        multiple: boolean = false) {

        this.name = name;
        this.value = value;
        this.type = type;
        this.mandatory = mandatory;
        this.multiple = multiple;
    }

}

export default Parameter;
