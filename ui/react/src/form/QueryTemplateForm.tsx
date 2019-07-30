import * as React from 'react';
import ParameterForm from './ParameterForm';
import EditableList, { EditorProps } from '../ctrl/EditableList';
import Parameter from '../model/Parameter';
import QueryTemplate from '../model/QueryTemplate';
import EditBox from '../ctrl/EditBox';
import ValueChange from "../ctrl/ValueChange";

const BuiltinParamEditor = (props: EditorProps<Parameter>) => {
    return (
        <ParameterForm
            parameter={props.value}
            afterChange={props.onSubmit} />
    );
}

interface QueryTemplateFormProps extends React.ClassAttributes<QueryTemplateForm> {
    queryTemplate: QueryTemplate,
    afterChange?: (queryTemplate: QueryTemplate) => void,
}

class QueryTemplateForm extends React.Component<QueryTemplateFormProps> {
    protected static defaultProps: Partial<QueryTemplateFormProps> = {
        queryTemplate: {
            scheme: '',
            builtinParams: []
        }
    }

    constructor(props: QueryTemplateFormProps) {
        super(props);

        this.handleSchemeChange = this.handleSchemeChange.bind(this);
        this.handleBuiltinParamsChange = this.handleBuiltinParamsChange.bind(this);
    }

    protected handleSchemeChange(change: ValueChange<string>) {
        if (change.type !== 'commit') {
            return;
        }
        this.props.queryTemplate.scheme = change.toValue;
        this.forceUpdate();
        this.onUpdate();
    }

    protected handleBuiltinParamsChange(list: Parameter[]) {
        this.onUpdate();
    }

    protected onUpdate() {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.queryTemplate);
        }
    }

    protected getParamLabel(param: Parameter, index: number) {
        const value = param.value;
        return param.name + '=' + (value.isFormula() ?
            value.getFormulaString() : value.toString());
    }

    protected createParam() {
        return new Parameter();
    }

    public render() {
        const queryTemplate = this.props.queryTemplate;
        return (
            <div className="query-template-form">
                <label className="field query-scheme">
                    <span>Scheme</span>
                    <EditBox
                        name="scheme"
                        value={queryTemplate.scheme}
                        afterChange={this.handleSchemeChange} />
                </label>
                <div className="builtin-params">
                    <label>参数</label>
                    <EditableList<Parameter>
                        list={queryTemplate.builtinParams}
                        getLabel={this.getParamLabel}
                        createItem={this.createParam}
                        editor={BuiltinParamEditor}
                        afterChange={this.handleBuiltinParamsChange} />
                </div>
            </div>
        );
    }
}

export default QueryTemplateForm;
export { QueryTemplateFormProps };
