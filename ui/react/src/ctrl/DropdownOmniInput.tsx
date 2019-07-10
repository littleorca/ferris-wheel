import * as React from "react";
import * as Popover from "react-popover";
import OmniInput, { OmniInputProps } from "./OmniInput";
import "./DropdownOmniInput.css";


interface DropdownOmniInputProps extends OmniInputProps {

}

interface DropdownOmniInputState {
    isOpen: boolean;
}

class DropdownOmniInput extends React.Component<DropdownOmniInputProps, DropdownOmniInputState> {
    constructor(props: DropdownOmniInputProps) {
        super(props);
        this.state = { isOpen: false };

        this.handleClick = this.handleClick.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.afterChange = this.afterChange.bind(this);
    }

    protected handleClick() {
        const isOpen = !this.state.isOpen;
        this.setState({ isOpen });
    }

    protected handleClose() {
        this.setState({ isOpen: false });
    }

    protected afterChange() { }

    render() {
        return (
            <Popover
                className="dropdown-omni-input-popover"
                isOpen={this.state.isOpen}
                preferPlace="below"
                onOuterAction={this.handleClose}
                enterExitTransitionDurationMs={0}
                tipSize={.01}
                body={<OmniInput {...this.props} />}>
                <span
                    className="dropdown-omni-input"
                    onClick={this.handleClick}>
                    {this.props.value.map((v, i) => (
                        <span
                            key={i + "" + v}
                            className="dropdown-omni-input-value-item">
                            {v}
                        </span>
                    ))}
                </span>
            </Popover>
        );
    }
}

export default DropdownOmniInput;
export { DropdownOmniInputProps };
