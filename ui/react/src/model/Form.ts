import FormField from "./FormField";
import Layout from "./Layout";

class Form {
    public name: string;
    public fields: FormField[];
    public layout: Layout;

    public static deserialize(input: any): Form {
        const name = input.name;
        const fields = [];
        if (typeof input.fields !== 'undefined' && input.fields !== null) {
            for (const field of input.fields) {
                fields.push(FormField.deserialize(field));
            }
        }
        const layout = typeof input.layout !== 'undefined' ?
            Layout.deserialize(input.layout) : undefined;

        return new Form(
            name,
            fields,
            layout,
        );
    }

    constructor(
        name: string = '',
        fields: FormField[] = [],
        layout: Layout = new Layout()) {

        this.name = name;
        this.fields = fields;
        this.layout = layout;
    }

    clone() {
        return Form.deserialize(this);
    }
}

export default Form;
