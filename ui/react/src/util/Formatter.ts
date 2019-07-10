import UnionValue from "../model/UnionValue";
import { VariantType } from "../model/Variant";
import * as moment from "moment";
import numbro from "numbro";

/**
 * #,##0.00
 * #,##0.00%
 * yyyy-MM-dd
 * yyyy-MM-dd HH:mm
 * yyyy-MM-dd HH:mm:ss
 */

export type FormatType = "decimal" | "percent" | "datetime";

const REG_DECIMAL_OR_PERCENT = /^(#,##)?0(?:\.(0+))?(%)?$/;
const REG_DATETIME = /^yyyy-MM-dd(?:\s(HH:mm)(:ss)?)?$/;

export interface FormatOption {
    type?: FormatType;
}

export interface DecimalFormatOption extends FormatOption {
    decimalPlaces: number;
    useThousandsSeparator: boolean;
}

export interface DateTimeFormatOption extends FormatOption {
    format: string;
}

class Formatter {
    public static readonly DEFAULT_DECIMAL_FORMAT = "#,##0.00";
    public static readonly DEFAULT_PERCENT_FORMAT = "#,##0.00%";
    public static readonly DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static format(
        value: UnionValue,
        format: string | FormatOption
    ): string {
        if (value.isBlank()) {
            return "";
        }
        let formatOption =
            typeof format === "string"
                ? this.parseFormat(format)
                : (format as FormatOption);
        if (typeof formatOption.type === "undefined") {
            if (value.valueType() === VariantType.DATE) {
                formatOption = this.parseFormat(this.DEFAULT_DATETIME_FORMAT);
            }
        }
        if (formatOption.type === "decimal") {
            if (value.valueType() !== VariantType.DECIMAL) {
                return value.toString();
            }
            return this.formatDecimal(
                value.numberValue(),
                formatOption as DecimalFormatOption
            );
        } else if (formatOption.type === "percent") {
            if (value.valueType() !== VariantType.DECIMAL) {
                return value.toString();
            }
            return this.formatPercent(
                value.numberValue(),
                formatOption as DecimalFormatOption
            );
        } else if (formatOption.type === "datetime") {
            if (value.valueType() !== VariantType.DATE) {
                return value.toString();
            }
            return this.formatDateTime(
                value.dateValue(),
                formatOption as DateTimeFormatOption
            );
        } else {
            return value.toString();
        }
    }

    public static parseFormat(format: string): FormatOption {
        let match = REG_DECIMAL_OR_PERCENT.exec(format);
        if (match !== null) {
            const useThousandsSeparator = typeof match[1] !== "undefined";
            const decimalPlaces =
                typeof match[2] !== "undefined" ? match[2].length : 0;
            const type =
                typeof match[3] !== "undefined" ? "percent" : "decimal";
            return {
                type,
                decimalPlaces,
                useThousandsSeparator
            } as DecimalFormatOption;
        }
        match = REG_DATETIME.exec(format);
        if (match !== null) {
            return { type: "datetime", format } as DateTimeFormatOption;
        } else {
            return {} as FormatOption;
        }
    }

    public static createFormat(option: FormatOption): string {
        if (typeof option.type === "undefined") {
            return "";
        } else if (option.type === "decimal" || option.type === "percent") {
            const decOpt = option as DecimalFormatOption;
            let format = "";
            if (decOpt.useThousandsSeparator) {
                format += "#,##";
            }
            format += "0";
            if (decOpt.decimalPlaces > 0) {
                format += ".";
                for (let i = 0; i < decOpt.decimalPlaces; i++) {
                    format += "0";
                }
            }
            if (decOpt.type === "percent") {
                format += "%";
            }
            return format;
        } else if (option.type === "datetime") {
            const dtOpt = option as DateTimeFormatOption;
            return dtOpt.format;
        } else {
            throw new Error("Unsupported format type: " + option.type);
        }
    }

    private static formatDecimal(
        value: number,
        option: DecimalFormatOption
    ): string {
        return numbro(value).format({
            mantissa: option.decimalPlaces,
            thousandSeparated: option.useThousandsSeparator
        });
    }

    private static formatPercent(
        value: number,
        option: DecimalFormatOption
    ): string {
        return numbro(value).format({
            mantissa: option.decimalPlaces,
            thousandSeparated: option.useThousandsSeparator,
            output: "percent"
        });
    }

    private static formatDateTime(
        value: Date,
        option: DateTimeFormatOption
    ): string {
        const fixedFormat = option.format.replace(/y/g, "Y").replace(/d/g, "D");
        return moment(value).format(fixedFormat);
    }
}

export default Formatter;
