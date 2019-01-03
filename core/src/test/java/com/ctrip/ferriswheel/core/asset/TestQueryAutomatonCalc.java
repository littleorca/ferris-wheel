package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.api.*;
import com.ctrip.ferriswheel.api.query.DataProvider;
import com.ctrip.ferriswheel.api.query.DataQuery;
import com.ctrip.ferriswheel.api.query.DataSet;
import com.ctrip.ferriswheel.api.table.Table;
import com.ctrip.ferriswheel.api.variant.DynamicVariant;
import com.ctrip.ferriswheel.api.variant.VariantRule;
import com.ctrip.ferriswheel.core.bean.DynamicVariantImpl;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.bean.Value;
import com.ctrip.ferriswheel.core.loader.DataSetBuilder;
import com.ctrip.ferriswheel.core.loader.DefaultProviderManager;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestQueryAutomatonCalc extends TestCase {
    private DefaultWorkbook workbook;
    private DefaultTable normalTable;
    private DefaultTable autoTable;
    private FakeProvider provider;
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        provider = new FakeProvider();
        DefaultProviderManager pm = new DefaultProviderManager();
        pm.register(provider);
        environment = new DefaultEnvironment.Builder().setProviderManager(pm).build();
        workbook = new FilingClerk(environment).createWorkbook("test");
        DefaultSheet s1 = workbook.addSheet("s1");
        normalTable = (DefaultTable) s1.addAsset(Table.class, "normal");
        autoTable = (DefaultTable) s1.addAsset(Table.class, "auto");
    }

    public void testCalcThroughQueryAutomaton() {
        HashMap<String, DynamicVariant> builtinParams = new HashMap<>();

        builtinParams.put("p1", Value.str("this is parameter 1").dynamic());
        builtinParams.put("p2", new DynamicVariantImpl("normal!A1"));

        Map<String, VariantRule> userParamRules = Collections.emptyMap();

        normalTable.setCellValue(0, 0, Value.str("Cell A1"));
        normalTable.setCellFormula(0, 1, "auto!A1");
        autoTable.automate(new TableAutomatonInfo.QueryAutomatonInfo(
                new TableAutomatonInfo.QueryTemplateInfo(
                        TestQueryAutomatonCalc.class.getName(),
                        builtinParams,
                        userParamRules),
                null, null));

        System.out.println(workbook);

        assertEquals(1, normalTable.getRowCount());
        assertEquals(2, normalTable.getColumnCount());
        assertEquals("Cell A1", normalTable.getCell(0, 0).strValue());
        assertEquals("scheme", normalTable.getCell(0, 1).strValue());

        assertEquals(3, autoTable.getRowCount());
        assertEquals(2, autoTable.getColumnCount());
        assertEquals("scheme", autoTable.getCell(0, 0).strValue());
        assertEquals(TestQueryAutomatonCalc.class.getName(), autoTable.getCell(0, 1).strValue());
        assertEquals("p1", autoTable.getCell(1, 0).strValue());
        assertEquals("this is parameter 1", autoTable.getCell(1, 1).strValue());
        assertEquals("p2", autoTable.getCell(2, 0).strValue());
        assertEquals("Cell A1", autoTable.getCell(2, 1).strValue());

        // test circular dependencies via automaton
        try {
            // auto->param2->normal!A1->normal!B1->auto!A1
            normalTable.setCellFormula(0, 0, "B1");
            fail("Expects circular dependency exception.");
        } catch (IllegalArgumentException e) {
        }
    }


    class FakeProvider implements DataProvider {
        @Override
        public boolean acceptsQuery(DataQuery query) {
            return query.getScheme().equals(TestQueryAutomatonCalc.class.getName());
        }

        @Override
        public DataSet execute(DataQuery query) {
            final int rows = query.getParamNames().size() + 1;
            final int cols = 2;

            DataSetBuilder dataSetBuilder = new DataSetBuilder()
                    .setColumnCount(2)
                    .setHasRowMeta(false);
            dataSetBuilder.newRecord()
                    .set(0, Value.str("scheme"))
                    .set(1, Value.str(query.getScheme()))
                    .commit();
            for (String name : query.getParamNames()) {
                dataSetBuilder.newRecord()
                        .set(0, Value.str(name))
                        .set(1, query.getParam(name))
                        .commit();
            }
            return dataSetBuilder.build();
        }
    }
}
