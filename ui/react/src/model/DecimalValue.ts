import { VariantType } from "./Variant";
import UnionValue from "./UnionValue";

class DecimalValue extends UnionValue {
    public decimal: string;

    constructor(decimal: string, formulaString?: string) {
        super(formulaString);
        this.decimal = decimal;
    }

    public valueType() {
        return VariantType.DECIMAL;
    }

    public decimalValue() {
        return this.decimal;
    }

    public numberValue(): number {
        return parseFloat(this.decimal);
    }
}

export default DecimalValue;
