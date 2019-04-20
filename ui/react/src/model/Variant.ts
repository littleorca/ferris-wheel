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
    OK = "EC_UNSET",
    NULL = "EC_NULL"                 /* = 1 */,
    DIV = "EC_DIV"                   /* = 2 */,
    VALUE = "EC_VALUE"               /* = 3 */,
    REF = "EC_REF"                   /* = 4 */,
    NAME = "EC_NAME"                 /* = 5 */,
    NUM = "EC_NUM"                   /* = 6 */,
    NA = "EC_NA"                     /* = 7 */,
    GETTING_DATA = "EC_GETTING_DATA" /* = 8 */
};

const ErrorCodeNames = Object.freeze({
    [ErrorCode.OK]: '#OK!',
    [ErrorCode.NULL]: '#NULL!',
    [ErrorCode.DIV]: "#DIV/0!",
    [ErrorCode.VALUE]: '#VALUE!',
    [ErrorCode.REF]: '#REF!',
    [ErrorCode.NAME]: '#NAME?',
    [ErrorCode.NUM]: '#NUM!',
    [ErrorCode.NA]: '#N/A',
    [ErrorCode.GETTING_DATA]: '#GETTING_DATA',
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
