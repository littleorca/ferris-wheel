import * as React from 'react';
import * as Popover from 'react-popover';
import Button, { ButtonProps } from './Button';
import classnames from 'classnames';
import './DropdownButton.css';

interface DropdownItemProps extends ButtonProps {
    renderer?: React.SFC<DropdownItemProps>;
}

interface DropdownButtonProps extends React.ClassAttributes<DropdownButton> {
    primary: ButtonProps;
    items: DropdownItemProps[];
    className?: string;
}

interface DropdownButtonState {
    isDroppedDown: boolean;
}

class DropdownButton extends React.Component<DropdownButtonProps, DropdownButtonState> {
    public static SEPARATOR: string = ""; // special button name for separator.

    constructor(props: DropdownButtonProps) {
        super(props);

        this.state = {
            isDroppedDown: false,
        };

        this.handlePrimaryClick = this.handlePrimaryClick.bind(this);
        this.toggleDropdown = this.toggleDropdown.bind(this);
        this.closeDropdown = this.closeDropdown.bind(this);
    }

    protected handlePrimaryClick(name: string, event: React.MouseEvent<HTMLButtonElement>) {
        if (typeof this.props.primary.onClick !== 'undefined') {
            this.props.primary.onClick(name, event);
        } else {
            this.toggleDropdown();
        }
    }

    protected toggleDropdown() {
        this.setState({
            isDroppedDown: !this.state.isDroppedDown,
        });
    }

    protected closeDropdown() {
        this.setState({
            isDroppedDown: false,
        });
    }

    public render() {
        const className = classnames(
            "dropdown-button",
            "button",
            this.props.className);

        const triggerClassName = classnames(
            "dropdown-trigger",
            { "active": this.state.isDroppedDown });

        return (
            <div
                className={className}>
                <Popover
                    className="dropdown-popover"
                    isOpen={this.state.isDroppedDown}
                    preferPlace="below"
                    onOuterAction={this.closeDropdown}
                    enterExitTransitionDurationMs={0}
                    tipSize={.01}
                    style={{
                        zIndex: 999,
                    }}
                    body={(
                        <div className="dropdown-content">
                            {this.props.items.map((e, i) =>
                                this.renderItem(e, i))}
                        </div>
                    )}>

                    <div className="dropdown-primary">
                        <Button
                            {...this.props.primary}
                            className={classnames("dropdown-header", this.props.primary.className)}
                            style={{
                                height: '100%', // set height here to boost priority and avoid using important in css file.
                            }}
                            onClick={this.handlePrimaryClick} />
                        <Button
                            className={triggerClassName}
                            style={{
                                height: '100%', // set height here to boost priority and avoid using important in css file.
                            }}
                            name="_dropdown_button_trigger_"
                            label="&#9660;"
                            tips="See more options."
                            onClick={this.toggleDropdown} />
                    </div>
                </Popover>
            </div>
        );
    }

    protected renderItem(item: DropdownItemProps, index: number) {
        const className = classnames("dropdown-button-item", item.className);

        const onClick = (name: string, event: React.MouseEvent<HTMLButtonElement>) => {
            this.closeDropdown();
            if (typeof item.onClick !== 'undefined') {
                item.onClick(name, event);
            }
        }

        if (typeof item.renderer !== 'undefined') {
            return (
                <div
                    key={index + ":" + item.name}
                    className={className}>
                    {item.renderer({ ...item, onClick })}
                </div>
            );

        } else if (item.name !== '') {
            return (
                <Button {...item}
                    key={index + ":" + item.name}
                    className={className}
                    onClick={onClick} />
            );

        } else {
            return (
                <hr
                    key={index + ":separator"}
                    className="dropdown-button-separator" />
            );
        }
    }
}

export default DropdownButton;
export { DropdownButtonProps };
