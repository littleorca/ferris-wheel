import * as React from "react";
import InputCtrl, { InputCtrlProps } from "./InputCtrl";
import SelectOption from "./SelectOption";
import classnames from "classnames";

interface RadioGroupProps extends InputCtrlProps<string> {
    options: SelectOption[];
}

class RadioGroup extends InputCtrl<string, RadioGroupProps> {
    constructor(props: RadioGroupProps) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    protected handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        const value = e.currentTarget.value;
        this.afterChange(value);
    }

    public render() {
        const props = this.props;
        const className = classnames("radio-group", props.className);

        return (
            <div className={className}
                style={props.style}>
                {props.options.map((option, i) => {
                    const label = typeof option.label !== 'undefined' ? option.label : option.value;
                    return (
                        <label key={i + ":" + option.value}>
                            <input
                                type="radio"
                                name={props.name}
                                value={option.value}
                                checked={option.value === props.value}
                                onChange={this.handleChange} />
                            <span>{label}</span>
                        </label>
                    )
                })}
            </div>
        );
    }
}

export default RadioGroup;
export { RadioGroupProps };
