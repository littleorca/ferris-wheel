import * as React from "react";
import FormField from "../model/FormField";
import Field from "./Field";
import Variant, { VariantType } from "../model/Variant";
import CheckBox from "../ctrl/CheckBox";
import DateSelect from "../ctrl/DateSelect";
import Values from "../model/Values";
import NumberInput from "../ctrl/NumberInput";
import Select from "../ctrl/Select";
import SelectOption from "../ctrl/SelectOption";
import DropdownOmniInput from "../ctrl/DropdownOmniInput";
import UnionValue from "../model/UnionValue";
import classnames from "classnames";
import "./SmartField.css";

interface SmartFieldProps extends React.ClassAttributes<SmartField> {
    className?: string;
    style?: React.CSSProperties;
    field: FormField;
    afterChange?: (newValue: Variant, name?: string) => void;
}

class SmartField extends React.PureComponent<SmartFieldProps> {
    constructor(props: SmartFieldProps) {
        super(props);
        this.afterChange = this.afterChange.bind(this);
    }

    protected afterChange() {
        if (typeof this.props.afterChange === 'function') {
            const field = this.props.field;
            this.props.afterChange(field.value, field.name);
        }
        this.forceUpdate();
    }

    render() {
        const className = classnames("smart-field", this.props.className);
        const field = this.props.field;

        return (
            <Field
                className={className}
                style={this.props.style}
                name={field.name}
                label={field.label}
                tips={field.tips}>

                {this.renderControl(field)}

            </Field>
        );
    }

    protected renderControl(field: FormField) {
        if (field.type === VariantType.BOOL) {
            const value = field.value.isBlank() ? false : field.value.booleanValue();
            return (
                <CheckBox
                    name={field.name}
                    // label={field.label}
                    value={value}
                    afterChange={newValue => {
                        field.value = Values.bool(newValue);
                        this.afterChange();
                    }} />
            );

        } else if (field.type === VariantType.DATE) {
            const value = field.value.isBlank() ? new Date() : field.value.dateValue();
            return (
                <DateSelect
                    name={field.name}
                    value={value}
                    afterChange={newValue => {
                        field.value = Values.date(newValue);
                        this.afterChange();
                    }} />
            );

        } else if (field.type === VariantType.DECIMAL || field.type === VariantType.STRING) {
            if (field.options.isBlank()) { // with out options (free input)
                if (!field.multiple) {
                    // normal input
                    return this.renderSingleInput(field);
                } else {
                    // multiple input
                    return this.renderMultipleInput(field);
                }

            } else { // with options (disable free input)
                if (!field.multiple) {
                    // single select
                    return this.renderSingleSelect(field);
                } else {
                    // multiple select
                    return this.renderMultipleSelect(field);
                }
            }

        } else {
            return (
                <div>Field not supported! name={field.name} type={field.type}</div>
            );
        }
    }

    // type = decimal or string
    protected renderSingleInput(field: FormField) {
        if (field.type === VariantType.DECIMAL) {
            const value = field.value.isBlank() ? 0 : field.value.numberValue();
            return (
                <NumberInput
                    placeholder={field.label}
                    name={field.name}
                    value={value}
                    afterChange={value => {
                        field.value = Values.dec(value);
                        this.afterChange();
                    }} />
            );

        } else {
            return (
                <input
                    type="text"
                    placeholder={field.label}
                    name={field.name}
                    value={field.value.toString()}
                    onChange={e => {
                        field.value = Values.str(e.currentTarget.value);
                        this.afterChange();
                    }} />
            );
        }
    }

    // type = decimal or string
    protected renderMultipleInput(field: FormField) {
        const value: string[] = [];
        if (field.value.valueType() === VariantType.LIST) {
            for (let i = 0; i < field.value.itemCount(); i++) {
                value.push(field.value.item(i).toString());
            }
        } else if (!field.value.isBlank()) {
            value.push(field.value.toString());
        }
        const handleChange = (value: string[]) => {
            const values = value.map(v => field.type === VariantType.DECIMAL ?
                Values.dec(v) : Values.str(v));
            field.value = Values.list(values);
            this.afterChange();
        }
        return (
            <DropdownOmniInput
                name={field.name}
                label={field.label}
                tips={field.tips}
                value={value}
                afterChange={handleChange} />
        );
    }

    // type = decimal or string
    protected renderSingleSelect(field: FormField) {
        const options: SelectOption[] = this.toOptionList(field.type, field.options);
        const value = this.toOptionValue(field.type, field.value);
        return (
            <Select
                name={field.name}
                value={value}
                options={options}
                afterChange={value => {
                    field.value = Values.withType(field.type, value);
                    this.afterChange();
                }} />
        );
    }

    // type = decimal or string
    protected renderMultipleSelect(field: FormField) {
        const selected: string[] = [];
        const options: SelectOption[] = this.toOptionList(field.type, field.options);
        if (field.value.valueType() === VariantType.LIST) {
            for (let i = 0; i < field.value.itemCount(); i++) {
                selected.push(this.toOptionValue(field.type, field.value.item(i)));
            }
        } else if (!field.value.isBlank()) {
            selected.push(this.toOptionValue(field.type, field.value));
        }
        return (
            <DropdownOmniInput
                name={field.name}
                label={field.label}
                tips={field.tips}
                value={selected}
                options={options}
                afterChange={value => {
                    const list: UnionValue[] = [];
                    value.forEach(item => {
                        list.push(Values.withType(field.type, item));
                    });
                    field.value = Values.list(list);
                    this.afterChange();
                }} />
        );
    }

    protected toOptionList(type: VariantType, options: UnionValue): SelectOption[] {
        const result: SelectOption[] = [];
        if (options.valueType() === VariantType.LIST) {
            for (let i = 0; i < options.itemCount(); i++) {
                const value = this.toOptionValue(type, options.item(i));
                result.push({ value });
            }

        } else {
            const value = this.toOptionValue(type, options);
            result.push({ value });
        }
        return result;
    }

    protected toOptionValue(type: VariantType, value: Variant) {
        if (type === VariantType.DECIMAL) {
            return value.isBlank() ? "" : value.decimalValue();
        } else if (type === VariantType.STRING) {
            return value.toString();
        } else {
            throw new Error("Unsupported variant.");
        }
    }
}

export default SmartField;
