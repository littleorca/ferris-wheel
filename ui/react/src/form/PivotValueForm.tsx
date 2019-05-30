import * as React from 'react';
import PivotValue from '../model/PivotValue';
import EditableList, { EditorProps } from '../ctrl/EditableList';
import { AggregateTypeNames, AggregateTypeList } from '../model/AggregateType';
import EditBox from '../ctrl/EditBox';
import FormatInput from '../ctrl/FormatInput';
import ValueChange from "../ctrl/ValueChange";

interface PivotValueFormProps extends React.ClassAttributes<PivotValueForm> {
    values: PivotValue[];
    afterChange?: (values: PivotValue[]) => void;
}

const PivotValueEditor = (props: EditorProps<PivotValue>) => {
    const pivotValue = props.value;

    const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const target = event.currentTarget;
        const name = target.name;
        const value = target.value;
        pivotValue[name] = value;

        props.onSubmit(pivotValue);
    };

    const handleEditBoxChange = (change: ValueChange<string>) => {
        if (change.type !== 'commit') {
            return;
        }
        const name = change.name;
        const value = change.toValue;
        if (typeof name !== 'undefined') { // should always be true
            pivotValue[name] = value;
        }
        props.onSubmit(pivotValue);
    };

    const handleFormatChange = (format: string) => {
        pivotValue.format = format;
        props.onSubmit(pivotValue);
    }

    return (
        <div className="pivot-value-editor">
            <label className="field pivot-value-field">
                <EditBox
                    name="field"
                    placeholder="字段名"
                    value={pivotValue.field}
                    afterChange={handleEditBoxChange} />
            </label>
            <label className="field pivot-value-aggregateType">
                <select
                    name="aggregateType"
                    value={pivotValue.aggregateType}
                    onChange={handleChange}>
                    {AggregateTypeList.map((e, i) =>
                        <option
                            key={i}
                            value={e}>
                            {AggregateTypeNames[e]}
                        </option>
                    )}
                </select>
            </label>
            <label className="field pivot-value-label">
                <EditBox
                    name="label"
                    placeholder="标签"
                    value={pivotValue.label}
                    afterChange={handleEditBoxChange} />
            </label>
            <label className="field pivot-value-format">
                <FormatInput
                    format={pivotValue.format}
                    afterChange={handleFormatChange} />
            </label>
        </div>
    );
};

class PivotValueForm extends React.Component<PivotValueFormProps> {

    constructor(props: PivotValueFormProps) {
        super(props);
    }

    protected getPivotValueLabel(value: PivotValue) {
        return AggregateTypeNames[value.aggregateType]
            + ': ' + (value.label !== '' ? value.label : value.field);
    }

    public render() {
        const values = this.props.values;
        return (
            <EditableList<PivotValue>
                className="pivot-value-form"
                list={values}
                getLabel={this.getPivotValueLabel}
                createItem={createPivotValue}
                editor={PivotValueEditor}
                afterChange={this.props.afterChange} />
        );
    }
}

function createPivotValue() {
    return new PivotValue();
}

export default PivotValueForm;
export { PivotValueFormProps };
