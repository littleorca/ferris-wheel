import * as React from 'react';
import FormField from '../model/FormField';
import SmartField from './SmartField';
import Button from '../ctrl/Button';
import classnames from "classnames";
import "./SmartForm.css";

interface SmartFormProps extends React.ClassAttributes<SmartForm> {
    className?: string;
    style?: React.CSSProperties;
    fields: FormField[];
    submitButtonLabel?: string;
    afterChange?: (field: FormField) => void;
    onSubmit: (fields: FormField[]) => void;
}

class SmartForm extends React.Component<SmartFormProps> {
    constructor(props: SmartFormProps) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    protected handleChange(field: FormField) {
        if (typeof this.props.afterChange !== "undefined") {
            this.props.afterChange(field);
        }
    }

    protected handleSubmit() {
        this.props.onSubmit(this.props.fields);
    }

    render() {
        const props = this.props;
        const className = classnames("smart-form", props.className);
        return (
            <form
                className={className}
                style={props.style}
                onSubmit={e => {
                    e.preventDefault();
                    this.handleSubmit();
                }}>
                {props.fields.map((f, i) => {
                    return (
                        <SmartField
                            key={i + ":" + f.name}
                            field={f}
                            afterChange={() => this.handleChange(f)} />
                    );
                })}
                <Button
                    className="submit-button"
                    name="submit"
                    type="submit"
                    label={props.submitButtonLabel || "确定"} />
            </form>
        );
    }
}

export default SmartForm;
export { SmartFormProps };
