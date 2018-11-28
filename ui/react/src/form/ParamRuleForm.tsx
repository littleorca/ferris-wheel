import * as React from 'react';
import ParamRule from '../model/ParamRule';
import { VariantType } from '../model/Variant';
import UnionValue from '../model/UnionValue';
import UnionValueListEdit from '../ctrl/UnionValueListEdit';
import EditBox, { EditBoxChange } from '../ctrl/EditBox';

interface ParamRuleFormProps extends React.ClassAttributes<ParamRuleForm> {
    rule: ParamRule,
    afterChange?: (rule: ParamRule) => void,
}

class ParamRuleForm extends React.Component<ParamRuleFormProps> {
    constructor(props: ParamRuleFormProps) {
        super(props);

        this.handleNameChange = this.handleNameChange.bind(this);
        this.handleRuleChange = this.handleRuleChange.bind(this);
        this.handleAllowedValuesChange = this.handleAllowedValuesChange.bind(this);
    }

    protected handleNameChange(change: EditBoxChange) {
        if (change.type !== 'commit') {
            return;
        }
        const value = change.nextValue;
        this.props.rule.name = value;
        this.onUpdate();
    }

    protected handleRuleChange(event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
        const target = event.currentTarget;
        const name = target.name;
        const value = target.type === 'checkbox' ?
            (target as HTMLInputElement).checked : target.value;
        this.props.rule[name] = value;
        this.forceUpdate();
        this.onUpdate();
    }

    protected handleAllowedValuesChange(list: UnionValue[]) {
        this.onUpdate();
    }

    protected onUpdate() {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.rule);
        }
    }

    public render() {
        const rule = this.props.rule;
        return (
            <div className="param-rule-form">
                <label className="field rule-name">
                    <span className="field-name">名称</span>
                    <EditBox
                        name="name"
                        value={rule.name}
                        afterChange={this.handleNameChange} />
                </label>
                <label className="field rule-type">
                    <span className="field-name">类型</span>
                    <select
                        name="type"
                        value={rule.type}
                        onChange={this.handleRuleChange}>
                        <option value={VariantType.DECIMAL}>数字</option>
                        <option value={VariantType.BOOL}>布尔</option>
                        <option value={VariantType.DATE}>日期</option>
                        <option value={VariantType.STRING}>字符串</option>
                        {/* <option value={VariantType.LIST}>列表</option> */}
                    </select>
                </label>
                {/* <label className="field rule-nullable">
                    <span className="field-name">&nbsp;</span>
                    <input
                        type="checkbox"
                        name="nullable"
                        checked={rule.nullable}
                        onChange={this.handleRuleChange} />
                    <span>允许为空</span>
                </label> */}
                <div className="field rule-allowedValues">
                    <label className="field-name">选项</label>
                    <UnionValueListEdit
                        list={rule.allowedValues}
                        afterChange={this.handleAllowedValuesChange} />
                </div>
            </div>
        );
    }
}

export default ParamRuleForm;
export { ParamRuleFormProps };
