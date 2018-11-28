import { VariantType } from "./Variant";
import UnionValue from "./UnionValue";

class BooleanValue extends UnionValue {
    public boolean: boolean;

    constructor(bool: boolean, formulaString?: string) {
        super(formulaString);
        this.boolean = bool;
    }

    public valueType() {
        return VariantType.BOOL;
    }

    public booleanValue() {
        return this.boolean;
    }
}

export default BooleanValue;
