import * as React from "react";
import * as moment from "moment";
import NumberInput from "../ctrl/NumberInput";
import UnionValue from "../model/UnionValue";
import Values from "../model/Values";
import Formatter, {
    FormatType,
    FormatOption,
    DecimalFormatOption,
    DateTimeFormatOption
} from "../util/Formatter";
import classnames from "classnames";
import "./FormatForm.css";

export interface FormatFormProps extends React.ClassAttributes<FormatForm> {
    format: string;
    sample?: UnionValue;
    className?: string;
    style?: React.CSSProperties;
    onChange(format: string): void;
}

class FormatForm extends React.Component<FormatFormProps> {
    constructor(props: FormatFormProps) {
        super(props);

        this.handleChangeType = this.handleChangeType.bind(this);
        this.renderFormatDetail = this.renderFormatDetail.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    public componentDidUpdate(prevProps: FormatFormProps) {
        if (this.props.format !== prevProps.format) {
            this.forceUpdate();
        }
    }

    protected detectFormatType(format: string): FormatType | undefined {
        return Formatter.parseFormat(format).type;
    }

    protected handleChangeType(event: React.ChangeEvent<HTMLSelectElement>) {
        const newVal = event.currentTarget.value;
        let format = this.props.format;
        switch (newVal) {
            case "":
                format = "";
                break;
            case "decimal":
                if (this.detectFormatType(format) !== "decimal") {
                    format = Formatter.DEFAULT_DECIMAL_FORMAT; // default decimal format
                }
                break;
            case "percent":
                if (this.detectFormatType(format) !== "percent") {
                    format = Formatter.DEFAULT_PERCENT_FORMAT; // default percent format
                }
                break;
            case "datetime":
                if (this.detectFormatType(format) !== "datetime") {
                    format = Formatter.DEFAULT_DATETIME_FORMAT; // default datetime format
                }
                break;
        }
        this.handleChange(format);
    }

    protected handleChange(format: string) {
        this.props.onChange(format);
    }

    public render() {
        const className = classnames("format-form", this.props.className);
        const format = this.props.format;
        const option = Formatter.parseFormat(format);
        return (
            <div className={className} style={this.props.style}>
                <select
                    className="format-type"
                    value={option.type || ""}
                    size={4}
                    onChange={this.handleChangeType}
                >
                    <option value="">默认</option>
                    <option value="decimal">数字</option>
                    <option value="percent">百分比</option>
                    <option value="datetime">日期</option>
                </select>
                <div className="format-option">
                    {this.renderFormatDetail(option)}
                </div>
            </div>
        );
    }

    protected renderFormatDetail(option: FormatOption) {
        const sample = this.props.sample;
        switch (option.type) {
            case undefined:
                return (
                    <div className="general-format">
                        <span>使用通用格式</span>
                        {typeof sample !== "undefined" && (
                            <div className="format-sample">
                                {Formatter.format(sample, "")}
                            </div>
                        )}
                    </div>
                );
            case "decimal":
            case "percent":
                return (
                    <DecimalFormat
                        option={option as DecimalFormatOption}
                        sample={sample}
                        onChange={this.handleChange}
                    />
                );
            case "datetime":
                return (
                    <DateTimeFormat
                        option={option as DateTimeFormatOption}
                        sample={sample}
                        onChange={this.handleChange}
                    />
                );
            default:
                return null;
        }
    }
}

export default FormatForm;

export interface DecimalFormatProps
    extends React.ClassAttributes<DecimalFormat> {
    option: DecimalFormatOption;
    sample?: UnionValue;
    onChange(format: string): void;
}

export class DecimalFormat extends React.Component<DecimalFormatProps> {
    protected afterChange() {
        this.props.onChange(Formatter.createFormat(this.props.option));
    }

    public render() {
        const option = this.props.option;

        const sampleValue =
            typeof this.props.sample !== "undefined"
                ? this.props.sample
                : Values.dec("1234.5678");
        const sampleText = Formatter.format(sampleValue, option);

        const changeDecimalPlaces = (value: number) => {
            this.props.option.decimalPlaces = value;
            this.afterChange();
        };

        const changeThousandSeparator = (
            e: React.ChangeEvent<HTMLInputElement>
        ) => {
            const value = e.currentTarget.checked;
            this.props.option.useThousandsSeparator = value;
            this.afterChange();
        };

        return (
            <div className="decimal-format-option">
                <label>
                    <span>样例</span>
                    <div className="format-sample">{sampleText}</div>
                </label>
                <label>
                    <span>小数位</span>
                    <NumberInput
                        value={option.decimalPlaces}
                        min={0}
                        max={15}
                        afterChange={changeDecimalPlaces}
                    />
                </label>
                <label>
                    <span>使用千分位</span>
                    <input
                        type="checkbox"
                        checked={option.useThousandsSeparator}
                        onChange={changeThousandSeparator}
                    />
                </label>
            </div>
        );
    }
}

export interface DateTimeFormatProps
    extends React.ClassAttributes<DateTimeFormat> {
    option: DateTimeFormatOption;
    sample?: UnionValue;
    onChange(format: string): void;
}

export class DateTimeFormat extends React.Component<DateTimeFormatProps> {
    constructor(props: DateTimeFormatProps) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
    }

    protected handleChange(e: React.ChangeEvent<HTMLSelectElement>) {
        const format = e.currentTarget.value;
        this.props.onChange(format);
    }

    public render() {
        const format = this.props.option.format;
        const date = new Date();
        const sampleValue =
            typeof this.props.sample !== "undefined"
                ? this.props.sample
                : Values.date(date);
        const sampleText = Formatter.format(sampleValue, format);

        return (
            <div className="datetime-format-option">
                <label>
                    <span>样例</span>
                    <div className="format-sample">{sampleText}</div>
                </label>
                <select size={3} value={format} onChange={this.handleChange}>
                    <option value="yyyy-MM-dd">
                        {moment(date).format("YYYY-MM-DD")}
                    </option>
                    <option value="yyyy-MM-dd HH:mm">
                        {moment(date).format("YYYY-MM-DD HH:mm")}
                    </option>
                    <option value="yyyy-MM-dd HH:mm:ss">
                        {moment(date).format("YYYY-MM-DD HH:mm:ss")}
                    </option>
                </select>
            </div>
        );
    }
}
