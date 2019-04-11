import * as React from "react";
import FormatForm from "../form/FormatForm";
import Popover from "react-popover";
import classnames from "classnames";
import "./FormatInput.css";

interface FormatInputProps extends React.ClassAttributes<FormatInput> {
    className?: string;
    style?: React.CSSProperties;
    format: string;
    popoverPlacement?: 'top' | 'right' | 'bottom' | 'left';
    afterChange?(format: string): void;
}

interface FormatInputState {
    isOpen: boolean,
    pendingFormat: string,
}

class FormatInput extends React.Component<FormatInputProps, FormatInputState> {
    constructor(props: FormatInputProps) {
        super(props);

        this.state = {
            isOpen: false,
            pendingFormat: props.format
        }

        this.handleClick = this.handleClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    public componentDidUpdate(prevProps: FormatInputProps) {
        if (this.props.format !== prevProps.format) {
            this.setState({
                pendingFormat: this.props.format
            });
        }
    }

    protected handleClick() {
        if (!this.state.isOpen) {
            this.setState({
                isOpen: true
            });
        } else {
            this.handleClose();
        }
    }

    protected handleClose() {
        const format = this.state.pendingFormat;
        this.setState({
            isOpen: false,
            pendingFormat: this.props.format
        });
        if (typeof this.props.afterChange === 'function') {
            this.props.afterChange(format);
        }
    }

    protected handleChange(format: string) {
        this.setState({
            pendingFormat: format
        });
    }

    public render() {
        const className = classnames("format-input", this.props.className);
        const isOpen = this.state.isOpen;
        const format = this.state.pendingFormat;

        return <Popover
            className="format-input-popover"
            isOpen={isOpen}
            preferPlace="above"
            onOuterAction={this.handleClose}
            body={<FormatForm
                format={format}
                onChange={this.handleChange} />}>
            <div className={className} onClick={this.handleClick}>
                格式：{this.props.format || "默认格式"}
            </div>
        </Popover>
    }
}

export default FormatInput;
export { FormatInputProps }
