enum AggregateType {
    SUMMARY = 'AT_SUMMARY',
    COUNT = 'AT_COUNT',
    AVERAGE = 'AT_AVERAGE',
    MAXIMUM = 'AT_MAXIMUM',
    MINIMUM = 'AT_MINIMUM',
    PRODUCT = 'AT_PRODUCT',
    DECIMAL_ONLY_COUNT = 'AT_DECIMAL_ONLY_COUNT',
    STANDARD_DEVIATION = 'AT_STANDARD_DEVIATION',
    STANDARD_DEVIATION_POPULATION = 'AT_STANDARD_DEVIATION_POPULATION',
    VARIANCE = 'AT_VARIANCE',
    VARIANCE_POPULATION = 'AT_VARIANCE_POPULATION',
    CUSTOM = 'AT_CUSTOM',
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
    [AggregateType.STANDARD_DEVIATION_POPULATION]: '总体标准偏差',
    [AggregateType.VARIANCE]: '方差',
    [AggregateType.VARIANCE_POPULATION]: '总体方差',
    [AggregateType.CUSTOM]: '自定义公式',
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
    AggregateType.STANDARD_DEVIATION_POPULATION,
    AggregateType.VARIANCE,
    AggregateType.VARIANCE_POPULATION,
    AggregateType.CUSTOM,
]);

export default AggregateType;
export { AggregateTypeNames, AggregateTypeList };
