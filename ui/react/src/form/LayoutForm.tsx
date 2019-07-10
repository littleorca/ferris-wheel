import * as React from 'react';
import Layout from '../model/Layout';
import Display from '../model/Display';
import Placement from '../model/Placement';
import NumberInput from '../ctrl/NumberInput';
import IntervalInput from '../ctrl/IntervalInput';
import Interval from '../model/Interval';
import Span from '../model/Span';
import classnames from "classnames";

interface LayoutFormProps extends React.ClassAttributes<LayoutForm> {
    layout: Layout;
    className?: string;
    afterChange?: (layout: Layout) => void;
}

class LayoutForm extends React.Component<LayoutFormProps> {
    constructor(props: LayoutFormProps) {
        super(props);

        this.handleChangeDisplay = this.handleChangeDisplay.bind(this);
        this.handleChangeWidth = this.handleChangeWidth.bind(this);
        this.handleChangeHeight = this.handleChangeHeight.bind(this);
        this.handleChangeAlign = this.handleChangeAlign.bind(this);
        this.handleChangeVerticalAlign = this.handleChangeVerticalAlign.bind(this);
        this.handleChangeColumns = this.handleChangeColumns.bind(this);
        this.handleChangeRows = this.handleChangeRows.bind(this);
        this.handleChangeColumnSpan = this.handleChangeColumnSpan.bind(this);
        this.handleChangeRowSpan = this.handleChangeRowSpan.bind(this);
    }

    protected handleChangeDisplay(event: React.ChangeEvent<HTMLSelectElement>) {
        const target = event.currentTarget;
        this.props.layout.display = target.value as Display;
        this.afterChange();
    }

    protected handleChangeWidth(value: number) {
        this.props.layout.width = value;
        this.afterChange();
    }

    protected handleChangeHeight(value: number) {
        this.props.layout.height = value;
        this.afterChange();
    }

    protected handleChangeAlign(event: React.ChangeEvent<HTMLSelectElement>) {
        const target = event.currentTarget;
        this.props.layout.align = target.value as Placement;
        this.afterChange();
    }

    protected handleChangeVerticalAlign(event: React.ChangeEvent<HTMLSelectElement>) {
        const target = event.currentTarget;
        this.props.layout.verticalAlign = target.value as Placement;
        this.afterChange();
    }

    protected handleChangeColumns(value: number) {
        this.props.layout.grid.columns = value;
        this.afterChange();
    }

    protected handleChangeRows(value: number) {
        this.props.layout.grid.rows = value;
        this.afterChange();
    }

    protected handleChangeColumnSpan(value: Interval) {
        this.props.layout.grid.column = new Span(value.from, value.to);
        this.afterChange();
    }

    protected handleChangeRowSpan(value: Interval) {
        this.props.layout.grid.row = new Span(value.from, value.to);
        this.afterChange();

    }

    protected afterChange() {
        if (typeof this.props.afterChange !== 'undefined') {
            this.props.afterChange(this.props.layout);
        }
        this.forceUpdate();
    }

    public render() {
        const className = classnames("layout-form", this.props.className);
        const layout = this.props.layout;

        return (
            <div className={className}>
                <div className="fieldset common">
                    <h4>常规</h4>
                    <label className="field display">
                        <span className="field-name">Display</span>
                        <select
                            name="display"
                            value={layout.display}
                            onChange={this.handleChangeDisplay}>
                            <option value={Display.UNSET}>未设置</option>
                            <option value={Display.NONE}>不显示</option>
                            <option value={Display.BLOCK}>块状</option>
                            <option value={Display.GRID}>网格</option>
                        </select>
                    </label>
                    <label className="field width">
                        <span className="field-name">Width</span>
                        <NumberInput
                            name="width"
                            value={layout.width}
                            afterChange={this.handleChangeWidth}
                        />
                    </label>
                    <label className="field height">
                        <span className="field-name">Height</span>
                        <NumberInput
                            name="height"
                            value={layout.height}
                            afterChange={this.handleChangeHeight} />
                    </label>
                    <label className="field align">
                        <span className="field-name">Align</span>
                        <select
                            name="align"
                            value={layout.align}
                            onChange={this.handleChangeAlign}>
                            <option value={Placement.UNSET}>未设置</option>
                            <option value={Placement.LEFT}>左</option>
                            <option value={Placement.CENTER}>中</option>
                            <option value={Placement.RIGHT}>右</option>
                        </select>
                    </label>
                    <label className="field verticalAlign">
                        <span className="field-name">Vertical Align</span>
                        <select
                            name="verticalAlign"
                            value={layout.verticalAlign}
                            onChange={this.handleChangeVerticalAlign}>
                            <option value={Placement.UNSET}>未设置</option>
                            <option value={Placement.TOP}>上</option>
                            <option value={Placement.CENTER}>中</option>
                            <option value={Placement.BOTTOM}>下</option>
                        </select>
                    </label>
                </div>
                <div className="fieldset grid">
                    <h4>网格布局</h4>
                    <label className="field grid-columns">
                        <span className="field-name">Columns</span>
                        <NumberInput
                            name="grid-columns"
                            value={layout.grid.columns}
                            afterChange={this.handleChangeColumns} />
                    </label>
                    <label className="field grid-rows">
                        <span className="field-name">Rows</span>
                        <NumberInput
                            name="grid-rows"
                            value={layout.grid.rows}
                            afterChange={this.handleChangeRows} />
                    </label>
                    <label className="field grid-column">
                        <span className="field-name">Column span</span>
                        <IntervalInput
                            // name="grid-column"
                            value={{
                                from: layout.grid.column.start,
                                to: layout.grid.column.end,
                            }}
                            afterChange={this.handleChangeColumnSpan} />
                    </label>
                    <label className="field grid-row">
                        <span className="field-name">Row span</span>
                        <IntervalInput
                            // name="grid-row"
                            value={{
                                from: layout.grid.row.start,
                                to: layout.grid.row.end,
                            }}
                            afterChange={this.handleChangeRowSpan} />
                    </label>
                </div>
            </div>
        );
    }
}

export default LayoutForm;
export { LayoutFormProps };
