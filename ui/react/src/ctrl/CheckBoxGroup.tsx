import * as React from "react";
import InputCtrl, { InputCtrlProps } from "./InputCtrl";
import CheckBox from "./CheckBox";
import SelectOption from "./SelectOption";
import classnames from "classnames";

interface CheckBoxGroupProps extends InputCtrlProps<string[]> {
    options: SelectOption[];
}

class CheckBoxGroup extends InputCtrl<string[], CheckBoxGroupProps> {
    constructor(props: CheckBoxGroupProps) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    protected handleChange(option: SelectOption, checked: boolean | undefined) {
        const value = this.props.value;
        const index = value.indexOf(option.value);
        if (checked) {
            if (index === -1) {
                value.push(option.value);
            }
        } else {
            if (index !== -1) {
                value.splice(index, 1);
            }
        }
        this.afterChange(value);
    }

    render() {
        const props = this.props;
        const className = classnames("check-box-group", props.className);

        return (
            <div className={className}
                style={props.style}>
                {props.options.map((opt, i) => {
                    const label = typeof opt.label !== "undefined" ? opt.label : opt.value;
                    return (
                        <CheckBox
                            key={i + ":" + opt.value}
                            label={label}
                            value={props.value.indexOf(opt.value) !== -1}
                            afterChange={checked => this.handleChange(opt, checked)}
                        />
                    )
                })}
            </div>
        );
    }
}

export default CheckBoxGroup;
export { CheckBoxGroupProps };
