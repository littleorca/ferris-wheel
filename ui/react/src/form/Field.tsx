import * as React from "react";
import classnames from "classnames";
import "./Field.css";

interface FieldProps<V> extends React.ClassAttributes<Field<V>> {
    className?: string;
    style?: React.CSSProperties;
    name: string;
    label?: string;
    tips?: string;
    error?: string;
    disableLabel?: boolean;
    disableTips?: boolean;
    disableError?: boolean;
    afterChange?: (name: string, value: V) => void;
}

class Field<V> extends React.PureComponent<FieldProps<V>> {
    render() {
        const props = this.props;
        const className = classnames("field-container", props.className);

        return (
            <div
                className={className}
                style={this.props.style}
                title={this.props.tips}>
                {props.disableLabel || (
                    <label className="field-label">{props.label || props.name}</label>
                )}
                <span className="field-control">
                    {props.children}
                    {props.disableError || (
                        <span className="field-error">{props.error}</span>
                    )}
                </span>
                {props.disableTips || (
                    <p className="field-tips">{props.tips}</p>
                )}
            </div>
        );
    }
}

export default Field;
export { FieldProps };
