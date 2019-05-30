import * as React from 'react';
import AxisBand from '../model/AxisBand';
import ColorInput from '../ctrl/ColorInput';
import Color from '../model/Color';
import IntervalInput from '../ctrl/IntervalInput';
import Interval from '../model/Interval';
import EditBox from '../ctrl/EditBox';
import ValueChange from "../ctrl/ValueChange";

export interface BandFormProps extends React.ClassAttributes<BandForm> {
    band: AxisBand,
    afterChange?: (band: AxisBand) => void,
}

class BandForm extends React.Component<BandFormProps> {
    constructor(props: BandFormProps) {
        super(props);

        this.handleLabelChange = this.handleLabelChange.bind(this);
        this.handleIntervalChange = this.handleIntervalChange.bind(this);
        this.handleColorChange = this.handleColorChange.bind(this);
    }

    protected handleLabelChange(change: ValueChange<string>) {
        if (change.type !== 'commit') {
            return;
        }
        const value = change.toValue;
        const band = this.props.band;
        band.label = value;
        this.forceUpdate();
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(band);
        }
    }

    protected handleIntervalChange(interval: Interval) {
        // since props been changed directly, nothing to do here.
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.band);
        }
    }

    protected handleColorChange(color: Color) {
        const band = this.props.band;
        band.color = color;
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(band);
        }
    }

    public render() {
        const band = this.props.band;
        const color = band.color;

        return (
            <div className="band-form">
                <label className="field band-interval">
                    <span className="field-name">区间</span>
                    <IntervalInput
                        value={band.interval}
                        afterChange={this.handleIntervalChange} />
                </label>
                <label className="field band-label">
                    <span className="field-name">标签</span>
                    <EditBox
                        name="label"
                        placeholder="输入标签"
                        value={band.label}
                        afterChange={this.handleLabelChange} />
                </label>
                <label className="field band-color">
                    <span className="field-name">颜色</span>
                    <ColorInput
                        value={color}
                        disabled={band === null}
                        popoverPlacement="top"
                        afterChange={this.handleColorChange} />
                </label>
            </div>
        );
    }
}

export default BandForm;
