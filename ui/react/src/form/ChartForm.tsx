import * as React from 'react';
import Chart from '../model/Chart';
import Binder from '../model/Binder';
import Series from '../model/Series';
import UnionValueEdit from '../ctrl/UnionValueEdit';
import ValueChange from "../ctrl/ValueChange";
import EditableList, { EditorProps } from '../ctrl/EditableList';
import AxisForm from './AxisForm';
import DataBinderForm from './DataBinderForm';
import SeriesBinderForm from './SeriesBinderForm';
import Axis from '../model/Axis'
import Values from '../model/Values';
import UnionValue from '../model/UnionValue';
import './ChartForm.css';

interface ChartFormProps extends React.ClassAttributes<ChartForm> {
    chart: Chart;
    afterChange?: (chart: Chart) => void;
}

interface ChartFormState {
    useBinder: boolean;
    namedAxes: NamedAxis[];
}

interface NamedAxis {
    name: string;
    axis(): Axis;
}

class ChartForm extends React.Component<ChartFormProps, ChartFormState> {

    constructor(props: ChartFormProps) {
        super(props);

        this.state = this.createInitialState(props);

        this.handleInputChange = this.handleInputChange.bind(this);
        this.handleVariantChange = this.handleVariantChange.bind(this);
        this.toggleUseBinder = this.toggleUseBinder.bind(this);
        this.handleAutoBinderChange = this.handleAutoBinderChange.bind(this);
        this.handleSeriesBinderChange = this.handleSeriesBinderChange.bind(this);
        this.handleAxesChange = this.handleAxesChange.bind(this);
    }

    public componentDidUpdate(prevProps: ChartFormProps) {
        if (this.props.chart !== prevProps.chart) {
            this.setState(this.createInitialState(this.props));
        }
    }

    protected createInitialState(props: ChartFormProps) {
        const chart = props.chart;
        const namedAxes = [
            { name: "X轴", axis: () => chart.xAxis },
            { name: "Y轴", axis: () => chart.yAxis },
            { name: "Z轴", axis: () => chart.zAxis }
        ];
        const hasBinder = chart.binder.data.isFormula();
        const hasData = chart.categories.isFormula() ||
            !chart.categories.isBlank() ||
            chart.series.length > 0
        return {
            useBinder: hasBinder || !hasData,
            namedAxes,
        };
    }

    protected handleInputChange(event: React.ChangeEvent<HTMLInputElement>) {
        const chart = this.props.chart;
        const target = event.currentTarget;
        const name = target.name;
        const value = target.value;
        chart[name] = value;
        this.forceUpdate();
        this.onUpdate();
    }

    protected handleVariantChange(change: ValueChange<UnionValue>) {
        if (change.type !== 'commit') {
            return;
        }
        const chart = this.props.chart;
        if (change.name) {
            chart[change.name] = change.toValue;
        } // else throw new Error();
        this.onUpdate();
    }

    protected toggleUseBinder(event: React.ChangeEvent<HTMLInputElement>) {
        // const cs = this.props.chart;
        this.setState({
            useBinder: event.currentTarget.checked
        });
    }

    protected handleAutoBinderChange(binder: Binder) {
        this.onUpdate();
    }

    protected handleSeriesBinderChange(series: Series[]) {
        this.onUpdate();
    }

    protected handleAxesChange(namedAxes: NamedAxis[]) {
        this.onUpdate();
    }

    protected onUpdate() {
        if (typeof this.props.afterChange !== 'undefined') {
            if (!this.state.useBinder &&
                this.props.chart.binder.data.isFormula()) {
                this.props.chart.binder.data = Values.blank();
            }
            this.props.afterChange(this.props.chart);
        }
    }

    protected getLabel(namedAxis: NamedAxis, index: number) {
        return namedAxis.name;
    }

    public render() {
        const chart = this.props.chart;
        const useBinder = this.state.useBinder;
        const namedAxes = this.state.namedAxes;

        return (
            <div className="chart-form">
                <input
                    type="hidden"
                    name="type"
                    value={chart.type}
                    onChange={this.handleInputChange} />
                <input
                    type="hidden"
                    name="name"
                    value={chart.name}
                    onChange={this.handleInputChange} />
                <div className="chart-data">
                    <label className="field chart-title">
                        <span className="field-name">标题</span>
                        <UnionValueEdit
                            name="title"
                            placeholder="输入图表标题，支持公式。"
                            value={chart.title}
                            modes={["formula", "string"]}
                            afterChange={this.handleVariantChange} />
                    </label>
                    <label className="field auto-bind">
                        <span />
                        <input
                            type="checkbox"
                            checked={useBinder}
                            onChange={this.toggleUseBinder} />
                        <span>自动绑定</span>
                    </label>
                    {useBinder ?
                        (
                            <DataBinderForm
                                className="data-binder auto-binder"
                                binder={chart.binder}
                                afterChange={this.handleAutoBinderChange} />
                        )
                        :
                        (
                            <div className="data-binder manual-binder">
                                <SeriesBinderForm
                                    series={chart.series}
                                    afterChange={this.handleSeriesBinderChange} />
                                <div className="category-settings">
                                    <label className="field categories">
                                        <span className="field-name">水平（分类）轴标签</span>
                                        <UnionValueEdit
                                            name="categories"
                                            value={chart.categories.isFormula() ? chart.categories : Values.blank()}
                                            modes={["formula"]}
                                            afterChange={this.handleVariantChange} />
                                    </label>
                                </div>
                            </div>
                        )
                    }
                </div>

                <EditableList<NamedAxis>
                    className="axes-option"
                    horizontal={true}
                    list={namedAxes}
                    sortable={false}
                    addible={false}
                    removable={false}
                    getLabel={this.getLabel}
                    editor={AxisEditor}
                    afterChange={this.handleAxesChange} />
            </div>
        );
    }
}

const AxisEditor = (props: EditorProps<NamedAxis>) => {
    const afterChange = (axis: Axis) => {
        props.onSubmit(props.value);
    }
    return (
        <AxisForm {...props}
            axis={props.value.axis()}
            afterChange={afterChange} />
    );
}

export default ChartForm;
export { ChartFormProps };
