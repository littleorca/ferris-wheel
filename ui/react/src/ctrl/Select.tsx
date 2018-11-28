import * as React from 'react';

interface SelectOption {
    value: string;
    label: string;
}

interface SelectProps extends React.ClassAttributes<Select> {
    options: SelectOption[];
    value: string;
    name?: string;
    className?: string;
    afterChange: (value: string) => void;
}

class Select extends React.Component<SelectProps> {

    protected handleChange(event: React.ChangeEvent<HTMLSelectElement>) {
        const value = event.currentTarget.value;
        this.props.afterChange(value);
    }

    public render() {
        return (
            <select
                name={this.props.name}
                value={this.props.value}
                className={this.props.className}
                onChange={this.handleChange}>
                {this.props.options.map((e, i) => {
                    return (
                        <option
                            key={e.value}
                            value={e.value}>
                            {e.label}
                        </option>
                    );
                })}
            </select>
        );
    }
}

export default Select;
export { SelectOption, SelectProps };
