import * as React from "react";
import EditableList, { EditorProps } from "../ctrl/EditableList";
import Form from "../model/Form";
import FormField from "../model/FormField";
import FormFieldForm from "../form/FormFieldForm";
import Field from "../form/Field";
// import EditBox from "../ctrl/EditBox";

interface FormFormProps extends React.ClassAttributes<FormForm> {
    form: Form;
    afterChange?: (form: Form) => void;
}

class FormForm extends React.Component<FormFormProps> {
    constructor(props: FormFormProps) {
        super(props);

        this.getFormFieldLabel = this.getFormFieldLabel.bind(this);
        this.createFormField = this.createFormField.bind(this);
        this.afterChange = this.afterChange.bind(this);

        this.renderEditor = this.renderEditor.bind(this);
    }

    protected getFormFieldLabel(field: FormField, index: number) {
        return field.name || field.label || "[" + index + "]";
    }

    protected createFormField() {
        return new FormField();
    }

    protected afterChange() {
        if (typeof this.props.afterChange !== "undefined") {
            this.props.afterChange(this.props.form);
        }
        this.forceUpdate();
    }

    public render() {
        const form = this.props.form;

        return (
            <div className="form-form">
                {/* <Field name="name"
                    label="名称">
                    <EditBox
                        name="name"
                        value={form.name}
                        placeholder={"输入表单名称"}
                        afterChange={change => {
                            if (change.type === "commit") {
                                form.name = change.toValue;
                                this.afterChange();
                            }
                        }} />
                </Field> */}
                <Field
                    name="fields"
                    label="表单域">
                    <EditableList<FormField>
                        list={form.fields}
                        getLabel={this.getFormFieldLabel}
                        createItem={this.createFormField}
                        editor={this.renderEditor}
                        afterChange={this.afterChange} />
                </Field>
            </div>
        );
    }

    protected renderEditor(props: EditorProps<FormField>) {
        const afterChange = (field: FormField) => {
            this.afterChange();
        };
        return <FormFieldForm
            field={props.value}
            afterChange={afterChange} />;
    }
}

export default FormForm;