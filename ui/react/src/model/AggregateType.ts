enum AggregateType {
    SUMMARY = 'AT_SUMMARY',
    COUNT = 'AT_COUNT',
    AVERAGE = 'AT_AVERAGE',
    MAXIMUM = 'AT_MAXIMUM',
    MINIMUM = 'AT_MINIMUM',
    PRODUCT = 'AT_PRODUCT',
    DECIMAL_ONLY_COUNT = 'AT_DECIMAL_ONLY_COUNT',
    STANDARD_DEVIATION = 'AT_STANDARD_DEVIATION',
    GLOBAL_STANDARD_DEVIATION = 'AT_GLOBAL_STANDARD_DEVIATION',
    VARIANCE = 'AT_VARIANCE',
    GLOBAL_VARIANCE = 'AT_GLOBAL_VARIANCE',
};

const AggregateTypeNames = Object.freeze({
    [AggregateType.SUMMARY]: '求和',
    [AggregateType.COUNT]: '计数',
    [AggregateType.AVERAGE]: '平均值',
    [AggregateType.MAXIMUM]: '最大值',
    [AggregateType.MINIMUM]: '最小值',
    [AggregateType.PRODUCT]: '乘积',
    [AggregateType.DECIMAL_ONLY_COUNT]: '数值计数',
    [AggregateType.STANDARD_DEVIATION]: '标准偏差',
    [AggregateType.GLOBAL_STANDARD_DEVIATION]: '总体标准偏差',
    [AggregateType.VARIANCE]: '方差',
    [AggregateType.GLOBAL_VARIANCE]: '总体方差',
});

/**
 * For convenience of renderring select list.
 */
const AggregateTypeList = Object.freeze([
    AggregateType.SUMMARY,
    AggregateType.COUNT,
    AggregateType.AVERAGE,
    AggregateType.MAXIMUM,
    AggregateType.MINIMUM,
    AggregateType.PRODUCT,
    AggregateType.DECIMAL_ONLY_COUNT,
    AggregateType.STANDARD_DEVIATION,
    AggregateType.GLOBAL_STANDARD_DEVIATION,
    AggregateType.VARIANCE,
    AggregateType.GLOBAL_VARIANCE,
]);

export default AggregateType;
export { AggregateTypeNames, AggregateTypeList };
