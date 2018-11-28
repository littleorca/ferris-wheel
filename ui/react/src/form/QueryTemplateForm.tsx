import * as React from 'react';
import NamedValueForm from './NamedValueForm';
import EditableList, { EditorProps } from '../ctrl/EditableList';
import NamedValue from '../model/NamedValue';
import ParamRule from '../model/ParamRule';
import ParamRuleForm from './ParamRuleForm';
import QueryTemplate from '../model/QueryTemplate';
import { VariantType } from '../model/Variant';
import EditBox, { EditBoxChange } from '../ctrl/EditBox';

const BuiltinParamEditor = (props: EditorProps<NamedValue>) => {
    return (
        <NamedValueForm
            namedValue={props.value}
            afterChange={props.onSubmit} />
    );
}

const ParamRuleEditor = (props: EditorProps<ParamRule>) => {
    return (
        <ParamRuleForm
            rule={props.value}
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
            builtinParams: [],
            userParamRules: []
        }
    }

    constructor(props: QueryTemplateFormProps) {
        super(props);

        this.handleSchemeChange = this.handleSchemeChange.bind(this);
        this.handleBuiltinParamsChange = this.handleBuiltinParamsChange.bind(this);
        this.handleParamRulesChange = this.handleParamRulesChange.bind(this);
    }

    protected handleSchemeChange(change: EditBoxChange) {
        if (change.type !== 'commit') {
            return;
        }
        this.props.queryTemplate.scheme = change.nextValue;
        this.forceUpdate();
        this.onUpdate();
    }

    protected handleBuiltinParamsChange(list: NamedValue[]) {
        this.onUpdate();
    }

    protected handleParamRulesChange(list: ParamRule[]) {
        this.onUpdate();
    }

    protected onUpdate() {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.queryTemplate);
        }
    }

    protected getParamLabel(param: NamedValue, index: number) {
        const value = param.value;
        return param.name + '=' + (value.isFormula() ?
            value.getFormulaString() : value.toString());
    }

    protected createParam() {
        return new NamedValue();
    }

    protected getRuleLabel(rule: ParamRule, index: number) {
        return rule.name;
    }

    protected createRule() {
        return new ParamRule('', VariantType.STRING, true, []);
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
                    <label>内置参数</label>
                    <EditableList<NamedValue>
                        list={queryTemplate.builtinParams}
                        getLabel={this.getParamLabel}
                        createItem={this.createParam}
                        editor={BuiltinParamEditor}
                        afterChange={this.handleBuiltinParamsChange} />
                </div>
                <div className="user-param-rules">
                    <label>用户参数</label>
                    <EditableList<ParamRule>
                        list={queryTemplate.userParamRules}
                        getLabel={this.getRuleLabel}
                        createItem={this.createRule}
                        editor={ParamRuleEditor}
                        afterChange={this.handleParamRulesChange} />
                </div>
            </div>
        );
    }
}

export default QueryTemplateForm;
export { QueryTemplateFormProps };
