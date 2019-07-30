import * as React from "react";
import Form from "../model/Form";
import FormField from "../model/FormField";
import EditableList, { EditorProps } from "../ctrl/EditableList";
import { VariantType } from "../model/Variant";
// import EditBox from "../ctrl/EditBox";
import FormFieldForm from "../form/FormFieldForm";
import CheckBox from "../ctrl/CheckBox";
import FormFieldBinding from "../model/FormFieldBinding";
import Values from "../model/Values";
import EscapeHelper from "../util/EscapeHelper";
import Field from "./Field";
import Layout from "../model/Layout";
import Display from "../model/Display";
import Span from "../model/Span";
import classnames from "classnames";
import "./AddFormForm.css";

interface PendingField {
    sheetName: string;
    assetName: string;
    paramName: string;
    paramType: VariantType;
    mandatory?: boolean;
    multiple?: boolean;
    formField?: FormField;
};

interface AddFormFormProps extends React.ClassAttributes<AddFormForm> {
    className?: string;
    style?: React.CSSProperties;
    pendingFields: PendingField[];
    afterChange?: (form: Form) => void;
}

class AddFormForm extends React.Component<AddFormFormProps> {
    constructor(props: AddFormFormProps) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.getFieldKey = this.getFieldKey.bind(this);
        this.getFieldLabel = this.getFieldLabel.bind(this);
        this.renderPendingFieldForm = this.renderPendingFieldForm.bind(this);
    }

    protected createFormField(pendingField: PendingField) {
        return new FormField(
            pendingField.paramName,
            pendingField.paramType,
            Values.blank(),
            !!pendingField.mandatory,
            !!pendingField.multiple,
            pendingField.paramName,
            undefined,
            undefined,
            [
                new FormFieldBinding(this.toReference(pendingField))
            ]
        );
    }

    protected toReference(pendingField: PendingField) {
        return EscapeHelper.escapeNameIfNeeded(pendingField.sheetName) + "!"
            + EscapeHelper.escapeNameIfNeeded(pendingField.assetName) + "!"
            + EscapeHelper.escapeName(pendingField.paramName)
    }

    protected createForm() {
        const fields: FormField[] = [];
        this.props.pendingFields.forEach(pendingField => {
            if (typeof pendingField.formField !== "undefined") {
                fields.push(pendingField.formField);
            }
        });
        const layout = new Layout(Display.BLOCK);
        layout.grid.column = new Span(1, 13);
        layout.grid.row = new Span(1, 3);
        return new Form("form-" + new Date().getTime(), fields, layout); // TODO review form name
    }

    public getCurrentForm() {
        return this.createForm();
    }

    protected handleChange(list: PendingField[]) {
        if (typeof this.props.afterChange !== "undefined") {
            this.props.afterChange(this.createForm());
        }
        this.forceUpdate();
    }

    protected getFieldKey(field: PendingField) {
        return this.toReference(field);
    }

    protected getFieldLabel(field: PendingField) {
        const prefix = typeof field.formField === "undefined" ? "　" : "✓";
        return prefix + " " + this.toReference(field);
    }

    public render() {
        const className = classnames("add-form-form", this.props.className);
        return (
            <div
                className={className}
                style={this.props.style}>
                {/* <Field
                    className="form-name"
                    name="name"
                    label="表单名称">
                    <EditBox name="form" value="" />
                </Field> */}
                <Field
                    className="field-settings"
                    name="fields"
                    label="字段设置">
                    <EditableList<PendingField>
                        list={this.props.pendingFields}
                        editor={this.renderPendingFieldForm}
                        addible={false}
                        removable={false}
                        sortable={false}
                        getKey={this.getFieldKey}
                        getLabel={this.getFieldLabel}
                        afterChange={this.handleChange}
                    />
                </Field>
            </div>
        );
    }

    protected renderPendingFieldForm(props: EditorProps<PendingField>) {
        const handleCheckBoxChange = (newVal: boolean) => {
            if (newVal && typeof props.value.formField === 'undefined') {
                props.value.formField = this.createFormField(props.value);
            } else {
                props.value.formField = undefined;
            }
            this.handleChange(this.props.pendingFields);
        };
        const handleFieldChange = (field: FormField) => {
            this.handleChange(this.props.pendingFields);
        };
        return (
            <>
                <Field
                    name="enable"
                    disableLabel={true}>
                    <CheckBox
                        name="enable"
                        label="添加到表单"
                        value={typeof props.value.formField !== 'undefined'}
                        afterChange={handleCheckBoxChange} />
                </Field>
                {typeof props.value.formField !== 'undefined' && (
                    <FormFieldForm
                        field={props.value.formField}
                        afterChange={handleFieldChange} />
                )}
            </>
        );
    }
}

export default AddFormForm;
export { PendingField, AddFormFormProps };
