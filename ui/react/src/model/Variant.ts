enum VariantType {
    ERROR = 'VT_ERROR',
    BLANK = 'VT_BLANK',
    DECIMAL = 'VT_DECIMAL',
    BOOL = 'VT_BOOL',
    DATE = 'VT_DATE',
    STRING = 'VT_STRING',
    LIST = 'VT_LIST',
}

const VariantTypeNames = Object.freeze({
    [VariantType.ERROR]: '错误',
    [VariantType.BLANK]: '空白',
    [VariantType.DECIMAL]: '数值',
    [VariantType.BOOL]: '布尔',
    [VariantType.DATE]: '日期',
    [VariantType.STRING]: '字符串',
    [VariantType.LIST]: '列表',
});

enum ErrorCode {
    OK = 'EC_OK',
    UNKNOWN = 'EC_UNKNOWN',
    ILLEGAL_REF = 'EC_ILLEGAL_REF',
    ILLEGAL_VALUE = 'EC_ILLEGAL_VALUE',
    DIV_0 = 'EC_DIV_0',
};

const ErrorCodeNames = Object.freeze({
    [ErrorCode.OK]: '#OK!',
    [ErrorCode.UNKNOWN]: '#UNKNOWN!',
    [ErrorCode.ILLEGAL_REF]: '#REF!',
    [ErrorCode.ILLEGAL_VALUE]: '#VALUE!',
    [ErrorCode.DIV_0]: "#DIV/0!",
});

interface Variant {
    valueType: () => VariantType;
    isBlank: () => boolean;
    errorValue: () => ErrorCode;
    numberValue: () => number;
    decimalValue: () => string;
    booleanValue: () => boolean;
    dateValue: () => Date;
    strValue: () => string;
    listValue: () => Variant[];
    itemCount: () => number;
    item: (i: number) => Variant;
}

export default Variant;
export { VariantType, VariantTypeNames, ErrorCode, ErrorCodeNames };
