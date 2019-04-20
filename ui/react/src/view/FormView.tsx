import * as React from 'react';
import SharedViewProps from './SharedViewProps';
import RenameAsset from '../action/RenameAsset';
import Action from '../action/Action';
import Form from '../model/Form';
import UpdateForm from '../action/UpdateForm';
import SmartForm from '../form/SmartForm';
import FormField from '../model/FormField';
import SubmitForm from '../action/SubmitForm';
import Parameter from '../model/Parameter';
import classnames from "classnames";

interface FormViewProps extends SharedViewProps<FormView> {
    form: Form;
    className?: string;
}

class FormView extends React.Component<FormViewProps>{
    constructor(props: FormViewProps) {
        super(props);

        this.applyAction = this.applyAction.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    public componentDidMount() {
        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.subscribe(this.applyAction);
        }
    }

    public componentWillUnmount() {
        if (typeof this.props.herald !== 'undefined') {
            this.props.herald.unsubscribe(this.applyAction);
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
            </div>
        );
    }
}

export default FormView;
export { FormViewProps };
