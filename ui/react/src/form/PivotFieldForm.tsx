import * as React from 'react';
import PivotField from '../model/PivotField';
import EditableList, { EditorProps } from '../ctrl/EditableList';
import EditBox from '../ctrl/EditBox';
import ValueChange from "../ctrl/ValueChange";

interface PivotFieldFormProps extends React.ClassAttributes<PivotFieldForm> {
    fields: PivotField[];
    afterChange?: (fields: PivotField[]) => void;
}

const PivotFieldEditor = (props: EditorProps<PivotField>) => {
    const pivotValue = props.value;

    const handleChange = (change: ValueChange<string>) => {
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

    return (
        <div className="pivot-field-editor">
            <label className="field pivot-field-field">
                <EditBox
                    name="field"
                    placeholder="字段名"
                    value={pivotValue.field}
                    afterChange={handleChange} />
            </label>
            {/* <label className="field pivot-field-label">
                <EditBox
                    name="label"
                    placeholder="标签"
                    value={pivotValue.label}
                    afterChange={handleChange} />
            </label> */}
        </div>
    );
};

class PivotFieldForm extends React.Component<PivotFieldFormProps> {

    constructor(props: PivotFieldFormProps) {
        super(props);
    }

    protected getPivotFieldLabel(field: PivotField) {
        return /*field.label !== '' ? field.label :*/ field.field;
    }

    public render() {
        const fields = this.props.fields;
        return (
            <EditableList<PivotField>
                className="pivot-field-form"
                list={fields}
                getLabel={this.getPivotFieldLabel}
                createItem={createPivotField}
                editor={PivotFieldEditor}
                afterChange={this.props.afterChange} />
        );
    }
}

function createPivotField() {
    return new PivotField();
}

export default PivotFieldForm;
export { PivotFieldFormProps };
