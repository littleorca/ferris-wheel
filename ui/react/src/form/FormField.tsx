import * as React from "react";
import classnames from "classnames";

/**
 * Still inprogress!
 */

interface FormFieldProps extends React.ClassAttributes<FormField> {
    name: string;
    label?: string;
    placeholder?: string;
    tips?: string;
    error?: string;
    className?: string;
    style?: React.CSSProperties;
}

class FormField extends React.Component<FormFieldProps> {
    public render() {
        const className = classnames("form-field", this.props.className);
        return <div className={className}>
            <div />
        </div>
    }
}

export default FormField;
export { FormFieldProps }