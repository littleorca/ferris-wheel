import * as React from "react";
import Button from "../ctrl/Button";
import classnames from "classnames";
import "./Dialog.css";

interface DialogAction {
    name: string;
    label?: string;
    tips?: string;
    callback(name: string): void;
}

interface DialogProps extends React.ClassAttributes<Dialog> {
    className?: string;
    style?: React.CSSProperties;
    title?: string;
    content?: React.ReactNode;
    actions: DialogAction[];
}

class Dialog extends React.Component<DialogProps> {
    constructor(props: DialogProps) {
        super(props);
        this.handleAction = this.handleAction.bind(this);
    }

    protected handleAction(name: string) {
        //
    }

    render() {
        const className = classnames("dialog", this.props.className);
        return (
            <div className={className} style={this.props.style}>
                {this.props.title && (
                    <h3 className="dialog-title">{this.props.title}</h3>
                )}
                <div className="dialog-content">
                    {this.props.content || this.props.children}
                </div>
                <div className="dialog-actions">
                    {this.props.actions.map((action, i) => (
                        <Button
                            key={action.name + "@" + i}
                            name={action.name}
                            label={action.label}
                            tips={action.tips}
                            onClick={action.callback}
                        />
                    ))}
                </div>
            </div>
        );
    }
}

export default Dialog;
export { DialogAction, DialogProps };
