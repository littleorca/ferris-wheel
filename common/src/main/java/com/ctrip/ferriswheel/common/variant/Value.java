/*
 * MIT License
 *
 * Copyright (c) 2018 Ctrip.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.ctrip.ferriswheel.common.variant;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;

public abstract class Value implements Variant {
    public static final BlankValue BLANK = new BlankValue();
    public static final BooleanValue TRUE = new BooleanValue(true);
    public static final BooleanValue FALSE = new BooleanValue(false);

    public static ErrorValue err(ErrorCode errorCode) {
        return ErrorValue.valueOf(errorCode);
    }

    public static DecimalValue dec(Number number) {
        if (number instanceof BigDecimal) {
            return dec((BigDecimal) number);
        } else if (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof AtomicInteger) {
            return dec(number.intValue());
        } else if (number instanceof Long
                || number instanceof AtomicLong
                || number instanceof LongAccumulator
                || number instanceof LongAdder) {
            return dec(number.longValue());
        } else if (number instanceof Float) {
            return dec(number.floatValue());
        } else if (number instanceof Double
                || number instanceof DoubleAccumulator
                || number instanceof DoubleAdder) {
            return dec(number.doubleValue());
        } else if (number instanceof BigInteger) {
            return dec(new BigDecimal((BigInteger) number));
        } else {
            // WARN: unsafe convert.
            return dec(number.doubleValue());
        }
    }

    public static DecimalValue dec(int intValue) {
        return dec(new BigDecimal(intValue));
    }

    public static DecimalValue dec(long longValue) {
        return dec(new BigDecimal(longValue));
    }

    public static DecimalValue dec(float floatValue) {
        return dec(new BigDecimal(floatValue));
    }

    public static DecimalValue dec(double doubleValue) {
        return dec(new BigDecimal(doubleValue));
    }

    public static DecimalValue dec(BigDecimal decimalValue) {
        return new DecimalValue(decimalValue);
    }

    public static BooleanValue bool(boolean booleanValue) {
        return booleanValue ? TRUE : FALSE;
    }

    public static DateValue date(double daysFromCommonEra) {
        return new DateValue(daysFromCommonEra);
    }

    public static DateValue date(String dateString) {
        return new DateValue(dateString);
    }

    public static DateValue date(Date dateValue) {
        return new DateValue(dateValue);
    }

    public static DateValue date(Instant instant) {
        return new DateValue(instant);
    }

    public static StrValue str(String strValue) {
        return new StrValue(strValue);
    }

    public static ListValue list(List<Variant> list) {
        return list(list, 1);
    }

    public static ListValue list(List<Variant> list, int columnCount) {
        if (list == null) {
            return null;
        }
        List<Variant> copyList = new ArrayList<>(list.size());
        for (Variant var : list) {
            copyList.add(from(var));
        }
        return new ListValue(copyList, columnCount);
    }

    public static Value from(Variant var) {
        if (var == null) {
            return null;
        } else if (var instanceof Value) {
            return (Value) var;
        }
        switch (var.valueType()) {
            case ERROR:
                return err(var.errorValue());
            case BLANK:
                return BLANK;
            case DECIMAL:
                return dec(var.decimalValue());
            case BOOL:
                return bool(var.booleanValue());
            case DATE:
                return date(var.dateValue());
            case STRING:
                return str(var.strValue());
            case LIST:
                return list(var.listValue());
            default:
                throw new RuntimeException("Unrecognized variant type: " + var.valueType());
        }
    }

    public static VariantType getCompatibleTypeForMath(VariantType type1, VariantType type2) {
        return VariantType.compatible(type1, type2);
    }

    private Value() {
        // prevents user from extending other classes.
    }

    @Override
    public int compareTo(Variant o) {
        if (equals(o)) {
            return 0;
        }
        VariantType compatibleType = Value.getCompatibleTypeForMath(valueType(), o.valueType());
        switch (compatibleType) {
            case BLANK:
                return 0; // BLANK == BLANK

            case BOOL:
                return Boolean.compare(booleanValue(), o.booleanValue());

            case DECIMAL:
                return decimalValue().compareTo(o.decimalValue());

            case DATE:
                return dateValue().compareTo(o.dateValue());

            case STRING:
                if (valueType() == o.valueType()) {
                    return strValue().compareTo(o.strValue());

                } else if (valueType() == VariantType.STRING) {
                    if (o.valueType() == VariantType.BLANK && strValue().isEmpty()) {
                        return 0;
                    } else {
                        return 1; // according MS Excel, string is always greater than non-string primitive value.
                    }

                } else if (o.valueType() == VariantType.STRING) {
                    if (valueType() == VariantType.BLANK && o.strValue().isEmpty()) {
                        return 0;
                    } else {
                        return -1;
                    }

                } else { // consider the MATH_TYPE_MAPPING, this will not happen, or it must be a bug!
                    throw new RuntimeException("There is probably a a bug, please check MATH_TYPE_MAPPING!");
                }

            default:
                throw new UnsupportedOperationException("Type " + valueType() + " and " + o.valueType()
                        + " are not comparable.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value1 = (Value) o;
        return Objects.equals(rawValue(), value1.rawValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), rawValue());
    }

    @Override
    public String toString() {
        return strValue();
    }

    /**
     * Get boolean value which indicates if the value is valid.
     * Invalid value means there are something wrong,
     * e.g.: an invalid formula generates invalid value.
     *
     * @return
     */
    public boolean isValid() {
        return true;
    }

    public boolean isBlank() {
        return false;
    }

    @Override
    public ErrorCode errorValue() {
        return ErrorCodes.OK;
    }

    @Override
    public int intValue() {
        return decimalValue().intValue();
    }

    @Override
    public long longValue() {
        return decimalValue().longValue();
    }

    @Override
    public float floatValue() {
        return decimalValue().floatValue();
    }

    @Override
    public double doubleValue() {
        return decimalValue().doubleValue();
    }

    @Override
    public BigDecimal decimalValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean booleanValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date dateValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String strValue() {
        return String.valueOf(rawValue());
    }

    @Override
    public List<Variant> listValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int itemCount() {
        return 1;
    }

    @Override
    public Variant item(int i) {
        if (i == 0) {
            return this;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public int columnCount() {
        return 1;
    }

    @Override
    public int rowCount() {
        return 1;
    }

    protected abstract Serializable rawValue();

    public DynamicValue dynamic() {
        return new DynamicValue(this);
    }


    public static final class ErrorValue extends Value {
        private static final Map<Integer, ErrorValue> ERROR_VALUES = new ConcurrentHashMap<>();
        private ErrorCode errorCode;

        public static ErrorValue valueOf(ErrorCode errorCode) {
            ErrorValue val = ERROR_VALUES.get(errorCode.getCode());
            if (val == null) {
                val = new ErrorValue(errorCode);
                ErrorValue prev = ERROR_VALUES.putIfAbsent(errorCode.getCode(), val);
                if (prev != null) {
                    val = prev;
                }
            }
            return val;
        }

        private ErrorValue(ErrorCode errorCode) {
            if (errorCode == null) {
                throw new IllegalArgumentException();
            }
            this.errorCode = errorCode;
        }

        @Override
        public VariantType valueType() {
            return VariantType.ERROR;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public ErrorCode errorValue() {
            return errorCode;
        }

        @Override
        public BigDecimal decimalValue() {
            return BigDecimal.valueOf(errorValue().getCode());
        }

        @Override
        public boolean booleanValue() {
            return !BigDecimal.ZERO.equals(decimalValue());
        }

        @Override
        public String strValue() {
            return errorCode.getName();
        }

        @Override
        protected Serializable rawValue() {
            return errorCode;
        }
    }

    /**
     * For blank only value.
     */
    public static final class BlankValue extends Value {
        private BlankValue() {
            // Make Value.BLANK the singleton instance.
        }

        @Override
        public VariantType valueType() {
            return VariantType.BLANK;
        }

        @Override
        public boolean isBlank() {
            return true;
        }

        @Override
        protected Serializable rawValue() {
            return null;
        }

        @Override
        public BigDecimal decimalValue() {
            return BigDecimal.ZERO;
        }

        @Override
        public boolean booleanValue() {
            return Boolean.FALSE;
        }

        @Override
        public Date dateValue() {
            return new Date(DateValue.COMMON_ERA_TIMESTAMP);
        }

        @Override
        public String strValue() {
            return "";
        }
    }

    public static final class DecimalValue extends Value {
        private final BigDecimal decimalValue;
        public static final MathContext mathContext = MathContext.DECIMAL64;

        public DecimalValue(Integer intValue) {
            this(new BigDecimal(intValue));
        }

        public DecimalValue(Long longValue) {
            this(new BigDecimal(longValue));
        }

        public DecimalValue(Float floatValue) {
            this(new BigDecimal(floatValue));
        }

        public DecimalValue(Double doubleValue) {
            this(new BigDecimal(doubleValue));
        }

        public DecimalValue(String strValue) {
            this(new BigDecimal(strValue));
        }

        public DecimalValue(BigDecimal decimalValue) {
            if (decimalValue == null) {
                throw new IllegalArgumentException();
            }
            this.decimalValue = decimalValue;
        }

        @Override
        public VariantType valueType() {
            return VariantType.DECIMAL;
        }

        @Override
        public BigDecimal decimalValue() {
            return decimalValue;
        }

        @Override
        public boolean booleanValue() {
            return !BigDecimal.ZERO.equals(decimalValue);
        }

        /**
         * MS Excel's date starts from 1900-01-01, with a bug, for compatible with Lotus.
         * When you look into date/calendar in the computer world, you'll find a mess.
         * Even JDK 8 has added new classes for represent/manipulate time data.
         * Whatever, forget the chaos, let's make it begin with AD1 (note there is no AD0).
         * <p>
         * When converting decimal to date, treat the decimal value as days after AD1 Jun 1.
         * If there is no decimal part, the converted date will have no <em>time</em> part,
         * else the decimal part will be calculated also and the <em>time</em> part of
         * converted date will be filled.
         *
         * @return
         */
        @Override
        public Date dateValue() {
            BigDecimal milliseconds = decimalValue.multiply(BigDecimal.valueOf(DateValue.DAY_MILLISECONDS), mathContext);
            return new Date(DateValue.COMMON_ERA_TIMESTAMP + milliseconds.longValue());
        }

        @Override
        protected Serializable rawValue() {
            return decimalValue;
        }

        public Variant add(Variant v) {
            if (!isValid()) {
                return this;
            }
            if (!v.isValid()) {
                return v;
            }
            VariantType type = getCompatibleTypeForMath(valueType(), v.valueType());
            switch (type) {
                case BLANK:
                    return this;
                case DECIMAL:
                    return new DecimalValue(decimalValue.add(v.decimalValue(), mathContext));
                case DATE:
                    return new DateValue(new Date(v.dateValue().getTime() + longValue()));
                default:
                    throw new RuntimeException("Illegal operation.");
            }
        }

        public Variant sub(Variant v) {
            if (!isValid()) {
                return this;
            }
            if (!v.isValid()) {
                return v;
            }
            VariantType type = getCompatibleTypeForMath(valueType(), v.valueType());
            switch (type) {
                case DECIMAL:
                    return new DecimalValue(decimalValue.subtract(v.decimalValue(), mathContext));
                case DATE:
                    return new DateValue(new Date(longValue() - v.dateValue().getTime()));
                default:
                    throw new RuntimeException("Illegal operation.");
            }
        }

        public Variant mul(Variant v) {
            if (!isValid()) {
                return this;
            }
            if (!v.isValid()) {
                return v;
            }
            VariantType type = getCompatibleTypeForMath(valueType(), v.valueType());
            switch (type) {
                case DECIMAL:
                    return new DecimalValue(decimalValue.multiply(v.decimalValue(), mathContext));
                default:
                    throw new RuntimeException("Illegal operation.");
            }
        }

        public Variant div(Variant v) {
            if (!isValid()) {
                return this;
            }
            if (!v.isValid()) {
                return v;
            }
            VariantType type = getCompatibleTypeForMath(valueType(), v.valueType());
            switch (type) {
                case DECIMAL:
                    if (BigDecimal.ZERO.equals(v.decimalValue())) {
                        return new ErrorValue(ErrorCodes.DIV);
                    }
                    return new DecimalValue(decimalValue.divide(v.decimalValue(), mathContext));
                default:
                    return new ErrorValue(ErrorCodes.VALUE);
            }
        }

        public Variant pow(Variant v) {
            if (!isValid()) {
                return this;
            }
            if (!v.isValid()) {
                return v;
            }
            VariantType type = getCompatibleTypeForMath(valueType(), v.valueType());
            switch (type) {
                case DECIMAL:
                    if (decimalValue.scale() <= 0) {
                        return new DecimalValue(decimalValue.pow(v.intValue(), mathContext));
                    } else { // FIXME this will loss precision
                        return new DecimalValue(Math.pow(decimalValue.doubleValue(), v.doubleValue()));
                    }
                default:
                    return new ErrorValue(ErrorCodes.VALUE);
            }
        }

    }

    public static final class BooleanValue extends Value {
        private static final String NAME_TRUE = "TRUE";
        private static final String NAME_FALSE = "FALSE";
        private final boolean booleanValue;

        private BooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        @Override
        public VariantType valueType() {
            return VariantType.BOOL;
        }

        @Override
        public boolean booleanValue() {
            return booleanValue;
        }

        @Override
        public BigDecimal decimalValue() {
            return BigDecimal.valueOf(booleanValue ? 1 : 0);
        }

        @Override
        public Date dateValue() {
            return new Date(DateValue.COMMON_ERA_TIMESTAMP + (booleanValue ? DateValue.DAY_MILLISECONDS : 0));
        }

        @Override
        protected Serializable rawValue() {
            return booleanValue;
        }

        @Override
        public String strValue() {
            return booleanValue ? NAME_TRUE : NAME_FALSE;
        }
    }

    public static final class DateValue extends Value {
        static final long DAY_MILLISECONDS = TimeUnit.DAYS.toMillis(1);
        static final long COMMON_ERA_TIMESTAMP = Instant.parse("0001-01-01T00:00:00Z").toEpochMilli();
        private final Instant instant;

        public DateValue(double daysFromCommonEra) {
            this(Instant.ofEpochMilli(COMMON_ERA_TIMESTAMP + (long) (daysFromCommonEra * DAY_MILLISECONDS)));
        }

        public DateValue(String dateString) {
            this(Instant.parse(dateString));
        }

        public DateValue(Date dateValue) {
            if (dateValue == null) {
                throw new IllegalArgumentException();
            }
            this.instant = dateValue.toInstant();
        }

        public DateValue(Instant instant) {
            if (instant == null) {
                throw new IllegalArgumentException();
            }
            this.instant = instant;
        }

        @Override
        public VariantType valueType() {
            return VariantType.DATE;
        }

        @Override
        public BigDecimal decimalValue() {
            return new BigDecimal(instant.toEpochMilli() - COMMON_ERA_TIMESTAMP)
                    .divide(BigDecimal.valueOf(DAY_MILLISECONDS), DecimalValue.mathContext);
        }

        @Override
        public boolean booleanValue() {
            return instant.getNano() % 1000000 != 0 || instant.toEpochMilli() != COMMON_ERA_TIMESTAMP;
        }

        @Override
        public Date dateValue() {
            return Date.from(instant);
        }

        @Override
        public String strValue() {
            return instant.toString();
        }

        @Override
        protected Serializable rawValue() {
            return instant;
        }

        public DateValue add(DateValue dateValue) {
            return add(dateValue.decimalValue());
        }

        public DateValue add(BigDecimal dec) {
            return Value.date(decimalValue().add(dec, DecimalValue.mathContext).doubleValue());
        }

        public DateValue sub(DateValue dateValue) {
            return sub(dateValue.decimalValue());
        }

        public DateValue sub(BigDecimal dec) {
            return Value.date(decimalValue().subtract(dec, DecimalValue.mathContext).doubleValue());
        }

        public DateValue mul(DateValue dateValue) {
            return mul(dateValue.decimalValue());
        }

        public DateValue mul(BigDecimal dec) {
            return Value.date(decimalValue().multiply(dec, DecimalValue.mathContext).doubleValue());
        }

        public DateValue div(DateValue dateValue) {
            return div(dateValue.decimalValue());
        }

        public DateValue div(BigDecimal dec) {
            return Value.date(decimalValue().divide(dec, DecimalValue.mathContext).doubleValue());
        }
    }

    public static final class StrValue extends Value {
        private final String strValue;

        public StrValue(String strValue) {
            if (strValue == null) {
                throw new IllegalArgumentException();
            }
            this.strValue = strValue;
        }

        @Override
        public VariantType valueType() {
            return VariantType.STRING;
        }

        @Override
        public BigDecimal decimalValue() {
            return new BigDecimal(strValue);
        }

        @Override
        public boolean booleanValue() {
            return Boolean.parseBoolean(strValue);
        }

        @Override
        public Date dateValue() {
            return Date.from(Instant.parse(strValue));
        }

        @Override
        public String strValue() {
            return strValue;
        }

        @Override
        protected Serializable rawValue() {
            return strValue();
        }

    }

    public static final class ListValue extends Value {
        private final List<Variant> values;
        private final int columns;

        public ListValue(List<Variant> values) {
            this(values, 1);
        }

        public ListValue(List<Variant> values, int columns) {
            if (values == null) {
                throw new IllegalArgumentException();
            }
            this.values = Collections.unmodifiableList(new ArrayList<>(values));
            this.columns = columns;
        }

        @Override
        public VariantType valueType() {
            return VariantType.LIST;
        }

        @Override
        public List<Variant> listValue() {
            return values;
        }

        @Override
        public int itemCount() {
            return values.size();
        }

        @Override
        public Variant item(int i) {
            return values.get(i);
        }

        @Override
        public int columnCount() {
            return columns;
        }

        @Override
        public int rowCount() {
            return itemCount() / columnCount();
        }

        @Override
        protected Serializable rawValue() {
            return (Serializable) values;
        }

    }
}
