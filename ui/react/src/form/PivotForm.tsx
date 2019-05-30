import * as React from 'react';
import PivotAutomaton from '../model/PivotAutomaton';
import UnionValue from "../model/UnionValue";
import UnionValueEdit from '../ctrl/UnionValueEdit';
import ValueChange from "../ctrl/ValueChange";
import PivotFilterForm from './PivotFilterForm';
import PivotFieldForm from './PivotFieldForm';
import PivotValueForm from './PivotValueForm';
import PivotFilter from '../model/PivotFilter';
import PivotField from '../model/PivotField';
import PivotValue from '../model/PivotValue';

interface PivotFormProps extends React.ClassAttributes<PivotForm> {
    pivot: PivotAutomaton;
    afterChange?: (pivot: PivotAutomaton) => void;
}

class PivotForm extends React.Component<PivotFormProps> {

    constructor(props: PivotFormProps) {
        super(props);

        this.handleDataChange = this.handleDataChange.bind(this);
        this.handleFilterChange = this.handleFilterChange.bind(this);
        this.handleColumnChange = this.handleColumnChange.bind(this);
        this.handleRowChange = this.handleRowChange.bind(this);
        this.handleValueChange = this.handleValueChange.bind(this);
    }

    protected handleDataChange(change: ValueChange<UnionValue>) {
        const pivot = this.props.pivot;
        if (change.type === 'commit') {
            pivot.data = change.toValue;
            this.onUpdate();
        }
    }

    protected handleFilterChange(filters: PivotFilter[]) {
        this.onUpdate();
    }

    protected handleColumnChange(columns: PivotField[]) {
        this.onUpdate();
    }

    protected handleRowChange(rows: PivotField[]) {
        this.onUpdate();
    }

    protected handleValueChange(values: PivotValue[]) {
        this.onUpdate();
    }

    protected onUpdate() {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.pivot);
        }
    }

    public render() {
        const pivot = this.props.pivot;

        return (
            <div className="pivot-form">
                <div className="pivot-form-row">
                    <label className="field pivot-data">
                        <span className="field-name">区域</span>
                        <UnionValueEdit
                            value={pivot.data}
                            modes={["formula"]}
                            afterChange={this.handleDataChange} />
                    </label>
                </div>
                <div className="pivot-form-row">
                    <div className="pivot-filters">
                        <label>过滤</label>
                        <PivotFilterForm
                            filters={pivot.filters}
                            afterChange={this.handleFilterChange} />
                    </div>
                    <div className="pivot-columns">
                        <label>列</label>
                        <PivotFieldForm
                            fields={pivot.columns}
                            afterChange={this.handleColumnChange} />
                    </div>
                </div>
                <div className="pivot-form-row">
                    <div className="pivot-rows">
                        <label>行</label>
                        <PivotFieldForm
                            fields={pivot.rows}
                            afterChange={this.handleRowChange} />
                    </div>
                    <div className="pivot-values">
                        <label>值</label>
                        <PivotValueForm
                            values={pivot.values}
                            afterChange={this.handleValueChange} />
                    </div>
                </div>
            </div>
        );
    }
}


export default PivotForm;
export { PivotFormProps };
