class FormFieldBinding {
    public target: string;

    public static deserialize(input: any): FormFieldBinding {
        const target = input.target;
        return new FormFieldBinding(target);
    }

    constructor(target: string = '') {
        this.target = target;
    }
}

export default FormFieldBinding;