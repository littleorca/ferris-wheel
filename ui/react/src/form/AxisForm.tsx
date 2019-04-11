import * as React from 'react';
import Axis from '../model/Axis';
import Placement from '../model/Placement';
import AxisBand from '../model/AxisBand';
import Interval from '../model/Interval';
import Stacking from '../model/Stacking';
import EditableList, { EditorProps } from '../ctrl/EditableList';
import IntervalInput from '../ctrl/IntervalInput';
import BandForm from './BandForm';
import EditBox, { EditBoxChange } from '../ctrl/EditBox';
import FormatInput from '../ctrl/FormatInput';

interface AxisFormProps extends React.ClassAttributes<AxisForm> {
    axis: Axis,
    afterChange: (axis: Axis) => void,
}

const BandEditor = (props: EditorProps<AxisBand>) => {
    return (
        <BandForm
            band={props.value}
            afterChange={props.onSubmit}
            {...props} />
    );
}

class AxisForm extends React.Component<AxisFormProps> {

    constructor(props: AxisFormProps) {
        super(props);

        this.handleChange = this.handleChange.bind(this);
        this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
        this.handleEditBoxChange = this.handleEditBoxChange.bind(this);
        this.handleIntervalChange = this.handleIntervalChange.bind(this);
        this.handleBandsChange = this.handleBandsChange.bind(this);
        this.handleFormatChange = this.handleFormatChange.bind(this);
    }

    protected handleChange(event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
        const target = event.currentTarget;
        const name = target.name;
        const value = target.value;
        const axis = this.props.axis;
        axis[name] = value;
        this.onUpdate();
    }

    protected handleCheckboxChange(event: React.ChangeEvent<HTMLInputElement>) {
        const target = event.currentTarget;
        const name = target.name;
        const axis = this.props.axis;
        axis[name] = (target as HTMLInputElement).checked;
        this.onUpdate();
    }

    protected handleEditBoxChange(change: EditBoxChange) {
        if (change.type !== 'commit') {
            return;
        }
        const name = change.name;
        const value = change.nextValue;
        const axis = this.props.axis;

        if (typeof name !== 'undefined') { // sould always be true
            axis[name] = value;
        }
        this.onUpdate();
    }

    protected handleIntervalChange(interval: Interval) {
        // nothing to do
        this.onUpdate();
    }

    protected handleBandsChange(list: AxisBand[]) {
        // nothing to do
        this.onUpdate();
    }

    protected handleFormatChange(format: string) {
        this.props.axis.format = format;
        this.onUpdate();
    }

    protected onUpdate() {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.axis);
        }
        this.forceUpdate();
    }

    protected getBandLabel(band: AxisBand) {
        return band.interval.from + '-' + band.interval.to;
    }

    protected createNewBand(): AxisBand {
        return new AxisBand();
    }

    public render() {
        const axis = this.props.axis;
        return (
            <div className="axis-form">
                <label className="field axis-title">
                    <span className="field-name">标题</span>
                    <EditBox
                        name="title"
                        placeholder="坐标轴标题"
                        value={axis.title}
                        afterChange={this.handleEditBoxChange} />
                </label>
                <label className="field axis-label">
                    <span className="field-name">标签</span>
                    <EditBox
                        name="label"
                        placeholder="待明确"
                        value={axis.label}
                        afterChange={this.handleEditBoxChange} />
                </label>
                <label className="field axis-placement">
                    <span className="field-name">位置</span>
                    <select
                        name="placement"
                        value={axis.placement}
                        onChange={this.handleChange}>
                        <option value={Placement.UNSET}>默认</option>
                        <option value={Placement.TOP}>上</option>
                        <option value={Placement.RIGHT}>右</option>
                        <option value={Placement.BOTTOM}>下</option>
                        <option value={Placement.LEFT}>左</option>
                    </select>
                </label>
                <label className="field axis-reversed">
                    <span className="field-name">反向</span>
                    <input
                        type="checkbox"
                        name="reversed"
                        checked={axis.reversed}
                        onChange={this.handleCheckboxChange} />
                </label>
                <label className="field axis-stacking">
                    <span className="field-name">堆叠</span>
                    <select
                        name="stacking"
                        value={axis.stacking}
                        onChange={this.handleChange}>
                        <option value={Stacking.UNSET}>默认</option>
                        <option value={Stacking.ABSOLUTE}>堆叠</option>
                        <option value={Stacking.PERCENT}>百分比堆叠（暂不支持）</option>
                    </select>
                </label>
                <label className="field axis-interval">
                    <span className="field-name">区间</span>
                    <IntervalInput
                        value={axis.interval}
                        afterChange={this.handleIntervalChange} />
                </label>
                <div className="field axis-bands">
                    <span className="field-name">标示带</span>
                    <EditableList<AxisBand>
                        list={axis.bands}
                        getLabel={this.getBandLabel}
                        createItem={this.createNewBand}
                        editor={BandEditor}
                        afterChange={this.handleBandsChange} />
                </div>
                <label className="field axis-format">
                    <FormatInput
                        format={axis.format}
                        afterChange={this.handleFormatChange} />
                </label>
            </div>
        );
    }
}

export default AxisForm;
export { AxisFormProps };
