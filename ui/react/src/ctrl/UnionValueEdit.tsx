import * as React from 'react';
import UnionValue from '../model/UnionValue';
import EditBox, { EditBoxChange } from './EditBox';
import Values from '../model/Values';
import { VariantType } from '../model/Variant';
import * as moment from 'moment';
import classnames from "classnames";
import './UnionValueEdit.css';

interface UninValueEditProps extends React.ClassAttributes<UnionValueEdit> {
    value: UnionValue,
    id?: string,
    name?: string,
    placeholder?: string,
    className?: string,
    style?: React.CSSProperties,
    multiline?: boolean,
    focusByDefault?: boolean,
    disabled?: boolean,
    afterChange: (change: UnionValueChange) => void,
    afterEndEdit?: () => void,
}

interface UnionValueChange {
    id?: string,
    name?: string,
    oldValue: UnionValue;
    newValue: UnionValue;
    type: 'edit' | 'commit' | 'rollback';
}

interface UnionValueEditState {
    editableString: string,
}

class UnionValueEdit extends React.Component<UninValueEditProps, UnionValueEditState> {
    protected currentValue: UnionValue;

    constructor(props: UninValueEditProps) {
        super(props);
        this.currentValue = props.value;
        this.state = {
            editableString: toEditableString(props.value)
        };

        this.afterChange = this.afterChange.bind(this);
    }

    public componentDidUpdate(prevProps: UninValueEditProps) {
        if (this.props.value !== this.currentValue) {
            this.currentValue = this.props.value;
            this.setState({
                editableString: toEditableString(this.props.value),
            });
        }
    }

    protected afterChange(change: EditBoxChange) {
        const editableString = change.type === 'rollback' ?
            change.originValue : change.nextValue;
        // if (editableString === this.state.editableString) {
        //     return; // nothing changed.
        // }
        const oldValue = this.currentValue;
        this.currentValue = fromEditableString(editableString);

        if (change.type === 'commit') {
            this.setState({ editableString });
        }

        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange({
                id: change.id,
                name: change.name,
                oldValue,
                newValue: this.currentValue,
                type: change.type,
            });
        }
    }

    public render() {
        const className = classnames("union-value-edit", this.props.className);

        return (
            <EditBox
                id={this.props.id}
                name={this.props.name}
                placeholder={this.props.placeholder}
                value={this.state.editableString}
                multiline={this.props.multiline}
                className={className}
                style={this.props.style}
                focused={this.props.focusByDefault}
                afterChange={this.afterChange}
                afterEndEdit={this.props.afterEndEdit}
                disabled={this.props.disabled}
            />
        );
    }
}

function fromEditableString(editableString: string): UnionValue {
    if (editableString.startsWith('=')) {
        return Values.formula(editableString.substring(1));
    } else if (editableString.startsWith('\'')) {
        return Values.str(editableString.substring(1));
    } else if (editableString === '') {
        return Values.blank();
    } else if (!isNaN(Number(editableString))) {
        return Values.dec(editableString);
    } else if ('true' === editableString.toLowerCase()) {
        return Values.bool(true);
    } else if ('false' === editableString.toLowerCase()) {
        return Values.bool(false);
    } else {
        const m = moment(editableString, moment.ISO_8601);
        if (m.isValid()) {
            return Values.date(m.toDate());
        } else {
            return Values.str(editableString);
        }
    }
}

function toEditableString(value: UnionValue): string {
    if (value.isFormula()) {
        return '=' + value.getFormulaString();
    } else if (value.isBlank()) {
        return ''; // treat as blank, or reconsider this?
    } else {
        switch (value.valueType()) {
            case VariantType.BLANK:
            case VariantType.DECIMAL:
            case VariantType.BOOL:
            case VariantType.DATE:
                return value.toString() || '';
            case VariantType.STRING:
                const s = value.strValue();
                if (s === null || s === '') {
                    return '';
                } else if (s.startsWith("'") ||
                    'true' === s.toLowerCase() ||
                    'false' === s.toLowerCase() ||
                    !isNaN(Number(s))) { // TODO 日期格式的字符串仍会有问题
                    return '\'' + s;
                } else {
                    return s;
                }
            case VariantType.ERROR:
            case VariantType.LIST:
            default:
                throw new Error('Not editable!');
        }
    }
}

export default UnionValueEdit;
export { UninValueEditProps, UnionValueChange, fromEditableString, toEditableString }
