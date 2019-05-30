import * as React from 'react';
import EditBox from './EditBox';
import ValueChange from "./ValueChange";
import classnames from 'classnames';
import './NumberInput.css';

interface NumberInputProps extends React.ClassAttributes<NumberInput> {
    value: number,
    afterChange: (value: number) => void,
    name?: string,
    className?: string,
    placeholder?: string,
    min?: number,
    max?: number,
    validator?: (value: number) => boolean,
}

interface NumberInputState {
    isValid: boolean,
}

class NumberInput extends React.Component<NumberInputProps, NumberInputState> {
    constructor(props: NumberInputProps) {
        super(props);

        this.state = this.createInitialState(props);

        this.checkChange = this.checkChange.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleEndEdit = this.handleEndEdit.bind(this);
    }

    public componentDidUpdate(prevProps: NumberInputProps, prevState: NumberInputState) {
        if (this.props.value !== prevProps.value &&
            !(isNaN(this.props.value) || isNaN(prevProps.value))) {
            this.setState(this.createInitialState(this.props));
        }
    }

    protected createInitialState(props: NumberInputProps): NumberInputState {
        return {
            isValid: this.validateNumber(props.value),
        };
    }

    protected validateStr(inputText: string): boolean {
        const value = Number(inputText);
        return this.validateNumber(value);
    }

    protected validateNumber(value: number): boolean {
        if (!isFinite(value)) {
            return false;
        }
        if (typeof this.props.min !== 'undefined' &&
            value < this.props.min) {
            return false;
        }
        if (typeof this.props.max !== 'undefined' &&
            value > this.props.max) {
            return false;
        }
        if (typeof this.props.validator !== 'undefined') {
            return this.props.validator(value);
        }
        return true;
    }

    protected checkChange(change: ValueChange<string>) {
        if (change.type !== 'commit') {
            return true;
        }
        return this.validateStr(change.toValue);
    }

    protected handleChange(change: ValueChange<string>) {
        switch (change.type) {
            case 'edit':
                this.setState({
                    isValid: this.validateStr(change.toValue),
                });
                break;
            case 'commit':
                if (this.state.isValid) {
                    const value = Number(change.toValue);
                    if (value !== this.props.value) {
                        this.props.afterChange(value);
                    }
                } else {
                    throw new Error('Invalid number committed, this is probably a bug!');
                }
                break;
            case 'rollback':
                this.setState(this.createInitialState(this.props));
                break;
        }
    }

    public handleEndEdit() {
        // this.setState(this.createInitialState(this.props));
    }

    public render() {
        const strVal = isFinite(this.props.value) ?
            this.props.value.toString() : "";

        const className = classnames(
            "number-input",
            this.state.isValid ? "valid" : "invalid",
            this.props.className);

        return (
            <EditBox
                value={strVal}
                name={this.props.name}
                placeholder={this.props.placeholder}
                className={className}
                beforeChange={this.checkChange}
                afterChange={this.handleChange}
                afterEndEdit={this.handleEndEdit} />
        );
    }
}

export default NumberInput;
export { NumberInputProps };
