import * as React from 'react';
import UnionValueEdit from '../ctrl/UnionValueEdit';
import Parameter from '../model/Parameter';
import { VariantType } from '../model/Variant';
import UnionValue from "../model/UnionValue";
import EditBox from '../ctrl/EditBox';
import ValueChange from "../ctrl/ValueChange";
import Field from './Field';
import Select from '../ctrl/Select';
import CheckBox from '../ctrl/CheckBox';

interface ParameterFormProps extends React.ClassAttributes<ParameterForm> {
    parameter: Parameter,
    afterChange?: (namedValue: Parameter) => void,
}

class ParameterForm extends React.Component<ParameterFormProps> {

    constructor(props: ParameterFormProps) {
        super(props);

        this.handleNameChange = this.handleNameChange.bind(this);
        this.handleValueChange = this.handleValueChange.bind(this);
        this.handleTypeChange = this.handleTypeChange.bind(this);
    }

    protected handleNameChange(change: ValueChange<string>) {
        if (change.type !== 'commit') {
            return;
        }
        const param = this.props.parameter;
        const value = change.toValue;
        param.name = value;
        this.onUpdate();
    }

    protected handleValueChange(change: ValueChange<UnionValue>) {
        if (change.type !== 'commit') {
            return;
        }
        const param = this.props.parameter;
        param.value = change.toValue;
        this.onUpdate();
    }

    protected handleTypeChange(newVal: string) {
        const param = this.props.parameter;
        if (newVal !== "") {
            param.type = newVal as VariantType;
        } else {
            param.type = VariantType.BLANK;
        }
        this.onUpdate();
    }

    protected onUpdate() {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.parameter);
        }
        this.forceUpdate();
    }

    public render() {
        const param = this.props.parameter;
        return (
            <div className="parameter-form">
                <Field
                    className="field param-name"
                    name="field-param-name"
                    label="名称">
                    <EditBox
                        name="name"
                        value={param.name}
                        afterChange={this.handleNameChange} />
                </Field>
                <Field
                    className="field param-value"
                    name="field-param-value"
                    label="默认值">
                    <UnionValueEdit
                        value={param.value}
                        afterChange={this.handleValueChange} />
                </Field>
                <Field
                    className="field param-type"
                    name="field-param-type"
                    label="类型">
                    <Select name="type"
                        value={param.type || VariantType.BLANK}
                        options={[
                            { value: VariantType.BLANK, label: "请选择…" },
                            { value: VariantType.STRING, label: "字符串" },
                            { value: VariantType.DECIMAL, label: "数字" },
                            { value: VariantType.BOOL, label: "布尔" },
                            { value: VariantType.DATE, label: "日期" },
                        ]}
                        afterChange={this.handleTypeChange} />
                </Field>
                <Field
                    className="field param-multiple"
                    name="field-param-multiple"
                    label="多值">
                    <CheckBox
                        name="multiple"
                        value={param.multiple}
                        afterChange={newVal => {
                            param.multiple = newVal;
                            this.onUpdate();
                        }} />
                </Field>
            </div>
        );
    }
}

export default ParameterForm;
export { ParameterFormProps as ParameterFormProps };
