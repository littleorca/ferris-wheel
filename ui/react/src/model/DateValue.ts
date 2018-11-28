import { VariantType } from "./Variant";
import UnionValue from "./UnionValue";
import * as moment from 'moment';

class DateValue extends UnionValue {
    public date: Date;

    constructor(date: Date | number | string, formulaString?: string) {
        super(formulaString);
        if (date instanceof Date) {
            this.date = date;
        } else if (typeof date === 'number') {
            this.date = new Date(date as number);
        } else if (typeof date === 'string') {
            const m = moment(date as string);
            if (!m.isValid()) {
                throw new Error('Invalid date string: ' + date);
            }
            this.date = m.toDate();
        } else {
            throw new Error('Expected date compatible value, specified: ' + date);
        }
    }

    public valueType() {
        return VariantType.DATE;
    }

    public dateValue() {
        return this.date;
    }
}

export default DateValue;
