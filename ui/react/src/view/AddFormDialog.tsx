import * as React from "react";
import AddFormForm, { AddFormFormProps, PendingField } from "../form/AddFormForm";
import Dialog from "./Dialog";
import Modal from "./Modal";
import Form from "../model/Form";
import classnames from "classnames";
import "./AddFormDialog.css";

interface AddFormDialogProps extends React.ClassAttributes<AddFormDialog> {
    className?: string;
    style?: React.CSSProperties;
    pendingFields: PendingField[];
    onCancel: () => void;
    onOk: (form: Form) => void;
}

class AddFormDialog extends React.Component<AddFormDialogProps> {
    private formRef: React.RefObject<AddFormForm> = React.createRef();

    render() {
        const className = classnames("add-form-dialog", this.props.className);
        return (
            <Dialog
                className={className}
                style={this.props.style}
                title="添加表单"
                actions={[
                    {
                        name: "add-form-cancel",
                        label: "取消",
                        callback: this.props.onCancel
                    },
                    {
                        name: "add-form-ok",
                        label: "确定",
                        callback: () => {
                            if (this.formRef.current) {
                                this.props.onOk(this.formRef.current.getCurrentForm());
                            }
                        }
                    }
                ]}>
                <AddFormForm
                    {...this.props}
                    ref={this.formRef} />
            </Dialog>
        );
    }

    public static show(pendingFields: PendingField[], onOk: (form: Form) => void) {
        Modal.show(props => <AddFormDialog
            pendingFields={pendingFields}
            onCancel={props.close}
            onOk={form => {
                props.close();
                onOk(form);
            }} />);
    }
}

export default AddFormDialog;
export { AddFormFormProps };
