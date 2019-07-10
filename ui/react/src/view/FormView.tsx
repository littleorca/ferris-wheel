import * as React from 'react';
import * as ReactDOM from 'react-dom';
import SharedViewProps from './SharedViewProps';
import RenameAsset from '../action/RenameAsset';
import Action from '../action/Action';
import Form from '../model/Form';
import UpdateForm from '../action/UpdateForm';
import SmartForm from '../form/SmartForm';
import FormField from '../model/FormField';
import SubmitForm from '../action/SubmitForm';
import Parameter from '../model/Parameter';
import Layout from '../model/Layout';
import LayoutAsset from '../action/LayoutAsset';
import GroupView, { GroupItem } from './GroupView';
import FormForm from '../form/FormForm';
import LayoutForm from '../form/LayoutForm';
import classnames from "classnames";

interface FormViewProps extends SharedViewProps<FormView> {
    form: Form;
    className?: string;
}

class FormView extends React.Component<FormViewProps>{
    constructor(props: FormViewProps) {
        super(props);

        this.handleFormChange = this.handleFormChange.bind(this);
        this.handleLayoutChange = this.handleLayoutChange.bind(this);
        this.handleAction = this.handleAction.bind(this);
        this.applyAction = this.applyAction.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);

        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.subscribe(this.applyAction);
        }
    }

    public componentWillUnmount() {
        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.unsubscribe(this.applyAction);
        }
    }

    protected handleFormChange() {
        const updateForm = new UpdateForm("", this.props.form);
        this.handleAction(updateForm.wrapper());
    }

    protected handleLayoutChange(layout: Layout) {
        const layoutAsset = new LayoutAsset("", this.props.form.name, layout);
        this.handleAction(layoutAsset.wrapper());
    }

    protected handleAction(action: Action) {
        if (typeof this.props.onAction === "function") {
            this.props.onAction(action);
        }
    }

    protected applyAction(action: Action) {
        if (!action.isAssetAction() ||
            action.targetAsset() !== this.props.form.name) {
            return;
        }
        if (typeof action.renameAsset !== 'undefined') {
            this.applyRenameAsset(action.renameAsset);
        } else if (typeof action.updateForm !== 'undefined') {
            this.applyUpdateForm(action.updateForm);
        }
    }

    protected applyRenameAsset(renameAsset: RenameAsset) {
        this.props.form.name = renameAsset.newAssetName;
        this.forceUpdate();
    }

    protected applyUpdateForm(updateForm: UpdateForm) {
        Object.assign(this.props.form, updateForm.form);
        this.forceUpdate();
    }

    protected handleSubmit(fields: FormField[]) {
        const params: Parameter[] = fields.map(field => new Parameter(field.name, field.value));
        if (typeof this.props.onAction !== 'undefined') {
            const action = new SubmitForm(
                '',
                this.props.form.name,
                params,
            ).wrapper();
            this.props.onAction(action);
        }
    }

    public render() {
        const props = this.props;
        const className = classnames("form-view", props.className);

        return (
            <div className={className}>
                <SmartForm
                    fields={props.form.fields}
                    onSubmit={this.handleSubmit} />
                <>
                    {this.props.controlPortal &&
                        ReactDOM.createPortal(this.renderControl(), this.props.controlPortal)}
                </>
            </div>
        );
    }

    protected renderControl() {
        return (
            <GroupView
                className="realtime-edit form-option">
                <GroupItem
                    name="form"
                    title="表单">
                    <form onSubmit={e => e.preventDefault()}>
                        <FormForm
                            form={this.props.form}
                            afterChange={this.handleFormChange} />
                    </form>
                </GroupItem>
                <GroupItem
                    name="layout"
                    title="布局">
                    <form onSubmit={e => e.preventDefault()}>
                        <LayoutForm
                            layout={this.props.form.layout}
                            afterChange={this.handleLayoutChange} />
                    </form>
                </GroupItem>
            </GroupView>
        );
    }
}

export default FormView;
export { FormViewProps };
