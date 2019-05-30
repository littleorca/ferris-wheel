import * as React from 'react';
import PivotFilter from '../model/PivotFilter';
import EditableList, { EditorProps } from '../ctrl/EditableList';
import EditBox from '../ctrl/EditBox';
import ValueChange from "../ctrl/ValueChange";

interface PivotFilterFormProps extends React.ClassAttributes<PivotFilterForm> {
    filters: PivotFilter[];
    afterChange?: (filters: PivotFilter[]) => void;
}

const PivotFilterEditor = (props: EditorProps<PivotFilter>) => {
    const pivotFilter = props.value;

    const handleChange = (change: ValueChange<string>) => {
        if (change.type !== 'commit') {
            return;
        }
        const name = change.name;
        const value = change.toValue;
        if (typeof name !== 'undefined') { // should always be ture
            pivotFilter[name] = value;
        }
        props.onSubmit(pivotFilter);
    };

    return (
        <div className="pivot-filter-editor">
            <label className="field pivot-filter-field">
                <EditBox
                    name="field"
                    placeholder="字段名"
                    value={pivotFilter.field}
                    afterChange={handleChange} />
            </label>
        </div>
    );
};

class PivotFilterForm extends React.Component<PivotFilterFormProps> {

    constructor(props: PivotFilterFormProps) {
        super(props);
    }

    protected getPivotFilterLabel(filter: PivotFilter) {
        return filter.field;
    }

    public render() {
        const filters = this.props.filters;
        return (
            <EditableList<PivotFilter>
                className="pivot-filter-form"
                list={filters}
                getLabel={this.getPivotFilterLabel}
                createItem={createPivotFilter}
                editor={PivotFilterEditor}
                afterChange={this.props.afterChange} />
        );
    }
}

function createPivotFilter() {
    return new PivotFilter();
}

export default PivotFilterForm;
export { PivotFilterFormProps };
