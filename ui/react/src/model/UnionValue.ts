import Variant, { ErrorCode, VariantType, ErrorCodeNames } from "./Variant";
import * as moment from 'moment';

abstract class UnionValue implements Variant {
    public formulaString?: string;
    public error?: ErrorCode;
    public decimal?: string;
    public boolean?: boolean;
    public date?: Date;
    public string?: string;
    public list?: { items: UnionValue[] };

    constructor(formulaString?: string) {
        this.formulaString = formulaString;
    }

    public isFormula() {
        return typeof this.formulaString === 'string';
    }
    public getFormulaString() {
        return typeof this.formulaString === 'string' ?
            this.formulaString : "";
    }

    public valueType(): VariantType { throw new Error('Illegal state.'); }
    public isBlank() { return this.valueType() === VariantType.BLANK; }
    public errorValue(): ErrorCode { throw new Error('Illegal state.'); }
    public numberValue(): number { throw new Error('Illegal state.'); }
    public decimalValue(): string { throw new Error('Illegal state.'); }
    public booleanValue(): boolean { throw new Error('Illegal state.'); }
    public dateValue(): Date { throw new Error('Illegal state.'); }
    public strValue(): string { throw new Error('Illegal state.'); }
    public listValue(): UnionValue[] { throw new Error('Illegal state.'); }
    public itemCount(): number { return 1; }
    public item(i: number): UnionValue { throw new Error('Illegal state.'); }

    public toString(): string {
        switch (this.valueType()) {
            case VariantType.ERROR:
                const err = this.errorValue();
                if (err === null) {
                    throw new Error();
                }
                return ErrorCodeNames[err];
            case VariantType.BLANK:
                return '';
            case VariantType.DECIMAL:
                return '' + this.decimalValue();
            case VariantType.BOOL:
                return '' + this.booleanValue();
            case VariantType.DATE:
                const date = this.dateValue();
                if (date === null) {
                    throw new Error();
                }
                return moment(date).format("YYYY-MM-DD HH:mm:ss");
            case VariantType.STRING:
                const str = this.strValue();
                if (str === null) {
                    throw new Error();
                }
                return str;
            case VariantType.LIST:
                return '[...]'; // TODO review this
            default:
                throw Error();
        }
    }

}

export default UnionValue;
