import * as React from "react";
import Values from "../model/Values";
import FormFieldBinding from "../model/FormFieldBinding";
import Field from "./Field";
import UnionValueEdit from "../ctrl/UnionValueEdit";
import classnames from "classnames";

interface FormFieldBindingFormProps extends React.ClassAttributes<FormFieldBindingForm> {
    className?: string;
    style?: React.CSSProperties;
    binding: FormFieldBinding;
    afterChange?: (binding: FormFieldBinding) => void;
}

class FormFieldBindingForm extends React.Component<FormFieldBindingFormProps>{
    constructor(props: FormFieldBindingFormProps) {
        super(props);

        this.afterChange = this.afterChange.bind(this);
    }

    protected afterChange() {
        if (typeof this.props.afterChange === "function") {
            this.props.afterChange(this.props.binding);
        }
    }

    public render() {
        const className = classnames("form-field-binding-form", this.props.className);
        const binding = this.props.binding;
        return (
            <div className={className} style={this.props.style}>
                <Field
                    className="form-field-binding-form-target"
                    name="target"
                    label="目标">
                    <UnionValueEdit
                        name="target"
                        placeholder="输入目标引用"
                        modes={["formula"]}
                        value={Values.formula(binding.target)}
                        afterChange={change => {
                            if (change.type === "commit") {
                                binding.target = change.toValue.getFormulaString();
                                this.afterChange();
                            }
                        }} />
                </Field>
            </div>
        );
    }
}

export default FormFieldBindingForm;
export { FormFieldBindingFormProps };
