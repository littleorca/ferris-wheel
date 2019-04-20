import { VariantType } from "./Variant";
import UnionValue from "./UnionValue";
import Values from "./Values";
import FormFieldBinding from "./FormFieldBinding";

class FormField {
    public name: string;
    public type: VariantType;
    public value: UnionValue;
    public mandatory: boolean;
    public multiple: boolean;
    public label: string;
    public tips: string;
    public options: UnionValue;
    public bindings: FormFieldBinding[];

    public static deserialize(input: any): FormField {
        const name = input.name;
        const type = input.type as VariantType;
        const value = Values.deserialize(input.value);
        const mandatory = input.mandatory;
        const multiple = input.multiple;
        const label = input.label;
        const tips = input.tips;
        const options = Values.deserialize(input.options);

        const bindings: FormFieldBinding[] = [];
        if (Array.isArray(input.bindings)) {
            for (let i = 0; i < input.bindings.length; i++) {
                const binding = input.bindings[i];
                bindings.push(FormFieldBinding.deserialize(binding));
            };
        }

        return new FormField(
            name,
            type,
            value,
            mandatory,
            multiple,
            label,
            tips,
            options,
            bindings,
        );
    }

    constructor(
        name: string = '',
        type: VariantType = VariantType.BLANK,
        value: UnionValue = Values.blank(),
        mandatory: boolean = false,
        multiple: boolean = false,
        label: string = '',
        tips: string = '',
        options: UnionValue = Values.blank(),
        bindings: FormFieldBinding[] = [],
    ) {

        this.name = name;
        this.type = type;
        this.value = value;
        this.mandatory = mandatory;
        this.multiple = multiple;
        this.label = label;
        this.tips = tips;
        this.options = options;
        this.bindings = bindings;
    }

}

export default FormField;
