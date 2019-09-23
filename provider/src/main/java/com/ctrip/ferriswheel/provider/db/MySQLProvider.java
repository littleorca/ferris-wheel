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

import com.ctrip.ferriswheel.common.query.DataProvider;
import com.ctrip.ferriswheel.common.query.DataQuery;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLProvider extends DatabaseProviderBase implements DataProvider {
    private static final String SCHEME_PREFIX = "mysql://";
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    public void add(String dataSourceName, String url, String username, String password) {
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setUrl(url);
        poolProperties.setDriverClassName("com.mysql.jdbc.Driver");
        poolProperties.setUsername(username);
        poolProperties.setPassword(password);
        poolProperties.setDefaultReadOnly(true);
        poolProperties.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        poolProperties.setTestOnBorrow(true);
        poolProperties.setValidationQuery("SELECT 1");
        poolProperties.setValidationInterval(15000);
        poolProperties.setTimeBetweenEvictionRunsMillis(15000);
        poolProperties.setMaxActive(100);
        poolProperties.setInitialSize(10);
        poolProperties.setMinEvictableIdleTimeMillis(15000);
        poolProperties.setMinIdle(10);
        poolProperties.setLogAbandoned(true);
        poolProperties.setRemoveAbandoned(true);

        this.add(dataSourceName, new org.apache.tomcat.jdbc.pool.DataSource(poolProperties));
    }

    public void add(String dataSourceName, DataSource dataSource) {
        String scheme = SCHEME_PREFIX + dataSourceName;
        this.dataSourceMap.put(scheme, dataSource);
    }

    @Override
    public boolean acceptsQuery(DataQuery query) {
        return dataSourceMap.containsKey(query.getScheme());
    }

    @Override
    protected Connection getConnection(String scheme) throws SQLException {
        DataSource ds = dataSourceMap.get(scheme);
        if (ds == null) {
            // This should not happen since acceptsQuery will make sure of the existence.
            throw new RuntimeException("Cannot find data source of scheme: " + scheme);
        }
        return ds.getConnection();
    }
}
