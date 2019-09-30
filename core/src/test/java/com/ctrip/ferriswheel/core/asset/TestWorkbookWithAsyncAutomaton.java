/*
 * MIT License
 *
 * Copyright (c) 2018-2019 Ctrip.com
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

package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.query.*;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.util.DataSetBuilder;
import com.ctrip.ferriswheel.common.variant.*;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo.QueryAutomatonInfo;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo.QueryTemplateInfo;
import com.ctrip.ferriswheel.core.loader.DefaultProviderManager;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.function.Function;

/**
 * @author liuhaifeng
 */
public class TestWorkbookWithAsyncAutomaton extends TestCase {
    private DefaultWorkbook workbook;
    private DefaultTable t1, t2, t3, t4, t5;
    private FakeProvider provider;
    private Environment environment;

    /**
     * Calculate graph:
     * <pre>
     *    t2 -- t4
     *   /     /
     * t1 -- t3 -- t5
     * </pre>
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        provider = new FakeProvider();
        DefaultProviderManager pm = new DefaultProviderManager();
        pm.register(provider);
        environment = new DefaultEnvironment.Builder().setProviderManager(pm).build();
        workbook = new FilingClerk(environment).createWorkbook("test");
        DefaultSheet s1 = workbook.addSheet("s1");
        t1 = (DefaultTable) s1.addAsset(Table.class, "t1");
        t2 = (DefaultTable) s1.addAsset(Table.class, "t2");
        t3 = (DefaultTable) s1.addAsset(Table.class, "t3");
        t4 = (DefaultTable) s1.addAsset(Table.class, "t4");
        t5 = (DefaultTable) s1.addAsset(Table.class, "t5");

        t1.addRows(1);
        t1.addColumns(1);
        t1.setCellValue(0, 0, Value.dec(0));
        t2.automate(new QueryAutomatonInfo(new QueryTemplateInfo("test-scheme-1",
                Collections.singletonMap("val",
                        new DefaultParameter("val", new DynamicValue("t1!A1+1"))))));
        t3.automate(new QueryAutomatonInfo(new QueryTemplateInfo("test-scheme-2",
                Collections.singletonMap("val",
                        new DefaultParameter("val", new DynamicValue("t1!A1+1"))))));
        t4.automate(new QueryAutomatonInfo(new QueryTemplateInfo("test-scheme-3",
                Collections.singletonMap("val",
                        new DefaultParameter("val", new DynamicValue("t2!A1+t3!A1"))))));
        t5.addRows(1);
        t5.addColumns(1);
        t5.setCellFormula(0, 0, "t3!A1+1");

        workbook.refresh();
        System.out.println("## Initial workbook:");
        System.out.println(workbook);
    }

    public void testNormalCase() {
        t1.setCellValue(0, 0, Value.dec(1));
        workbook.refresh();
        assertEquals(2, t2.getCell(0, 0).intValue());
        assertEquals(2, t3.getCell(0, 0).intValue());
        assertEquals(4, t4.getCell(0, 0).intValue());
        assertEquals(3, t5.getCell(0, 0).intValue());
    }

    public void testException() {
        provider.beforeExecute = (q) -> {
            if ("test-scheme-1".equals(q.getScheme())) {
                throw new RuntimeException("test"); // t2
            }
            return null;
        };
        t1.setCellValue(0, 0, Value.dec(2));
        workbook.refresh();
        assertEquals(2, t1.getCell(0, 0).intValue());
//        assertEquals(3, t3.getCell(0, 0).intValue());

        provider.beforeExecute = (q) -> {
            if ("test-scheme-3".equals(q.getScheme())) {
                throw new RuntimeException("test"); // t4
            }
            return null;
        };
        workbook.refresh(true);
        assertEquals(2, t1.getCell(0, 0).intValue());
        assertEquals(3, t2.getCell(0, 0).intValue());
        assertEquals(3, t3.getCell(0, 0).intValue());
        assertEquals(4, t5.getCell(0, 0).intValue());

        provider.beforeExecute = null;
        workbook.refresh(true);
        assertEquals(2, t1.getCell(0, 0).intValue());
        assertEquals(3, t2.getCell(0, 0).intValue());
        assertEquals(3, t3.getCell(0, 0).intValue());
        assertEquals(6, t4.getCell(0, 0).intValue());
        assertEquals(4, t5.getCell(0, 0).intValue());
    }

    class FakeProvider implements DataProvider {
        Function<DataQuery, Void> beforeExecute;

        @Override
        public boolean acceptsQuery(DataQuery query) {
            return true;
        }

        @Override
        public QueryResult execute(DataQuery query, boolean forceRefresh) {
            if (beforeExecute != null) {
                beforeExecute.apply(query);
            }
            Variant val = query.getParam("val");
            System.out.println("execute: " + query.getScheme() + " => " + val);
            DataSetBuilder dataSetBuilder = DataSetBuilder.withColumnCount(1)
                    .newRecord()
                    .set(0, query.getParam("val"))
                    .commit();
            return new ImmutableQueryResult(ErrorCodes.OK, "Ok",
                    ImmutableCacheHint.newBuilder().maxAge(0).build(), dataSetBuilder.build());
        }
    }

}
