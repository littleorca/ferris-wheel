import * as React from "react";
import FormFieldBinding from "../model/FormFieldBinding";
import Field from "./Field";
import EditBox from "../ctrl/EditBox";
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

    render() {
        const className = classnames("form-field-binding-form", this.props.className);
        const binding = this.props.binding;
        return (
            <div className={className} style={this.props.style}>
                <Field
                    className="form-field-binding-form-target"
                    name="target"
                    label="目标">
                    <EditBox
                        name="target"
                        placeholder="输入目标引用"
                        value={binding.target}
                        afterChange={change => {
                            if (change.type === "commit") {
                                binding.target = change.nextValue;
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
