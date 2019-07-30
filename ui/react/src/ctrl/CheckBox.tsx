import * as React from 'react';
import InputCtrl, { InputCtrlProps } from './InputCtrl';
import classnames from "classnames";

interface CheckBoxProps extends InputCtrlProps<boolean> {
    label?: string;
    indeterminate?: boolean;
    readOnly?: boolean;
}

class CheckBox extends InputCtrl<boolean, CheckBoxProps> {
    private checkBoxRef: React.RefObject<HTMLInputElement> = React.createRef();
    constructor(props: CheckBoxProps) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    public componentDidMount() {
        if (this.props.indeterminate) {
            this.setIndeterminate();
        }
    }

    public componentDidUpdate() {
        if (this.props.indeterminate) {
            this.setIndeterminate();
        }
    }

    protected setIndeterminate() {
        const dom = this.checkBoxRef.current;
        if (dom !== null) {
            dom.indeterminate = true;
        }
    }

    protected handleChange(event: React.ChangeEvent<HTMLInputElement>) {
        this.afterChange(event.currentTarget.checked);
    }

    public render() {
        const props = this.props;
        const className = classnames("checkbox", props.className);
        const value = typeof props.value === "undefined" ? false : props.value;

        return (
            <label className={className} style={props.style}>
                <input
                    ref={this.checkBoxRef}
                    type="checkbox"
                    name={props.name}
                    checked={value}
                    disabled={this.props.readOnly}
                    onChange={this.handleChange} />
                <span>{props.label}</span>
            </label>
        );
    }
}

export default CheckBox;
export { CheckBoxProps };
