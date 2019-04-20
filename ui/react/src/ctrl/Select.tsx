import * as React from 'react';
import InputCtrl, { InputCtrlProps } from './InputCtrl';
import SelectOption from "./SelectOption";
import classnames from "classnames";

interface SelectProps extends InputCtrlProps<string> {
    value: string;
    options: SelectOption[];
}

class Select extends InputCtrl<string, SelectProps> {
    constructor(props: SelectProps) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    protected handleChange(event: React.ChangeEvent<HTMLSelectElement>) {
        const newValue = event.currentTarget.value;
        this.afterChange(newValue);
    }

    public render() {
        const className = classnames("select", this.props.className);

        return (
            <select
                className={className}
                style={this.props.style}
                name={this.props.name}
                value={this.props.value}
                onChange={this.handleChange}>
                {this.props.options.map((opt, i) => {
                    const label = typeof opt.label !== "undefined" ? opt.label : opt.value;
                    return (
                        <option
                            key={i + ":" + opt.value}
                            value={opt.value}>
                            {label}
                        </option>
                    );
                })}
            </select>
        );
    }
}

export default Select;
export { SelectProps };
