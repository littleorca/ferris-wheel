import * as React from "react";
import Modal, { ModalProps } from "./Modal";
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
    container?: Element;
    content?: React.ReactNode;
    actions?: DialogAction[];
}

class Dialog extends React.Component<DialogProps> {
    constructor(props: DialogProps) {
        super(props);
        this.handleAction = this.handleAction.bind(this);
    }

    protected handleAction(name: string) {
        //
    }

    public render() {
        const className = classnames("dialog", this.props.className);
        return (
            <div className={className} style={this.props.style}>
                {this.props.title && (
                    <h3 className="dialog-title">{this.props.title}</h3>
                )}
                <div className="dialog-content">
                    {this.props.content || this.props.children}
                </div>
                {this.props.actions && (
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
                )}
            </div>
        );
    }

    public static show(renderer: React.SFC<ModalProps>, actionCreator?: (props: ModalProps) => DialogAction[], parent?: Element) {
        Modal.show(props => {
            const actions = typeof actionCreator !== "undefined" ?
                actionCreator(props) : undefined;
            return (
                <Dialog actions={actions}>
                    {renderer(props)}
                </Dialog>
            )
        }, parent);
    }

    public static confirm(message: string, onOk: () => void, onCancel?: () => void, parent?: Element) {
        Modal.show(props => <Dialog actions={[{
            name: "cancel",
            label: "取消",
            callback: () => {
                props.close();
                if (typeof onCancel !== "undefined") {
                    onCancel();
                }
            }
        }, {
            name: "Ok",
            label: "确定",
            callback: () => {
                props.close();
                onOk();
            }
        }]}>{message}</Dialog>, parent);
    }
}

export default Dialog;
export { DialogAction, DialogProps };
