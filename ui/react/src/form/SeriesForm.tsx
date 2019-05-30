import * as React from 'react';
import Series from '../model/Series';
import Values from '../model/Values';
import UnionValueEdit from '../ctrl/UnionValueEdit';
import ValueChange from '../ctrl/ValueChange';
import UnionValue from '../model/UnionValue';

interface SeriesFormProps extends React.ClassAttributes<SeriesForm> {
    series: Series,
    afterChange?: (series: Series) => void,
}

class SeriesForm extends React.Component<SeriesFormProps> {

    constructor(props: SeriesFormProps) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
    }

    protected handleChange(change: ValueChange<UnionValue>) {
        if (change.type !== 'commit') {
            return;
        }
        const series = this.props.series;
        if (change.name) {
            series[change.name] = change.toValue;
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
                        modes={["formula", "string"]}
                        afterChange={this.handleChange} />
                </label>
                <label className="field x-values">
                    <span className="field-name">X值</span>
                    <UnionValueEdit
                        name="xValues"
                        value={series.xValues.isFormula() ? series.xValues : Values.blank()}
                        modes={["formula"]}
                        afterChange={this.handleChange} />
                </label>
                <label className="field y-values">
                    <span className="field-name">Y值</span>
                    <UnionValueEdit
                        name="yValues"
                        value={series.yValues.isFormula() ? series.yValues : Values.blank()}
                        modes={["formula"]}
                        afterChange={this.handleChange} />
                </label>
                <label className="field z-values">
                    <span className="field-name">Z值</span>
                    <UnionValueEdit
                        name="zValues"
                        value={series.zValues.isFormula() ? series.zValues : Values.blank()}
                        modes={["formula"]}
                        afterChange={this.handleChange} />
                </label>
            </div>
        );
    }
}

export default SeriesForm;
export { SeriesForm };
