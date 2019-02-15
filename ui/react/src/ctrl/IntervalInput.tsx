import * as React from 'react';
import Interval from '../model/Interval';
import NumberInput from './NumberInput';
import classnames from 'classnames';
import './IntervalInput.css';

interface IntervalInputProps extends React.ClassAttributes<IntervalInput> {
    value: Interval;
    className?: string;
    fromPlaceholder?: string;
    toPlaceholder?: string;
    afterChange?: (value: Interval) => void;
}

interface IntervalState {
    isIntervalValid: boolean,
}

class IntervalInput extends React.Component<IntervalInputProps, IntervalState> {

    protected static defaultProps: Partial<IntervalInputProps> = {
        fromPlaceholder: "起始值（小）",
        toPlaceholder: "结束值（大）",
    };

    constructor(props: IntervalInputProps) {
        super(props);

        this.state = this.createInitialState(props);

        this.handleFromChange = this.handleFromChange.bind(this);
        this.handleToChange = this.handleToChange.bind(this);
        this.validateFromValue = this.validateFromValue.bind(this);
        this.validateToValue = this.validateToValue.bind(this);
    }

    public componentDidUpdate(prevProps: IntervalInputProps) {
        if (this.props.value !== prevProps.value) {
            this.setState(this.createInitialState(this.props));
        }
    }

    protected createInitialState(props: IntervalInputProps): IntervalState {
        const interval = props.value;
        return {
            isIntervalValid: this.validateInterval(interval),
        };
    }

    public validateInterval(interval: Interval): boolean {
        return isFinite(interval.from) &&
            isFinite(interval.to) &&
            interval.from < interval.to;
    }

    protected handleFromChange(value: number) {
        const interval = this.props.value;
        interval.from = value;
        this.afterChange();
    }

    protected handleToChange(value: number) {
        const interval = this.props.value;
        interval.to = value;
        this.afterChange();
    }

    protected afterChange() {
        const interval = this.props.value;
        const isIntervalValid = this.validateInterval(interval);
        this.setState({
            isIntervalValid,
        });
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(interval);
        }
    }

    protected validateFromValue(value: number) {
        this.setState({
            isIntervalValid: value < this.props.value.to
        });
        return true; // when from > to, give a warning but still accept it.
    }

    protected validateToValue(value: number) {
        this.setState({
            isIntervalValid: value > this.props.value.from
        });
        return true; // when from > to, give a warning but still accept it.
    }

    public render() {
        const interval = this.props.value;
        const className = classnames(
            "interval-input",
            this.state.isIntervalValid ? "valid" : "invalid",
            this.props.className);

        return (
            <span className={className}>
                <NumberInput
                    className="interval-input-from"
                    name="from"
                    placeholder={this.props.fromPlaceholder}
                    value={interval.from}
                    afterChange={this.handleFromChange}
                    validator={this.validateFromValue} />
                <span className="separater">-</span>
                <NumberInput
                    className="interval-input-to"
                    name="to"
                    placeholder={this.props.toPlaceholder}
                    value={interval.to}
                    afterChange={this.handleToChange}
                    validator={this.validateToValue} />
            </span>
        );
    }
}

export default IntervalInput;