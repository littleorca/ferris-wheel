package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.query.*;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.util.DataSet;
import com.ctrip.ferriswheel.common.util.DataSetBuilder;
import com.ctrip.ferriswheel.common.variant.ErrorCodes;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.common.variant.Variant;
import com.ctrip.ferriswheel.common.variant.VariantType;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import com.ctrip.ferriswheel.core.loader.DefaultProviderManager;

import java.io.IOException;

public class TestDefaultTable extends DefaultTableTestSupport {
    private static final String FAKE_QUERY_SCHEME = "test-query";
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        DefaultProviderManager pm = new DefaultProviderManager();
        pm.register(new ProviderMock());
        environment = new DefaultEnvironment.Builder().setProviderManager(pm).build();
    }

    public void testSetAndGet() {
        DefaultWorkbook wb = new DefaultWorkbook(environment);
        DefaultTable table = (DefaultTable) wb.addSheet("s1").addAsset(Table.class, "t1");
        wb.refresh();

        // empty
        assertEquals(0, table.getRowCount());
        assertEquals(0, table.getColumnCount());

        try {
            table.getCell(0, 0);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            table.getCell(1, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        table.addColumns(0, 3);
        table.addRows(0, 3);

        // set 0, 0
        Variant old = table.setCellValue(0, 0, createIntValue(11));
        assertTrue(old.isBlank());

        wb.refresh();

        // update: blank cells won't be trimmed.
        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        // set 1, 1
        old = table.setCellValue(1, 1, createIntValue(2020));
        assertTrue(old.isBlank());

        wb.refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        // set 0, 1
        old = table.setCellValue(0, 1, createIntValue(12));
        assertTrue(old.isBlank());

        wb.refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        // set 1, 0
        old = table.setCellValue(1, 0, createIntValue(21));
        assertTrue(old.isBlank());

        wb.refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        // set 1, 1 again
        old = table.setCellValue(1, 1, createIntValue(22));
        assertEquals(2020, old.intValue());

        wb.refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        // set 1, 2
        old = table.setCellValue(1, 2, createIntValue(23));
        assertTrue(old.isBlank());

        wb.refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        // set 2, 1
        old = table.setCellValue(2, 1, createIntValue(32));
        assertTrue(old.isBlank());

        wb.refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        // set 2, 2
        old = table.setCellValue(2, 2, createIntValue(33));
        assertTrue(old.isBlank());

        wb.refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        // check cells

        checkIntegerGrid(new Integer[][]{
                {11, 12, null},
                {21, 22, 23},
                {null, 32, 33}
        }, table);
    }

    public void testFillUp() {
        DefaultTable table = createTable33();
        table.addColumns(3, 2);
        table.setCellFormula(2, 3, "SUM(A3:C3)");

        table.getWorkbook().refresh();
        System.out.println(table);

        table.fillUp(2, 3, 2);

        table.getWorkbook().refresh();
        System.out.println(table);

        assertEquals("SUM(A1:C1)", table.getCell(0, 3).getFormulaString());
        assertEquals("SUM(A2:C2)", table.getCell(1, 3).getFormulaString());
        assertEquals("SUM(A3:C3)", table.getCell(2, 3).getFormulaString());

        table.setCellFormula(2, 3, "SUM(A3:B3)");
        table.setCellFormula(2, 4, "SUM(B3:C3)");

        table.getWorkbook().refresh();
        System.out.println(table);

        table.fillUp(2, 3, 4, 2);

        table.getWorkbook().refresh();
        System.out.println(table);

        assertEquals("SUM(A1:B1)", table.getCell(0, 3).getFormulaString());
        assertEquals("SUM(B1:C1)", table.getCell(0, 4).getFormulaString());
        assertEquals("SUM(A2:B2)", table.getCell(1, 3).getFormulaString());
        assertEquals("SUM(B2:C2)", table.getCell(1, 4).getFormulaString());
        assertEquals("SUM(A3:B3)", table.getCell(2, 3).getFormulaString());
        assertEquals("SUM(B3:C3)", table.getCell(2, 4).getFormulaString());
    }

    public void testFillRight() {
        DefaultTable table = createTable33();

        table.addRows(3, 2);

        table.setCellFormula(3, 0, "SUM(A1:A3)");

        table.getWorkbook().refresh();
        System.out.println(table);

        table.fillRight(3, 0, 2);

        table.getWorkbook().refresh();
        System.out.println(table);

        assertEquals("SUM(A1:A3)", table.getCell(3, 0).getFormulaString());
        assertEquals("SUM(B1:B3)", table.getCell(3, 1).getFormulaString());
        assertEquals("SUM(C1:C3)", table.getCell(3, 2).getFormulaString());

        table.setCellFormula(3, 0, "SUM(A1:A2)");
        table.setCellFormula(4, 0, "SUM(A2:A3)");

        table.getWorkbook().refresh();
        System.out.println(table);

        table.fillRight(3, 4, 0, 2);

        table.getWorkbook().refresh();
        System.out.println(table);

        assertEquals("SUM(A1:A2)", table.getCell(3, 0).getFormulaString());
        assertEquals("SUM(A2:A3)", table.getCell(4, 0).getFormulaString());
        assertEquals("SUM(B1:B2)", table.getCell(3, 1).getFormulaString());
        assertEquals("SUM(B2:B3)", table.getCell(4, 1).getFormulaString());
        assertEquals("SUM(C1:C2)", table.getCell(3, 2).getFormulaString());
        assertEquals("SUM(C2:C3)", table.getCell(4, 2).getFormulaString());
    }

    public void testFillDown() {
        DefaultTable table = createTable33();
        table.addColumns(3, 2);
        table.setCellFormula(0, 3, "SUM(A1:C1)");

        table.getWorkbook().refresh();
        System.out.println(table);

        table.fillDown(0, 3, 2);

        table.getWorkbook().refresh();
        System.out.println(table);

        assertEquals("SUM(A1:C1)", table.getCell(0, 3).getFormulaString());
        assertEquals("SUM(A2:C2)", table.getCell(1, 3).getFormulaString());
        assertEquals("SUM(A3:C3)", table.getCell(2, 3).getFormulaString());

        table.setCellFormula(0, 3, "SUM(A1:B1)");
        table.setCellFormula(0, 4, "SUM(B1:C1)");

        table.getWorkbook().refresh();
        System.out.println(table);

        table.fillDown(0, 3, 4, 2);

        table.getWorkbook().refresh();
        System.out.println(table);

        assertEquals("SUM(A1:B1)", table.getCell(0, 3).getFormulaString());
        assertEquals("SUM(B1:C1)", table.getCell(0, 4).getFormulaString());
        assertEquals("SUM(A2:B2)", table.getCell(1, 3).getFormulaString());
        assertEquals("SUM(B2:C2)", table.getCell(1, 4).getFormulaString());
        assertEquals("SUM(A3:B3)", table.getCell(2, 3).getFormulaString());
        assertEquals("SUM(B3:C3)", table.getCell(2, 4).getFormulaString());
    }

    public void testFillLeft() {
        DefaultTable table = createTable33();
        table.addRows(3, 2);
        table.setCellFormula(3, 2, "SUM(C1:C3)");

        table.getWorkbook().refresh();
        System.out.println(table);

        table.fillLeft(3, 2, 2);

        table.getWorkbook().refresh();
        System.out.println(table);

        assertEquals("SUM(A1:A3)", table.getCell(3, 0).getFormulaString());
        assertEquals("SUM(B1:B3)", table.getCell(3, 1).getFormulaString());
        assertEquals("SUM(C1:C3)", table.getCell(3, 2).getFormulaString());

        table.setCellFormula(3, 2, "SUM(C1:C2)");
        table.setCellFormula(4, 2, "SUM(C2:C3)");

        table.getWorkbook().refresh();
        System.out.println(table);

        table.fillLeft(3, 4, 2, 2);

        table.getWorkbook().refresh();
        System.out.println(table);

        assertEquals("SUM(A1:A2)", table.getCell(3, 0).getFormulaString());
        assertEquals("SUM(A2:A3)", table.getCell(4, 0).getFormulaString());
        assertEquals("SUM(B1:B2)", table.getCell(3, 1).getFormulaString());
        assertEquals("SUM(B2:B3)", table.getCell(4, 1).getFormulaString());
        assertEquals("SUM(C1:C2)", table.getCell(3, 2).getFormulaString());
        assertEquals("SUM(C2:C3)", table.getCell(4, 2).getFormulaString());
    }

    public void testEraseCells() {
        DefaultTable table = createTable33();
        table.eraseCells(1, 2, 1, 0);
        table.getWorkbook().refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());
        assertEquals(11, table.getCell(0, 0).intValue());
        assertEquals(13, table.getCell(0, 2).intValue());
        assertTrue(table.getCell(1, 0).isBlank());
        assertTrue(table.getCell(1, 2).isBlank());
        assertEquals(31, table.getCell(2, 0).intValue());
        assertEquals(33, table.getCell(2, 2).intValue());

        table.addColumns(3, 2);
        table.setCellValue(2, 4, new Value.StrValue("test"));
        table.getWorkbook().refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(5, table.getColumnCount());

        table.eraseCells(1, 2, 2, 0);
        table.getWorkbook().refresh();
        // update: blank cells won't be trimmed.
        assertEquals(3, table.getRowCount());
        assertEquals(5, table.getColumnCount());
    }

    public void testInsertRows() {
        DefaultTable table = createTable33();
        table.addRows(1, 2);
        table.getWorkbook().refresh();

        assertEquals(5, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        table.setCellValue(1, 1, new Value.DecimalValue(10));
        table.setCellValue(2, 1, new Value.DecimalValue(24));
        table.getWorkbook().refresh();

        assertEquals(11, table.getCell(0, 0).intValue());
        assertEquals(13, table.getCell(0, 2).intValue());
        assertEquals(10, table.getCell(1, 1).intValue());
        assertEquals(24, table.getCell(2, 1).intValue());
        assertEquals(21, table.getCell(3, 0).intValue());
        assertEquals(23, table.getCell(3, 2).intValue());
    }

    public void testRemoveRows() {
        DefaultTable table = createTable33();
        table.removeRows(0, 2);
        table.getWorkbook().refresh();

        assertEquals(1, table.getRowCount());
        assertEquals(3, table.getColumnCount());
        assertEquals(31, table.getCell(0, 0).intValue());
        assertEquals(33, table.getCell(0, 2).intValue());

        table.addColumns(3, 2);
        table.addRows(1, 2);
        table.setCellFormula(2, 4, "SUM(A3:C3)");
        table.getWorkbook().refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(5, table.getColumnCount());

        table.removeRows(2, 1);
        table.getWorkbook().refresh();

        assertEquals(2, table.getRowCount());
        assertEquals(5, table.getColumnCount());
    }

    public void testInsertColumns() {
        DefaultTable table = createTable33();
        table.addColumns(1, 2);
        table.setCellValue(0, 1, new Value.DecimalValue(10));
        table.setCellValue(2, 2, new Value.DecimalValue(24));
        table.getWorkbook().refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(5, table.getColumnCount());
        assertEquals(11, table.getCell(0, 0).intValue());
        assertEquals(10, table.getCell(0, 1).intValue());
        assertTrue(table.getCell(0, 2).isBlank());
        assertEquals(12, table.getCell(0, 3).intValue());
        assertEquals(13, table.getCell(0, 4).intValue());
        assertEquals(31, table.getCell(2, 0).intValue());
        assertTrue(table.getCell(2, 1).isBlank());
        assertEquals(24, table.getCell(2, 2).intValue());
        assertEquals(32, table.getCell(2, 3).intValue());
        assertEquals(33, table.getCell(2, 4).intValue());
    }

    public void testRemoveColumns() {
        DefaultTable table = createTable33();
        table.removeColumns(0, 2);
        table.getWorkbook().refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(1, table.getColumnCount());
        assertEquals(13, table.getCell(0, 0).intValue());
        assertEquals(23, table.getCell(1, 0).intValue());
        assertEquals(33, table.getCell(2, 0).intValue());

        table.addColumns(1, 2);
        table.setCellFormula(1, 2, "SUM(A2:B2)");
        table.getWorkbook().refresh();

        assertEquals(3, table.getColumnCount());

        table.removeColumns(2, 1);
        table.getWorkbook().refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(2, table.getColumnCount()); // update: blank cells won't be trimmed.
    }

    Value createIntValue(int n) {
        return new Value.DecimalValue(n);
    }

    public void testAutomateWithQuery() {
        DefaultWorkbook workbook = new FilingClerk(environment).createWorkbook("test-workbook");
        DefaultSheet sheet = workbook.addSheet("sheet1");
        DefaultTable table = (DefaultTable) sheet.addAsset(Table.class, "table1");

        TableAutomatonInfo.QueryTemplateInfo template = new TableAutomatonInfo.QueryTemplateInfo();
        template.setScheme(FAKE_QUERY_SCHEME);
        table.automate(new TableAutomatonInfo.QueryAutomatonInfo(template));

        workbook.refresh();

        assertEquals("table1", table.getName());
        assertEquals(4, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        assertEquals("foo", table.getCell(0, 0).strValue());
        assertEquals("bar", table.getCell(0, 1).strValue());
        assertEquals("foobar", table.getCell(0, 2).strValue());

        assertEquals(11, table.getCell(1, 0).intValue());
        assertEquals(12, table.getCell(1, 1).intValue());
        assertEquals(13, table.getCell(1, 2).intValue());

        assertEquals(21, table.getCell(2, 0).intValue());
        assertEquals(22, table.getCell(2, 1).intValue());
        assertEquals(23, table.getCell(2, 2).intValue());

        assertEquals(31, table.getCell(3, 0).intValue());
        assertEquals(32, table.getCell(3, 1).intValue());
        assertEquals(33, table.getCell(3, 2).intValue());
    }

    public void testFillTable() {
        DefaultTable table = createTable33();

        table.getWorkbook().refresh();

        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());

        DataSet dataSet = DataSetBuilder.withColumnCount(1)
                .newRecord()
                .set(0, Value.str("foobar"))
                .commit()
                .build();
        table.doFillTable(dataSet);

        table.getWorkbook().refresh();

        assertEquals(1, table.getRowCount());
        assertEquals(1, table.getColumnCount());
    }

    class ProviderMock implements DataProvider {
        @Override
        public boolean acceptsQuery(DataQuery query) {
            if ("test-query".equals(query.getScheme())) {
                return true;
            }
            return false;
        }

        @Override
        public QueryResult execute(DataQuery query, boolean forceRefresh) throws IOException {
            DataSetBuilder dataSetBuilder = DataSetBuilder.metaDataBuilder()
                    .addColumn("foo", VariantType.DECIMAL)
                    .addColumn("bar", VariantType.DECIMAL)
                    .addColumn("foobar", VariantType.DECIMAL)
                    .seal();

            dataSetBuilder.newRecord()
                    .set(0, Value.dec(11))
                    .set(1, Value.dec(12))
                    .set(2, Value.dec(13))
                    .commit();

            dataSetBuilder.newRecord()
                    .set(0, Value.dec(21))
                    .set(1, Value.dec(22))
                    .set(2, Value.dec(23))
                    .commit();

            dataSetBuilder.newRecord()
                    .set(0, Value.dec(31))
                    .set(1, Value.dec(32))
                    .set(2, Value.dec(33))
                    .commit();

            return new ImmutableQueryResult(ErrorCodes.OK, "OK",
                    ImmutableCacheHint.newBuilder().build(), dataSetBuilder.build());
        }
    }
}
