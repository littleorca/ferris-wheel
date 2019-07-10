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
 */

package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.form.Form;
import com.ctrip.ferriswheel.common.query.DataProvider;
import com.ctrip.ferriswheel.common.query.DataQuery;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.util.ListDataSet;
import com.ctrip.ferriswheel.common.variant.*;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.FormFieldBindingData;
import com.ctrip.ferriswheel.core.bean.FormFieldData;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.loader.DefaultProviderManager;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestDefaultForm extends TestCase {
    private FakeProvider provider;
    private Environment environment;
    private DefaultWorkbook workbook;
    private DefaultSheet sheet;
    private DefaultTable table;
    private DefaultForm form;

    @Override
    protected void setUp() throws Exception {
        provider = new FakeProvider();
        DefaultProviderManager pm = new DefaultProviderManager();
        pm.register(provider);
        environment = new DefaultEnvironment.Builder().setProviderManager(pm).build();
        workbook = new FilingClerk(environment).createWorkbook("workbook");
        sheet = workbook.addSheet("sheet");
        table = (DefaultTable) sheet.addAsset(Table.class, "table");
        table.addColumns(0, 3);
        table.addRows(0, 3);
        form = (DefaultForm) sheet.addAsset(Form.class, "form");
    }

    public void testBindToCell() {
        form.addField(new FormFieldData("foo", VariantType.STRING, Value.str("Foolish"), false, false,
                "Foo", "A foo", null, Arrays.asList(new FormFieldBindingData("table!A1"))));
        form.addField(new FormFieldData("bar", VariantType.DECIMAL, Value.dec(3.14), false, false,
                "Bar", "A bar", null, Arrays.asList(new FormFieldBindingData("table!B1"))));

        Map<String, Variant> params = new HashMap<>();
        params.put("foo", Value.str("Test foo"));
        params.put("bar", Value.dec(new BigDecimal("3.14")));
        form.submit(params);

        assertEquals("Test foo", table.getCell(0, 0).strValue());
        assertEquals(new BigDecimal("3.14"), table.getCell(0, 1).decimalValue());
    }

    public void testBindToCellWhichHasMovedAfter() {
        form.addField(new FormFieldData("foobar", VariantType.STRING, Value.str("Foobar"), false, false,
                "Foo Bar", "Foo and bar.", null, Arrays.asList(new FormFieldBindingData("table!A1"))));
        table.addRows(0, 1);
        table.addColumns(0, 1);

        HashMap<String, Variant> params = new HashMap<>();
        params.put("foobar", Value.str("foo+bar"));
        form.submit(params);

        assertTrue(table.getCell(0, 0).isBlank());
        assertTrue(table.getCell(0, 1).isBlank());
        assertTrue(table.getCell(1, 0).isBlank());
        assertEquals("foo+bar", table.getCell(1, 1).strValue());
    }

    public void testBindToQuery() {
        final String scheme = TestDefaultForm.class.getName();
        HashMap<String, Parameter> builtinParams = new LinkedHashMap<>();
        builtinParams.put("foo", new DefaultParameter("foo", new DynamicValue(Value.str("This is foo!"))));
        builtinParams.put("bar", new DefaultParameter("bar", new DynamicValue(Value.dec(8))));
        table.automate(new TableAutomatonInfo.QueryAutomatonInfo(new TableAutomatonInfo.QueryTemplateInfo(scheme, builtinParams)));

        form.addField(new FormFieldData("foo", VariantType.STRING, Value.str("Foolish"), false, false,
                "Foo", "A foo", null, Arrays.asList(new FormFieldBindingData("table!'foo'"))));
        form.addField(new FormFieldData("bar2", VariantType.DECIMAL, Value.dec(3.14), false, false,
                "Bar", "A bar", null, Arrays.asList(new FormFieldBindingData("table!'bar'"))));

        Map<String, Variant> params = new HashMap<>();
        params.put("foo", Value.str("Test foo"));
        params.put("bar2", Value.dec(new BigDecimal("3.14")));
        form.submit(params);

        assertEquals("Test foo", table.getCell(1, 1).strValue());
        assertEquals(new BigDecimal("3.14"), table.getCell(2, 1).decimalValue());
    }

    class FakeProvider implements DataProvider {
        @Override
        public boolean acceptsQuery(DataQuery query) {
            return query.getScheme().equals(TestDefaultForm.class.getName());
        }

        @Override
        public DataSet execute(DataQuery query) {
            final int rows = query.getParamNames().size() + 1;
            final int cols = 2;

            ListDataSet.Builder dataSetBuilder = ListDataSet.newBuilder()
                    .setColumnCount(2);
            dataSetBuilder.newRecordBuilder()
                    .set(0, Value.str("scheme"))
                    .set(1, Value.str(query.getScheme()))
                    .commit();
            for (String name : query.getParamNames()) {
                dataSetBuilder.newRecordBuilder()
                        .set(0, Value.str(name))
                        .set(1, query.getParam(name))
                        .commit();
            }
            return dataSetBuilder.build();
        }
    }
}
