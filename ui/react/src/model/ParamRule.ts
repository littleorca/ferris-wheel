import { VariantType } from "./Variant";
import UnionValue from "./UnionValue";
import Values from './Values';

class ParamRule {
    public name: string;
    public type: VariantType;
    public nullable: boolean;
    public allowedValues: UnionValue[];

    public static deserialize(input: any) {
        const name = input.name;
        const type = input.type;
        const nullable = input.nullable;
        const allowedValues = [];
        for (const value of input.allowedValues) {
            allowedValues.push(Values.deserialize(value));
        }
        return new ParamRule(name, type, nullable, allowedValues);
    }

    constructor(
        name: string = '',
        type: VariantType = VariantType.BLANK,
        nullable: boolean = false,
        allowedValues: UnionValue[] = []) {

        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.allowedValues = allowedValues;
    }
}

export default ParamRule;
