import * as React from 'react';
import * as ReactDOM from 'react-dom';
import ParamRule from '../model/ParamRule';
import { VariantType } from '../model/Variant';
import NamedValue from '../model/NamedValue';
import Values from '../model/Values';
import NumberInput from '../ctrl/NumberInput';
import EditBox, { EditBoxChange } from '../ctrl/EditBox';
import UnionValueListEdit from '../ctrl/UnionValueListEdit';
import * as moment from 'moment';
import DatePicker from 'react-datepicker';
import UnionValue from '../model/UnionValue';
import Button from '../ctrl/Button';
import classnames from "classnames";
import 'react-datepicker/dist/react-datepicker.css';
import './AutoForm.css';

interface AutoFormProps extends React.ClassAttributes<AutoForm> {
    rules: ParamRule[];
    params?: NamedValue[];
    className?: string;
    afterChange?: (param: NamedValue) => void;
    onSumbit?: (params: NamedValue[]) => void;
}

interface Field {
    rule: ParamRule;
    param: NamedValue;
}

interface AutoFormState {
    fields: Field[];
}

class AutoForm extends React.Component<AutoFormProps, AutoFormState> {
    constructor(props: AutoFormProps) {
        super(props);

        this.state = this.createInitialState(props);

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    public componentDidUpdate(prevProps: AutoFormProps) {
        if (this.props.params !== prevProps.params ||
            this.props.rules !== prevProps.rules) {
            this.setState(this.createInitialState(this.props));
        }
    }

    protected createInitialState(props: AutoFormProps) {
        const fields: Field[] = [];
        const mapping = new Map<string, UnionValue>();
        if (typeof props.params !== 'undefined') {
            for (const value of props.params) {
                mapping.set(value.name, value.value);
            }
        }
        for (const rule of props.rules) {
            let value = mapping.get(rule.name);
            if (value === undefined) {
                value = Values.blank();
            }
            const param: NamedValue = {
                name: rule.name,
                value
            };
            fields.push({ rule, param });
        }
        return { fields };
    }

    protected handleChange(param: NamedValue) {
        this.forceUpdate();
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(param);
        }
    }

    protected handleSubmit(buttonName: string) {
        if (typeof this.props.onSumbit !== 'undefined') {
            const params = [];
            for (const field of this.state.fields) {
                params.push(field.param);
            }
            this.props.onSumbit(params);
        }
    }

    public render() {
        const className = classnames("auto-form", this.props.className);

        return (
            <div className={className}>
                {this.state.fields.map((f, i) => {
                    return (
                        <FieldInput
                            key={f.rule.name}
                            field={f}
                            className={"field-input type-" + f.rule.type}
                            afterChange={this.handleChange} />
                    )
                })}
                <Button
                    name="query"
                    label="查询/刷新"
                    tips="点击查询/刷新"
                    onClick={this.handleSubmit} />
            </div>
        );
    }
}

interface FieldInputProps extends React.ClassAttributes<any> {
    field: Field;
    className?: string;
    afterChange?: (param: NamedValue) => void;
}

function FieldInput(props: FieldInputProps) {
    let safeWithLabel = true;
    let InputNode;
    switch (props.field.rule.type) {
        case VariantType.BOOL:
            InputNode = BoolFieldInput(props);
            break;
        case VariantType.DECIMAL:
            if (props.field.rule.allowedValues.length > 0) {
                InputNode = DecimalFieldSelect(props);
            } else {
                InputNode = DecimalFieldInput(props);
            }
            break;
        case VariantType.STRING:
            if (props.field.rule.allowedValues.length > 0) {
                InputNode = StringFieldSelect(props);
            } else {
                InputNode = StringFieldInput(props);
            }
            break;
        case VariantType.DATE:
            InputNode = DateFieldInput(props);
            break;
        case VariantType.LIST:
            InputNode = ListFieldInput(props);
            safeWithLabel = false;
            break;
        default:
            throw new Error('Unsupported variant type: ' +
                props.field.rule.type);
    }

    return safeWithLabel ?
        (
            <label className={classnames(
                "field name-" + props.field.rule.name,
                "type-" + props.field.rule.type)}>
                <span className="field-name">{props.field.rule.name}</span>
                {InputNode}
            </label>
        ) : (
            <div className={classnames(
                "field " + props.field.rule.name,
                "type-" + props.field.rule.type)}>
                <span className="field-name">{props.field.rule.name}</span>
                {InputNode}
            </div>
        );
}

function BoolFieldInput(props: FieldInputProps) {
    const value = props.field.param.value;

    const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const target = event.currentTarget;
        props.field.param.value = Values.auto(target.checked);
        if (typeof props.afterChange !== 'undefined') {
            props.afterChange(props.field.param);
        }
    }

    return (
        <input
            type="checkbox"
            name={props.field.rule.name}
            checked={value.isBlank() ? false : value.booleanValue()}
            className={props.className}
            onChange={handleChange} />
    );
}

function DecimalFieldSelect(props: FieldInputProps) {
    const value = props.field.param.value;

    const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const num = Number(event.currentTarget.value);
        props.field.param.value = Values.dec(num);
        if (typeof props.afterChange !== 'undefined') {
            props.afterChange(props.field.param);
        }
    };

    return (
        <select
            name={props.field.rule.name}
            value={value.isBlank() ? '' : value.decimalValue()}
            className={props.className}
            onChange={handleChange}>
            <option value="" />
            {props.field.rule.allowedValues.map((e, i) => {
                const val = e.decimalValue();
                return (
                    <option
                        key={val}
                        value={val}
                        title={val}>
                        {val}
                    </option>
                );
            })}
        </select>
    );
}

function DecimalFieldInput(props: FieldInputProps) {
    const value = props.field.param.value;

    const handleChange = (newVal: number) => {
        props.field.param.value = Values.dec(newVal);
        if (typeof props.afterChange !== 'undefined') {
            props.afterChange(props.field.param);
        }
    };

    return (
        <NumberInput
            name={props.field.rule.name}
            value={value.isBlank() ? 0 : value.numberValue()}
            className={props.className}
            afterChange={handleChange} />
    );
}

function StringFieldSelect(props: FieldInputProps) {
    const value = props.field.param.value;

    const handleChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
        const newVal = event.currentTarget.value;
        props.field.param.value = Values.str(newVal);
        if (typeof props.afterChange !== 'undefined') {
            props.afterChange(props.field.param);
        }
    };

    return (
        <select
            name={props.field.rule.name}
            value={value.isBlank() ? '' : value.strValue()}
            className={props.className}
            onChange={handleChange}>
            <option value="" />
            {props.field.rule.allowedValues.map((e, i) => {
                const val = e.strValue();
                return (
                    <option
                        key={val}
                        value={val}
                        title={val}>
                        {val}
                    </option>
                );
            })}
        </select>
    );
}

function StringFieldInput(props: FieldInputProps) {
    const value = props.field.param.value;

    const handleChange = (change: EditBoxChange) => {
        if (change.type !== 'commit') {
            return;
        }
        props.field.param.value = Values.str(change.nextValue);
        if (typeof props.afterChange !== 'undefined') {
            props.afterChange(props.field.param);
        }
    };

    return (
        <EditBox
            name={props.field.rule.name}
            value={value.isBlank() ? "" : value.strValue()}
            className={props.className}
            afterChange={handleChange} />
    );
}

function DateFieldInput(props: FieldInputProps) {
    const value = props.field.param.value;
    const m = value.isBlank() ? moment() : moment(value.dateValue());

    const properContainerFunc = (props: { children: React.ReactNode[] }) => {
        return ReactDOM.createPortal(props.children, document.body);
    };

    const handleChange = (newMoment: moment.Moment) => {
        const newVal = Values.date(newMoment.toDate());
        props.field.param.value = newVal;
        if (typeof props.afterChange !== 'undefined') {
            props.afterChange(props.field.param);
        }
    }

    return (
        <DatePicker
            popperContainer={properContainerFunc}
            dateFormat="YYYY-MM-DD HH:mm"
            selected={m}
            showTimeSelect={true}
            onChange={handleChange} />
    );
}

function ListFieldInput(props: FieldInputProps) {
    const value = props.field.param.value;
    const list = (typeof value.list === 'undefined') ? [] : value.list.items;

    const handleChange = (newList: UnionValue[]) => {
        if (typeof value.list === 'undefined') {
            props.field.param.value = Values.auto(newList);
        }
        if (typeof props.afterChange !== 'undefined') {
            props.afterChange(props.field.param);
        }
    };

    return (
        <div className={props.className}>
            <UnionValueListEdit
                list={list}
                type={props.field.rule.type}
                afterChange={handleChange} />
        </div>
    );
}

export default AutoForm;
export { AutoFormProps };
