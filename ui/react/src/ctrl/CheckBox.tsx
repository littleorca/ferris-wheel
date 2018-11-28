import * as React from 'react';

interface CheckBoxProps extends React.ClassAttributes<CheckBox> {
    value: boolean;
    name?: string;
    className?: string;
    afterChange?: (value: boolean) => void;
}

interface CheckBoxState {
    checked: boolean;
}

class CheckBox extends React.Component<CheckBoxProps, CheckBoxState> {
    constructor(props: CheckBoxProps) {
        super(props);

        this.state = { checked: props.value };

        this.handleChange = this.handleChange.bind(this);
    }

    protected handleChange(event: React.ChangeEvent<HTMLInputElement>) {
        const target = event.currentTarget;
        this.setState({ checked: target.checked });
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(target.checked);
        }
    }

    public render() {
        return (
            <input
                type="checkbox"
                name={this.props.name}
                checked={this.props.value}
                className={this.props.className}
                onChange={this.handleChange} />
        );
    }
}

export default CheckBox;
export { CheckBoxProps };
