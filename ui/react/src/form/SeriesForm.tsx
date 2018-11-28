import * as React from 'react';
import { Series, Values } from '../model';
import { UnionValueEdit, UnionValueChange } from '../ctrl';

interface SeriesFormProps extends React.ClassAttributes<SeriesForm> {
    series: Series,
    afterChange?: (series: Series) => void,
}

class SeriesForm extends React.Component<SeriesFormProps> {

    constructor(props: SeriesFormProps) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
    }

    protected handleChange(change: UnionValueChange) {
        if (change.type !== 'commit') {
            return;
        }
        const series = this.props.series;
        if (change.name) {
            series[change.name] = change.newValue;
        }
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(series);
        }
    }

    public render() {
        const series = this.props.series;
        return (
            <div className="series-form">
                <label className="field name">
                    <span className="field-name">名称</span>
                    <UnionValueEdit
                        name="name"
                        value={series.name}
                        afterChange={this.handleChange} />
                </label>
                <label className="field x-values">
                    <span className="field-name">X值</span>
                    <UnionValueEdit
                        name="xValues"
                        value={series.xValues.isFormula() ? series.xValues : Values.blank()}
                        afterChange={this.handleChange} />
                </label>
                <label className="field y-values">
                    <span className="field-name">Y值</span>
                    <UnionValueEdit
                        name="yValues"
                        value={series.yValues.isFormula() ? series.yValues : Values.blank()}
                        afterChange={this.handleChange} />
                </label>
                <label className="field z-values">
                    <span className="field-name">Z值</span>
                    <UnionValueEdit
                        name="zValues"
                        value={series.zValues.isFormula() ? series.zValues : Values.blank()}
                        afterChange={this.handleChange} />
                </label>
            </div>
        );
    }
}

export default SeriesForm;
export { SeriesForm };
