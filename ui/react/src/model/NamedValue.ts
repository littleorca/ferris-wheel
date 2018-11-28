import UnionValue from "./UnionValue";
import Values from "./Values";

class NamedValue {
    public name: string;
    public value: UnionValue;

    public static deserialize(input: any): NamedValue {
        const name = input.name;
        const value = typeof input.value !== 'undefined' ?
            Values.deserialize(input.value) : undefined;
        return new NamedValue(name, value);
    }

    constructor(
        name: string = '',
        value: UnionValue = Values.blank()) {

        this.name = name;
        this.value = value;
    }
}

export default NamedValue;
