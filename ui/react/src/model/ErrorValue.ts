import { ErrorCode, VariantType } from "./Variant";
import UnionValue from "./UnionValue";

class ErrorValue extends UnionValue {
    public error: ErrorCode;

    constructor(errorCode: ErrorCode, formulaString?: string) {
        super(formulaString);
        this.error = errorCode;
    }

    public valueType() {
        return VariantType.ERROR;
    }

    public errorValue() {
        return this.error;
    }
}

export default ErrorValue;
