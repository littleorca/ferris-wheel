import * as React from "react";
import Modal, { ModalProps } from "./Modal";
import Dialog from "./Dialog";
import FormatForm from "../form/FormatForm";
import classnames from "classnames";
import "./FormatFormDialog.css";

interface FormatFormDialogProps
    extends React.ClassAttributes<FormatFormDialog> {
    className?: string;
    style?: React.CSSProperties;
    format: string;
    onCancel(): void;
    onOk(format: string): void;
}

interface FormatFormDialogState {
    pendingFormat: string;
}

class FormatFormDialog extends React.Component<
    FormatFormDialogProps,
    FormatFormDialogState
    > {
    constructor(props: FormatFormDialogProps) {
        super(props);
        this.state = { pendingFormat: props.format };

        this.handleChange = this.handleChange.bind(this);
    }

    protected handleChange(format: string) {
        this.setState({ pendingFormat: format });
    }

    public render() {
        const className = classnames(
            "format-form-dialog",
            this.props.className
        );
        return (
            <Dialog
                className={className}
                style={this.props.style}
                title="设置格式"
                actions={[
                    {
                        name: "format-form-cancel",
                        label: "取消",
                        callback: this.props.onCancel
                    },
                    {
                        name: "format-form-ok",
                        label: "确定",
                        callback: () =>
                            this.props.onOk(this.state.pendingFormat)
                    }
                ]}
            >
                <FormatForm
                    format={this.state.pendingFormat}
                    onChange={this.handleChange}
                />
            </Dialog>
        );
    }

    public static show(format: string, onOk: (format: string) => void) {
        const dialogRenderer = (props: ModalProps) => {
            const handleOk = (newFmt: string) => {
                props.close();
                onOk(newFmt);
            };
            return <FormatFormDialog
                format={format}
                onCancel={props.close}
                onOk={handleOk} />
        };
        Modal.show(dialogRenderer);
    }
}

export default FormatFormDialog;
