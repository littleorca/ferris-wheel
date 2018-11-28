import { VariantType } from './Variant';
import UnionValue from './UnionValue';

class BlankValue extends UnionValue {
    constructor(formulaString?: string) {
        super(formulaString);
    }

    public valueType() {
        return VariantType.BLANK;
    }
}

export default BlankValue;
