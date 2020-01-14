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

package com.ctrip.ferriswheel.provider.db;

import com.ctrip.ferriswheel.common.query.*;
import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.util.DataSetBuilder;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import com.ctrip.ferriswheel.provider.DataProviderSupport;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/**
 * @author liuhaifeng
 */
public abstract class DatabaseProviderBase extends DataProviderSupport implements DataProvider {
    private static final String PARAM_SQL = "sql";

    protected abstract Connection getConnection(String scheme) throws SQLException;

    @Override
    protected QueryResult doExecute(DataQuery query) throws IOException {
        String sql = query.getString(PARAM_SQL);
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("SQL is empty.");
        }
        SQLParser parser = new SQLParser();
        parser.parse(sql);
        sql = parser.getJdbcSql();
        List<String> params = parser.getParamNames();
        if (sql == null || sql.isEmpty()) {
            throw new IllegalArgumentException("Invalid SQL");
        }
        if (!query.getParamNames().containsAll(params)) {
            params.removeAll(query.getParamNames());
            StringBuilder sb = new StringBuilder("Missing parameter(s): ");
            params.forEach((s) -> sb.append(s).append(", "));
            sb.append("please specify all the parameters");
            throw new IllegalArgumentException(sb.toString());
        }

        Connection conn;
        try {
            conn = getConnection(query.getScheme());
        } catch (SQLException e) {
            throw new IOException(e);
        }
        if (conn == null) {
            throw new IOException("Cannot get connection for scheme: " + query.getScheme());
        }
        try {
            if (!conn.isReadOnly()) {
                conn.setReadOnly(true);
            }
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                Variant p = query.getParam(params.get(i));
                switch (p.valueType()) {
                    case BLANK:
                        stmt.setNull(i + 1, Types.VARCHAR); // FIXME maybe forbid BLANK value!
                        break;
                    case DECIMAL:
                        stmt.setBigDecimal(i + 1, p.decimalValue());
                        break;
                    case BOOL:
                        stmt.setBoolean(i + 1, p.booleanValue());
                        break;
                    case DATE:
                        stmt.setTimestamp(i + 1, new java.sql.Timestamp(p.dateValue().getTime()));
                        break;
                    case STRING:
                        stmt.setString(i + 1, p.strValue());
                        break;
                    case LIST:
                    case ERROR:
                    default:
                        throw new RuntimeException("Unsupported SQL parameter type!");
                }
            }
            ResultSet rs = stmt.executeQuery();
            DataSet dataSet = resultSetToDataSet(rs);
            rs.close();
            stmt.close();
            // FIXME cache control
            CacheHint cacheHint = createCacheHint(query);
            return new ImmutableQueryResult(ErrorCodes.OK, "Ok", cacheHint, dataSet);

        } catch (SQLException e) {
            throw new IOException(e);

        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }

    private DataSet resultSetToDataSet(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        DataSetBuilder.MetaDataBuilder metaDataBuilder = DataSetBuilder.metaDataBuilder();
//        DefaultColumnMeta[] columnMetas = new DefaultColumnMeta[md.getColumnCount()];
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String label = md.getColumnLabel(i);
//            columnMetas[i - 1] = new DefaultColumnMeta(label, mapType(md.getColumnType(i)));
            metaDataBuilder.addColumn(label, mapType(md.getColumnType(i)));
        }
        DataSetBuilder dataSetBuilder = metaDataBuilder.seal();
//        DefaultDataSetMeta meta = new DefaultDataSetMeta(false, columnMetas);
//        List<Variant[]> rows = new ArrayList<>();
        while (rs.next()) {
//            Variant[] row = new Variant[md.getColumnCount()];
            DataSetBuilder.RecordBuilder recordBuilder = dataSetBuilder.newRecord();
            for (int i = 1; i <= md.getColumnCount(); i++) {
//                row[i - 1] = mapValue(rs, i, mapType(md.getColumnType(i)));
                recordBuilder.set(i - 1, mapValue(rs, i, mapType(md.getColumnType(i))));
            }
//            rows.add(row);
            recordBuilder.commit();
        }
//        VariantsTable dataSet = new VariantsTable(meta, rows.toArray(new Variant[rows.size()][]));
        return dataSetBuilder.build();
    }

    private VariantType mapType(int columnType) {
        switch (columnType) {
            case Types.NULL:
                return VariantType.BLANK;
            case Types.TINYINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                return VariantType.DECIMAL;
            case Types.BOOLEAN:
            case Types.BIT:
                return VariantType.BOOL;
            case Types.DATE:
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return VariantType.DATE;
            case Types.CHAR:
            case Types.VARCHAR:
                return VariantType.STRING;
            default:
                //throw new RuntimeException("Unsupported type");
                return VariantType.STRING;
        }
    }

    private Variant mapValue(ResultSet rs, int i, VariantType type) throws SQLException {
        switch (type) {
            case BLANK:
                return Value.BLANK;
            case DECIMAL:
                BigDecimal dec = rs.getBigDecimal(i);
                return dec == null ? Value.BLANK : Value.dec(dec);
            case BOOL:
                boolean bool = rs.getBoolean(i);
                return Value.bool(bool);
            case DATE:
                Timestamp ts = rs.getTimestamp(i);
                return ts == null ? Value.BLANK : Value.date(ts);
            case STRING:
                String str = rs.getString(i);
                return str == null ? Value.BLANK : Value.str(str);
            default:
                // throw new RuntimeException("Unsupported type");
                str = rs.getString(i);
                return str == null ? Value.BLANK : Value.str(str);
        }
    }

    protected CacheHint createCacheHint(DataQuery query) {
        return ImmutableCacheHint.newBuilder().build(); // default cache hint, maxAge=0
    }

}
