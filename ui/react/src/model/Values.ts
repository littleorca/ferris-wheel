import { ErrorCode, ErrorCodeNames, VariantType } from './Variant';
import ErrorValue from './ErrorValue';
import BlankValue from './BlankValue';
import DecimalValue from './DecimalValue';
import BooleanValue from './BooleanValue';
import DateValue from './DateValue';
import StrValue from './StrValue';
import ListValue from './ListValue';
import UnionValue from './UnionValue';

class Values {

    public static errorCodeToName(code: ErrorCode) {
        return ErrorCodeNames[code];
    }

    public static error(code: ErrorCode, formula?: string) {
        return new ErrorValue(code, formula);
    }

    public static blank(formula?: string) {
        return new BlankValue(formula);
    }

    public static dec(dec: number | string, formula?: string) {
        if (typeof dec === 'number') {
            dec = '' + dec;
        }
        return new DecimalValue(dec, formula);
    }

    public static bool(b: boolean, formula?: string) {
        return new BooleanValue(b, formula);
    }

    public static date(date: Date | number | string, formula?: string) {
        return new DateValue(date, formula);
    }

    public static str(str: string, formula?: string) {
        return new StrValue(str, formula);
    }

    public static list(items: UnionValue[], formula?: string) {
        return new ListValue(items, formula);
    }

    public static auto(value: any): UnionValue {
        if (value instanceof UnionValue) {
            return value;

        } else if (typeof value === 'number') {
            return this.dec(value as number);

        } else if (typeof value === 'boolean') {
            return this.bool(value);

        } else if (value instanceof Date) {
            return this.date(value);

        } else if (typeof value === 'string') {
            return this.str(value);

        } else if (Array.isArray(value)) {
            const list = [];
            for (const item of value) {
                list.push(this.auto(item));
            }
            return this.list(list);

        } else if (typeof value === 'object' && value !== null) {
            let t = VariantType.BLANK;
            let v;

            if (typeof value.error === 'string') {
                t = VariantType.ERROR;
                v = value.error;

            } else if (typeof value.decimal === 'string' || typeof value.decimal === 'number') {
                t = VariantType.DECIMAL;
                v = value.decimal.toString();

            } else if (typeof value.boolean === 'boolean') {
                t = VariantType.BOOL;
                v = value.boolean;

            } else if (typeof value.date !== "undefined") {
                t = VariantType.DATE;
                v = value.date;

            } else if (typeof value.string === 'string') {
                t = VariantType.STRING;
                v = value.string.toString();

            } else if (typeof value.list !== 'undefined'
                && value.list !== null
                && typeof value.list.items !== 'undefined'
                && Array.isArray(value.list.items)) {
                t = VariantType.LIST;
                v = [];
                for (const item of value.list.items) {
                    v.push(this.auto(item));
                }
            }

            if (typeof value.formulaString === 'string' && value.formulaString !== '') {
                return this.withType(t, v, value.formulaString);
            } else {
                return this.withType(t, v);
            }

        } else {
            throw new Error("Given object cannot be converted to UnionValue: " + value);
        }
    }

    public static withType(type: VariantType, value: any, formula?: string): UnionValue {
        switch (type) {
            case VariantType.ERROR:
                return this.error(value, formula);
            case VariantType.BLANK:
                return this.blank(formula);
            case VariantType.DECIMAL:
                return this.dec(value, formula);
            case VariantType.BOOL:
                return this.bool(value, formula);
            case VariantType.DATE:
                return this.date(value, formula);
            case VariantType.STRING:
                return this.str(value, formula);
            case VariantType.LIST:
                return this.list(value, formula);
            default:
                throw new Error("Failed to create Value object, unknown type: " + type);
        }
    }

    public static formula(formula: string) {
        return new BlankValue(formula);
    }

    public static deserialize(input: any): UnionValue {
        if (typeof input === 'undefined' || input === null) {
            return this.blank();
        }

        const formulaString = (
            typeof input.formulaString === 'string' && input.formulaString !== ""
        ) ? input.formulaString : undefined;

        if (typeof input.error === 'string') {
            return this.error(input.error as ErrorCode, formulaString);
        } else if (typeof input.decimal === 'string') {
            return this.dec(input.decimal, formulaString);
        } else if (typeof input.boolean === 'boolean') {
            return this.bool(input.boolean, formulaString);
        } else if (typeof input.date !== 'undefined') {
            return this.date(input.date, formulaString);
        } else if (typeof input.string === 'string') {
            return this.str(input.string, formulaString);
        } else if (typeof input.list !== 'undefined') {
            const items = [];
            for (const item of input.list.items) {
                items.push(this.auto(item));
            }
            return this.list(items, formulaString);
        } else {
            return this.blank(formulaString);
        }
    }

}

export default Values;
