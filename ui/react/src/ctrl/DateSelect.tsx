import * as React from "react";
import InputCtrl, { InputCtrlProps } from "./InputCtrl";
import DatePicker from "react-datepicker";
import * as Popover from "react-popover";
import * as moment from "moment";
import classnames from "classnames";
import "react-datepicker/dist/react-datepicker.css";

interface DateSelectProps extends InputCtrlProps<Date> {
}

interface DateSelectState {
    isOpen: boolean;
}

class DateSelect extends InputCtrl<Date, DateSelectProps, DateSelectState> {
    constructor(props: DateSelectProps) {
        super(props);
        this.state = { isOpen: false };

        this.handleClick = this.handleClick.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.handleClose = this.handleClose.bind(this);
    }

    protected handleClick() {
        const isOpen = !this.state.isOpen;
        this.setState({ isOpen });
    }

    protected handleChange(newDate: Date | null,
        event: React.SyntheticEvent<any> | undefined) {
        if (newDate !== null) {
            this.afterChange(newDate);
        }
    }

    protected handleClose() {
        this.setState({ isOpen: false });
    }

    render() {
        const props = this.props;
        const className = classnames("date-select", props.className);
        const dt = props.value;
        const format = "YYYY-MM-DD HH:mm";

        return (
            <Popover
                className="color-input-popover"
                isOpen={this.state.isOpen}
                preferPlace="below"
                onOuterAction={this.handleClose}
                body={<DatePicker
                    inline
                    dateFormat={format}
                    selected={dt}
                    showTimeSelect={true}
                    onChange={this.handleChange} />}>
                <span
                    className={className}
                    style={props.style}
                    onClick={this.handleClick}>
                    {moment(dt).format(format)}
                </span>
            </Popover>
        );
    }
}

export default DateSelect;
export { DateSelectProps };
