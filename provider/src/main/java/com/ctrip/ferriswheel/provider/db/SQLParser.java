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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLParser {
    private String sql;
    private String jdbcSql;
    private List<String> paramNames;

    public void parse(String sql) {
        this.sql = sql;
        Matcher m = Pattern.compile("\\#\\{[$a-zA-Z_][a-zA-Z0-9_]*\\}").matcher(sql);
        StringBuilder sb = new StringBuilder();
        int lastPos = 0;
        List<String> paramNames = new ArrayList<>(m.groupCount());
        while (m.find()) {
            if (m.start() == 0) {
                throw new IllegalArgumentException();
            }
            sb.append(sql, lastPos, m.start()).append("?");
            paramNames.add(sql.substring(m.start() + 2, m.end() - 1));
            lastPos = m.end();
        }
        if (lastPos < sql.length()) {
            sb.append(sql, lastPos, sql.length());
        }
        this.jdbcSql = sb.toString();
        this.paramNames = paramNames;
    }

    public String getJdbcSql() {
        return jdbcSql;
    }

    public List<String> getParamNames() {
        return paramNames;
    }
}
