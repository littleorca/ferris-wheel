package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.Sheet;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.chart.DataSeries;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.*;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class TestDefaultWorkbook extends TestCase {
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
    }

    public void testSimpleCase() {
        DefaultWorkbook workbook = new DefaultWorkbook(environment);
        DefaultTable table = (DefaultTable) workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        table.addColumns(0, 3);
        table.addRows(0, 3);
        table.setCellValue(0, 0, new Value.DecimalValue(1));
        table.setCellValue(0, 1, new Value.DecimalValue(2));
        table.setCellFormula(0, 2, "A1+B1");
        table.setCellValue(1, 0, new Value.DecimalValue(3));
        table.setCellValue(1, 1, new Value.DecimalValue(4));
        table.setCellFormula(1, 2, "A2+B2");
        table.setCellFormula(2, 2, "C1+C2");

        assertEquals(1, workbook.getSheetCount());
        table = workbook.getSheet(0).getAsset("table1");
        assertEquals(3, table.getRowCount());
        assertEquals(3, table.getColumnCount());
        assertEquals(1, table.getCell(0, 0).intValue());
        assertEquals(2, table.getCell(0, 1).intValue());
        assertEquals(3, table.getCell(0, 2).intValue());
        assertEquals(3, table.getCell(1, 0).intValue());
        assertEquals(4, table.getCell(1, 1).intValue());
        assertEquals(7, table.getCell(1, 2).intValue());
        assertTrue(table.getCell(2, 0).isBlank());
        assertTrue(table.getCell(2, 1).isBlank());
        assertEquals(10, table.getCell(2, 2).intValue());
    }

    public void testFillDownCells() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultTable table = workbook.getSheet(0).getAsset("table1");

        table.addColumns(1);
        table.setCellFormula(0, 2, "SUM(A1:B1)");
        table.setCellFormula(0, 3, "A1*B1");

        table.fillDown(0, 2, 3, 2);
        System.out.println(table);

        assertEquals(23, table.getCell(0, 2).intValue());
        assertEquals(132, table.getCell(0, 3).intValue());
        assertEquals(43, table.getCell(1, 2).intValue());
        assertEquals(462, table.getCell(1, 3).intValue());
        assertEquals(63, table.getCell(2, 2).intValue());
        assertEquals(992, table.getCell(2, 3).intValue());

        table.addRows(1, 1);
        System.out.println(table);

        assertEquals(23, table.getCell(0, 2).intValue());
        assertEquals(132, table.getCell(0, 3).intValue());
        assertEquals(0, table.getCell(1, 2).intValue());
        assertEquals(0, table.getCell(1, 3).intValue());
        assertEquals(43, table.getCell(2, 2).intValue());
        assertEquals(462, table.getCell(2, 3).intValue());
        assertEquals(63, table.getCell(3, 2).intValue());
        assertEquals(992, table.getCell(3, 3).intValue());
    }

    public void testFillRightCells() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultTable table = workbook.getSheet(0).getAsset("table1");

        table.addRows(3, 2);
        table.setCellFormula(3, 0, "SUM(A1:A3)");
        table.setCellFormula(4, 0, "A1/A2");

        table.fillRight(3, 4, 0, 2);

        assertEquals(63, table.getCell(3, 0).intValue());
        assertEquals(0.5238095238095238, table.getCell(4, 0).doubleValue());
        assertEquals(66, table.getCell(3, 1).intValue());
        assertEquals(0.5454545454545455, table.getCell(4, 1).doubleValue());
        assertEquals(69, table.getCell(3, 2).intValue());
        assertEquals(0.5652173913043478, table.getCell(4, 2).doubleValue());

        table.addColumns(1, 1);

        assertEquals(63, table.getCell(3, 0).intValue());
        assertEquals(0.5238095238095238, table.getCell(4, 0).doubleValue());
        assertEquals(0, table.getCell(3, 1).intValue());
        assertEquals(ErrorCodes.DIV, table.getCell(4, 1).errorValue());
        assertEquals(66, table.getCell(3, 2).intValue());
        assertEquals(0.5454545454545455, table.getCell(4, 2).doubleValue());
        assertEquals(69, table.getCell(3, 3).intValue());
        assertEquals(0.5652173913043478, table.getCell(4, 3).doubleValue());
    }

    public void testCalculationAfterEraseCells() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultTable table = workbook.getSheet(0).getAsset("table1");
        table.addColumns(3, 2);
        table.setCellFormula(0, 3, "SUM(A1:C3)");
        table.setCellFormula(0, 4, "A2+B2");
        System.out.println(table);

        table.eraseCells(1, 4, 2, 0);
        System.out.println(table);

        assertEquals(3, table.getRowCount()); // update: blank cells won't be trimmed.
        assertEquals(5, table.getColumnCount());
        assertEquals("SUM(A1:C3)", table.getCell(0, 3).getFormulaString());
        assertEquals(36, table.getCell(0, 3).intValue());
        assertEquals("A2+B2", table.getCell(0, 4).getFormulaString());

        Variant value = table.getCell(0, 4).getData();
        assertEquals(VariantType.DECIMAL, value.valueType());
        assertEquals(BigDecimal.ZERO, value.decimalValue());

        workbook = createWorkbookWithTable33();
        table = workbook.getSheet(0).getAsset("table1");
        table.addColumns(3, 1);
        table.setCellFormula(0, 3, "SUM(A1:C3)");
        table.setCellFormula(1, 3, "A2+B2");
        System.out.println(table);

        table.eraseCells(0, 2, 2, 1);
        System.out.println(table);

        assertEquals(3, table.getRowCount());
        assertEquals(4, table.getColumnCount());
        assertEquals("SUM(A1:C3)", table.getCell(0, 3).getFormulaString());
        assertEquals(63, table.getCell(0, 3).intValue());
        assertEquals("A2+B2", table.getCell(1, 3).getFormulaString());
        assertEquals(21, table.getCell(1, 3).intValue());
    }

    public void testCalculationAfterInsertRows() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultTable table = workbook.getSheet(0).getAsset("table1");
        table.addColumns(3, 1);
        table.setCellFormula(0, 3, "SUM(A1:C3)");
        table.setCellFormula(1, 3, "SUM(A2:C2)");
        table.setCellFormula(2, 3, "B3+C3");
        table.addRows(1, 2);
        System.out.println(table);

        assertEquals(5, table.getRowCount());
        assertEquals(4, table.getColumnCount());
        assertEquals("SUM(A1:C5)", table.getCell(0, 3).getFormulaString());
        assertEquals(198, table.getCell(0, 3).intValue());
        assertEquals("SUM(A4:C4)", table.getCell(3, 3).getFormulaString());
        assertEquals(66, table.getCell(3, 3).intValue());
        assertEquals("B5+C5", table.getCell(4, 3).getFormulaString());
        assertEquals(65, table.getCell(4, 3).intValue());

        table.setCellValue(1, 0, new Value.DecimalValue(10));
        table.setCellValue(2, 2, new Value.DecimalValue(24));
        System.out.println(table);

        assertEquals(232, table.getCell(0, 3).intValue());
        assertEquals(66, table.getCell(3, 3).intValue());
        assertEquals(65, table.getCell(4, 3).intValue());
    }

    public void testCalculationAfterRemoveRows() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultTable table = workbook.getSheet(0).getAsset("table1");
        table.addColumns(3, 2);
        table.addRows(3, 1);
        table.setCellFormula(0, 3, "SUM(A1:C3)");
        table.setCellFormula(0, 4, "A2+B2");
        table.setCellFormula(3, 3, "SUM(A2:B2)"); // this row will shift up after remove rows
        System.out.println(table);

        table.removeRows(1, 2);
        System.out.println(table);

        assertEquals(2, table.getRowCount());
        assertEquals(5, table.getColumnCount());
        assertEquals("SUM(A1:C1)", table.getCell(0, 3).getFormulaString());
        assertEquals(36, table.getCell(0, 3).intValue());
        assertEquals("#REF!+#REF!", table.getCell(0, 4).getFormulaString());
        assertEquals("SUM(#REF!)", table.getCell(1, 3).getFormulaString());

        Variant value = table.getCell(0, 4).getData();
        assertFalse(value != null && value.isValid());
        value = table.getCell(1, 3).getData();
        assertFalse(value != null && value.isValid());

        table.removeRows(0, 1);
        table.removeRows(0, 1);
        System.out.println(table);
    }

    public void testRemoveRowsWithChartDependOnIt() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultSheet sheet = workbook.getSheet(0);
        DefaultTable table = sheet.getAsset("table1");
        List<DataSeries> series = Arrays.asList(
                new ChartData.SeriesImpl(
                        new DynamicValue("table1!$A2"),
                        null,
                        new DynamicValue("table1!$B$2:$C$2")),
                new ChartData.SeriesImpl(
                        new DynamicValue("table1!$A3"),
                        null,
                        new DynamicValue("table1!$B$3:$C$3"))
        );
        sheet.addAsset(Chart.class, new ChartData("c1", "Line",
                new DynamicValue("\"hello world\""),
                new DynamicValue("table1!$B$1:$C$1"),
                series));
        table.removeRows(0, 3);

        assertEquals(0, table.getRowCount());
        assertEquals(3, table.getColumnCount());
    }

    public void testCalculationAfterInsertCols() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultTable table = workbook.getSheet(0).getAsset("table1");
        table.addColumns(3, 1);
        table.setCellFormula(0, 3, "SUM(A1:C3)");
        table.setCellFormula(1, 3, "SUM(B2:C2)");
        table.setCellFormula(2, 3, "B3+C3");
        table.addColumns(1, 2);
        System.out.println(table);

        assertEquals(3, table.getRowCount());
        assertEquals(6, table.getColumnCount());
        assertEquals("SUM(A1:E3)", table.getCell(0, 5).getFormulaString());
        assertEquals(198, table.getCell(0, 5).intValue());
        assertEquals("SUM(D2:E2)", table.getCell(1, 5).getFormulaString());
        assertEquals(45, table.getCell(1, 5).intValue());
        assertEquals("D3+E3", table.getCell(2, 5).getFormulaString());
        assertEquals(65, table.getCell(2, 5).intValue());

        table.setCellValue(0, 1, new Value.DecimalValue(10));
        table.setCellValue(2, 2, new Value.DecimalValue(24));
        System.out.println(table);

        assertEquals(232, table.getCell(0, 5).intValue());
        assertEquals(45, table.getCell(1, 5).intValue());
        assertEquals(65, table.getCell(2, 5).intValue());
    }

    public void testCalculationAfterRemoveCols() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultTable table = workbook.getSheet(0).getAsset("table1");
        table.addColumns(3, 1);
        table.setCellFormula(0, 3, "SUM(A1:C3)");
        table.setCellFormula(1, 3, "A2+B2");
        System.out.println(table);

        table.removeColumns(1, 2);
        System.out.println(table);

        assertEquals(3, table.getRowCount());
        assertEquals(2, table.getColumnCount());
        assertEquals("SUM(A1:A3)", table.getCell(0, 1).getFormulaString());
        assertEquals(63, table.getCell(0, 1).intValue());
        assertEquals("A2+#REF!", table.getCell(1, 1).getFormulaString());

        Variant value = table.getCell(1, 1).getData();
        assertFalse(value != null && value.isValid());
    }

    public void testRemoveColumnsWithChartDependOnIt() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultSheet sheet = workbook.getSheet(0);
        DefaultTable table = sheet.getAsset("table1");
        List<DataSeries> series = Arrays.asList(
                new ChartData.SeriesImpl(
                        new DynamicValue("table1!$A2"),
                        null,
                        new DynamicValue("table1!$B$2:$C$2")),
                new ChartData.SeriesImpl(
                        new DynamicValue("table1!$A3"),
                        null,
                        new DynamicValue("table1!$B$3:$C$3"))
        );
        sheet.addAsset(Chart.class, new ChartData("c1", "Line",
                new DynamicValue("\"hello world\""),
                new DynamicValue("table1!$B$1:$C$1"),
                series));
        table.removeColumns(0, 3);

        assertEquals(3, table.getRowCount()); // update: blank cells won't be trimmed.
        assertEquals(0, table.getColumnCount());
    }

    public void testRenameSheetNameAndTableName() {
        DefaultWorkbook workbook = createWorkbookWithTable33();
        DefaultSheet sheet1 = workbook.getSheet(0);
        DefaultSheet sheet2 = workbook.addSheet("sheet2");
        DefaultTable table1 = sheet1.getAsset("table1");
        table1.addColumns(1);
        DefaultTable table2 = (DefaultTable) sheet2.addAsset(Table.class, "table2");
        table2.addColumns(3);
        table2.addRows(3);
        table1.setCellFormula(0, 3, "SUM(A1:C3)");
        table1.setCellFormula(1, 3, "sheet2!table2!A1*3");
        table2.setCellFormula(0, 0, "SUM(sheet1!table1!A1:C3)");
        table2.setCellFormula(0, 1, "A1*2");
        table2.setCellValue(1, 1, new Value.StrValue("foo"));
        table2.setCellValue(1, 2, new Value.StrValue("bar"));
        table2.setCellValue(2, 0, new Value.StrValue("series1"));
        table2.setCellValue(2, 1, new Value.DecimalValue(2000));
        table2.setCellValue(2, 2, new Value.DecimalValue(3000));

        sheet1.addAsset(Chart.class, new ChartData("chart1-1", "Line",
                new DynamicValue("\"Chart 1-1\""),
                new DynamicValue("table1!B1:C1"),
                Arrays.asList(
                        new ChartData.SeriesImpl(
                                new DynamicValue("table1!A2"),
                                null,
                                new DynamicValue("table1!B2:C2"))
                )));
        sheet1.addAsset(Chart.class, new ChartData("chart1-2", "Line",
                new DynamicValue("\"Chart 1-2\""),
                new DynamicValue("sheet2!table2!B2:C2"),
                Arrays.asList(
                        new ChartData.SeriesImpl(
                                new DynamicValue("sheet2!table2!A3"),
                                null,
                                new DynamicValue("sheet2!table2!B3:C3"))
                )));

        sheet2.addAsset(Chart.class, new ChartData("chart2-1", "Line",
                new DynamicValue("\"Chart 2-1\""),
                new DynamicValue("sheet1!table1!B1:C1"),
                Arrays.asList(
                        new ChartData.SeriesImpl(
                                new DynamicValue("sheet1!table1!A2"),
                                null,
                                new DynamicValue("sheet1!table1!B2:C2"))
                )));
        sheet2.addAsset(Chart.class, new ChartData("chart2-2", "Line",
                new DynamicValue("\"Chart 2-2\""),
                new DynamicValue("table2!B2:C2"),
                Arrays.asList(
                        new ChartData.SeriesImpl(
                                new DynamicValue("table2!A3"),
                                null,
                                new DynamicValue("table2!B3:C3"))
                )));

        workbook.renameSheet("sheet1", "s1");
        sheet2.renameAsset("table2", "t2");

        assertEquals("SUM(A1:C3)", table1.getCell(0, 3).getFormulaString());
        assertEquals(198, table1.getCell(0, 3).intValue());
        assertEquals("sheet2!t2!A1*3", table1.getCell(1, 3).getFormulaString());
        assertEquals(594, table1.getCell(1, 3).intValue());

        assertEquals("SUM(s1!table1!A1:C3)", table2.getCell(0, 0).getFormulaString());
        assertEquals(198, table2.getCell(0, 0).intValue());
        assertEquals("A1*2", table2.getCell(0, 1).getFormulaString());
        assertEquals(396, table2.getCell(0, 1).intValue());

        DefaultChart chart = sheet1.getAsset("chart1-1");
        assertEquals("Line", chart.getType());
        assertEquals("\"Chart 1-1\"", chart.getTitle().getFormulaString());
        assertEquals("Chart 1-1", chart.getTitle().strValue());
        assertEquals("table1!B1:C1", chart.getCategories().getFormulaString());
        assertEquals(2, chart.getCategories().itemCount());
        assertEquals("12", chart.getCategories().item(0).strValue());
        assertEquals("13", chart.getCategories().item(1).strValue());
        assertEquals(1, chart.getSeriesCount());
        DefaultDataSeries series = chart.getSeries(0);
        assertEquals("table1!A2", series.getName().getFormulaString());
        assertEquals("21", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals("table1!B2:C2", series.getyValues().getFormulaString());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(22, series.getyValues().item(0).intValue());
        assertEquals(23, series.getyValues().item(1).intValue());

        chart = sheet1.getAsset("chart1-2");
        assertEquals("Line", chart.getType());
        assertEquals("\"Chart 1-2\"", chart.getTitle().getFormulaString());
        assertEquals("Chart 1-2", chart.getTitle().strValue());
        assertEquals("sheet2!t2!B2:C2", chart.getCategories().getFormulaString());
        assertEquals(2, chart.getCategories().itemCount());
        assertEquals("foo", chart.getCategories().item(0).strValue());
        assertEquals("bar", chart.getCategories().item(1).strValue());
        assertEquals(1, chart.getSeriesCount());
        series = chart.getSeries(0);
        assertEquals("sheet2!t2!A3", series.getName().getFormulaString());
        assertEquals("series1", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals("sheet2!t2!B3:C3", series.getyValues().getFormulaString());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(2000, series.getyValues().item(0).intValue());
        assertEquals(3000, series.getyValues().item(1).intValue());

        chart = sheet2.getAsset("chart2-1");
        assertEquals("Line", chart.getType());
        assertEquals("\"Chart 2-1\"", chart.getTitle().getFormulaString());
        assertEquals("Chart 2-1", chart.getTitle().strValue());
        assertEquals("s1!table1!B1:C1", chart.getCategories().getFormulaString());
        assertEquals(2, chart.getCategories().itemCount());
        assertEquals("12", chart.getCategories().item(0).strValue());
        assertEquals("13", chart.getCategories().item(1).strValue());
        assertEquals(1, chart.getSeriesCount());
        series = chart.getSeries(0);
        assertEquals("s1!table1!A2", series.getName().getFormulaString());
        assertEquals("21", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals("s1!table1!B2:C2", series.getyValues().getFormulaString());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(22, series.getyValues().item(0).intValue());
        assertEquals(23, series.getyValues().item(1).intValue());

        chart = sheet2.getAsset("chart2-2");
        assertEquals("Line", chart.getType());
        assertEquals("\"Chart 2-2\"", chart.getTitle().getFormulaString());
        assertEquals("Chart 2-2", chart.getTitle().strValue());
        assertEquals("t2!B2:C2", chart.getCategories().getFormulaString());
        assertEquals(2, chart.getCategories().itemCount());
        assertEquals("foo", chart.getCategories().item(0).strValue());
        assertEquals("bar", chart.getCategories().item(1).strValue());
        assertEquals(1, chart.getSeriesCount());
        series = chart.getSeries(0);
        assertEquals("t2!A3", series.getName().getFormulaString());
        assertEquals("series1", series.getName().strValue());
        assertNull(series.getxValues());
        assertEquals("t2!B3:C3", series.getyValues().getFormulaString());
        assertEquals(2, series.getyValues().itemCount());
        assertEquals(2000, series.getyValues().item(0).intValue());
        assertEquals(3000, series.getyValues().item(1).intValue());
    }

    public void testBatch() {
        DefaultWorkbook workbook = new DefaultWorkbook(environment);
        workbook.batch((wb) -> {
            Sheet s1 = wb.addSheet("sheet1");
            Table t1 = s1.addAsset(Table.class, "table1");
            t1.addRows(1);
            t1.addColumns(1);
            // refer to table2 which not exists yet.
            t1.setCellFormula(0, 0, "table2!A1^2");
            Table t2 = s1.addAsset(Table.class, "table2");
            t2.addColumns(1);
            t2.addRows(1);
            t2.setCellValue(0, 0, Value.dec(8));
        }, true);
        assertEquals(1, workbook.getSheetCount());
        Sheet s1 = workbook.getSheet(0);
        assertEquals(2, s1.getAssetCount());
        Table t1 = s1.getAsset("table1");
        assertEquals(1, t1.getRowCount());
        assertEquals(1, t1.getColumnCount());
        assertEquals(64, t1.getCell(0, 0).getData().intValue());
        assertEquals("table2!A1^2", t1.getCell(0, 0).getData().getFormulaString());
        Table t2 = s1.getAsset("table2");
        assertEquals(1, t2.getRowCount());
        assertEquals(1, t2.getColumnCount());
        assertEquals(8, t2.getCell(0, 0).getData().intValue());
    }

    private DefaultWorkbook createWorkbookWithTable33() {
        DefaultWorkbook workbook = new DefaultWorkbook(environment);
        DefaultTable table = (DefaultTable) workbook.addSheet("sheet1").addAsset(Table.class, "table1");
        table.addRows(0, 3);
        table.addColumns(0, 3);
        table.setCellValue(0, 0, new Value.DecimalValue(11));
        table.setCellValue(0, 1, new Value.DecimalValue(12));
        table.setCellValue(0, 2, new Value.DecimalValue(13));
        table.setCellValue(1, 0, new Value.DecimalValue(21));
        table.setCellValue(1, 1, new Value.DecimalValue(22));
        table.setCellValue(1, 2, new Value.DecimalValue(23));
        table.setCellValue(2, 0, new Value.DecimalValue(31));
        table.setCellValue(2, 1, new Value.DecimalValue(32));
        table.setCellValue(2, 2, new Value.DecimalValue(33));
        return workbook;
    }

}
