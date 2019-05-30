import * as React from "react";
import FormField from "../model/FormField";
import FormFieldBinding from "../model/FormFieldBinding";
import { VariantType } from "../model/Variant";
import EditableList from "../ctrl/EditableList";
import FormFieldBindingForm from "./FormFieldBindingForm";
import UnionValueEdit from "../ctrl/UnionValueEdit";
import EditBox from "../ctrl/EditBox";
import Field from "./Field";
import CheckBox from "../ctrl/CheckBox";
import classnames from "classnames";

interface FormFieldFormProps extends React.ClassAttributes<FormFieldForm> {
    field: FormField;
    className?: string;
    style?: React.CSSProperties;
    afterChange?: (field: FormField) => void;
}

class FormFieldForm extends React.Component<FormFieldFormProps> {
    constructor(props: FormFieldFormProps) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    protected afterChange() {
        if (typeof this.props.afterChange === "function") {
            this.props.afterChange(this.props.field);
        }
        this.forceUpdate();
    }

    public render() {
        const className = classnames("form-field-form", this.props.className);
        const field = this.props.field;

        return (
            <div
                className={className}
                style={this.props.style}>
                <Field
                    className="field-name"
                    name="name"
                    label="字段名">
                    <EditBox
                        name="name"
                        placeholder="输入字段名"
                        value={field.name}
                        afterChange={change => {
                            if (change.type === "commit") {
                                field.name = change.toValue;
                                this.afterChange();
                            }
                        }} />
                </Field>
                <Field
                    className="field-type"
                    name="type"
                    label="类型">
                    <select
                        name="type"
                        value={field.type}
                        onChange={e => {
                            field.type = e.currentTarget.value as VariantType;
                            this.afterChange();
                        }}>
                        <option value={VariantType.BLANK}>请选择…</option>
                        <option value={VariantType.STRING}>字符串</option>
                        <option value={VariantType.DECIMAL}>数字</option>
                        <option value={VariantType.BOOL}>布尔</option>
                        <option value={VariantType.DATE}>日期</option>
                        {/* <option value={VariantType.LIST}>列表</option> */}
                    </select>
                </Field>
                <Field
                    className="field-label"
                    name="label"
                    label="标签">
                    <EditBox
                        name="label"
                        placeholder="输入标签"
                        value={field.label}
                        afterChange={change => {
                            if (change.type === "commit") {
                                field.label = change.toValue;
                                this.afterChange();
                            }
                        }} />
                </Field>
                <Field
                    className="field-tips"
                    name="tips"
                    label="提示">
                    <EditBox
                        name="tips"
                        placeholder="输入提示"
                        value={field.tips}
                        afterChange={change => {
                            if (change.type === "commit") {
                                field.tips = change.toValue;
                                this.afterChange();
                            }
                        }} />
                </Field>
                <Field
                    className="field-multiple"
                    name="multiple"
                    label="是否多值">
                    <CheckBox
                        name="multiple"
                        label="多值"
                        value={field.multiple}
                        afterChange={newVal => {
                            field.multiple = newVal;
                            this.afterChange();
                        }} />
                </Field>
                <Field
                    className="field-options"
                    name="options"
                    label="候选值">
                    <UnionValueEdit
                        className="option-list"
                        value={field.options}
                        afterChange={change => {
                            if (change.type === "commit") {
                                field.options = change.toValue;
                                this.afterChange();
                            }
                        }} />
                </Field>
                <Field
                    className="field-bindings"
                    name="bindings"
                    label="绑定">
                    <EditableList
                        className="binding-list"
                        list={field.bindings}
                        getLabel={item => item.target}
                        createItem={() => new FormFieldBinding()}
                        editor={props => <FormFieldBindingForm
                            binding={props.value}
                            afterChange={binding => props.onSubmit(binding)} />}
                        afterChange={list => { this.afterChange() }} />
                </Field>
            </div>
        );
    }
}

export default FormFieldForm;
export { FormFieldFormProps }