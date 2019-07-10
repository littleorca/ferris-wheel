package com.ctrip.ferriswheel.core.asset;

import com.ctrip.ferriswheel.common.Environment;
import com.ctrip.ferriswheel.common.SheetAsset;
import com.ctrip.ferriswheel.common.automaton.Automaton;
import com.ctrip.ferriswheel.common.chart.Chart;
import com.ctrip.ferriswheel.common.table.Cell;
import com.ctrip.ferriswheel.common.table.Row;
import com.ctrip.ferriswheel.common.table.Table;
import com.ctrip.ferriswheel.common.variant.DefaultParameter;
import com.ctrip.ferriswheel.common.variant.DynamicValue;
import com.ctrip.ferriswheel.common.variant.Value;
import com.ctrip.ferriswheel.core.bean.ChartData;
import com.ctrip.ferriswheel.core.bean.DefaultEnvironment;
import com.ctrip.ferriswheel.core.bean.TableAutomatonInfo;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Test assets' register and relationship maintenance.
 */
public class TestAssetManagement extends TestCase {
    private Environment environment;

    @Override
    protected void setUp() throws Exception {
        environment = new DefaultEnvironment.Builder().build();
    }

    public void testWorkbookAndSheets() {
        DefaultWorkbook wb = new DefaultWorkbook(environment);
        checkAssetMap(wb);

        wb.addSheet("sheet1");
        checkAssetMap(wb);

        wb.addSheet(0, "sheet0");
        checkAssetMap(wb);

        wb.renameSheet("sheet0", "sheet2");
        checkAssetMap(wb);

        wb.moveSheet("sheet2", 1);
        checkAssetMap(wb);

        wb.removeSheet(0);
        checkAssetMap(wb);

        wb.removeSheet("sheet2");
        checkAssetMap(wb);

        assertEquals(1, wb.getAssetManager().assetMap.size());
    }

    public void testSheetAndTables() {
        DefaultWorkbook wb = new DefaultWorkbook(environment);
        DefaultSheet sheet = wb.addSheet("sheet1");
        checkAssetMap(wb);

        sheet.addAsset(Table.class, "table1");
        checkAssetMap(wb);

        sheet.addAsset(Table.class, "table0");
        checkAssetMap(wb);

        sheet.renameAsset("table0", "table2");
        checkAssetMap(wb);

        sheet.removeAsset("table1");
        checkAssetMap(wb);

        sheet.removeAsset("table2");
        checkAssetMap(wb);

        assertEquals(2, wb.getAssetManager().assetMap.size());
    }

    public void testTableAndRows() {
        DefaultWorkbook wb = new DefaultWorkbook(environment);
        DefaultTable table = (DefaultTable) wb.addSheet("sheet1").addAsset(Table.class, "table1");
        checkAssetMap(wb);

        table.addRows(0, 7);
        checkAssetMap(wb);

        table.addColumns(0, 7);
        checkAssetMap(wb);

        // do something really effects

        table.setCellValue(0, 0, new Value.StrValue("test"));
        checkAssetMap(wb);

        table.setCellValue(1, 1, new Value.StrValue("=A1*2"));
        checkAssetMap(wb);

        table.setCellValue(2, 2, new Value.StrValue("=SUM(A1:A2)"));
        table.setCellValue(3, 3, new Value.StrValue("=SUM(D1:D3)"));
        table.setCellValue(4, 4, new Value.StrValue("=SUM(A1:D4)"));
        checkAssetMap(wb);

        table.setCellValue(5, 5, new Value.StrValue("blah.."));
        table.setCellValue(6, 6, new Value.StrValue("blah.."));
        checkAssetMap(wb);

        table.addRows(4, 2);
        checkAssetMap(wb);

        table.setCellValue(4, 4, new Value.StrValue("new"));
        table.setCellValue(5, 5, new Value.StrValue("new"));
        checkAssetMap(wb);

        table.eraseCells(1, 2, 1, 2);
        checkAssetMap(wb);

        table.removeRows(1, 2);
        checkAssetMap(wb);

        table.addColumns(4, 1);
        checkAssetMap(wb);

        table.setCellValue(4, 4, new Value.StrValue("new"));
        table.setCellValue(5, 5, new Value.StrValue("new"));
        checkAssetMap(wb);

        table.eraseCells(0, 3, 5, 1);
        checkAssetMap(wb);

        table.removeColumns(1, 2);
        checkAssetMap(wb);

        table.setCellValue(2, 2, new Value.DecimalValue(123));
        checkAssetMap(wb);

        table.setCellFormula(3, 3, "C3");
        checkAssetMap(wb);

        table.eraseCells(2, 2, 2, 2);
        checkAssetMap(wb);
    }

    public void testTableAndQueryAutomaton() {
        DefaultWorkbook wb = new DefaultWorkbook(environment);
        DefaultTable table = (DefaultTable) wb.addSheet("sheet1").addAsset(Table.class, "table1");
        checkAssetMap(wb);

        TableAutomatonInfo.QueryTemplateInfo queryTemplateInfo = new TableAutomatonInfo.QueryTemplateInfo();
        queryTemplateInfo.setScheme("test");
        queryTemplateInfo.addBuiltinParam("p1", new DefaultParameter("p1", new DynamicValue("NOW()")));
        try {
            table.automate(new TableAutomatonInfo.QueryAutomatonInfo(queryTemplateInfo, null, null));
        } catch (RuntimeException e) {
            // let's ignore automaton's init error as lack of data provider.
        }

        checkAssetMap(wb);
    }

    public void testSheetAndChart() {
        DefaultWorkbook wb = new DefaultWorkbook(environment);
        DefaultSheet s1 = wb.addSheet("sheet1");
        checkAssetMap(wb);

        DefaultTable t1 = (DefaultTable) s1.addAsset(Table.class, "table1");

        t1.addColumns(0, 4);
        t1.addRows(0, 4);

        t1.setCellValue(0, 1, new Value.StrValue("foo"));
        checkAssetMap(wb);
        t1.setCellValue(0, 2, new Value.StrValue("bar"));
        checkAssetMap(wb);
        t1.setCellValue(1, 0, new Value.StrValue("apple"));
        checkAssetMap(wb);
        t1.setCellValue(1, 1, new Value.DecimalValue("1.0"));
        checkAssetMap(wb);
        t1.setCellValue(1, 2, new Value.DecimalValue("1.2"));
        checkAssetMap(wb);
        t1.setCellValue(2, 0, new Value.StrValue("boy"));
        checkAssetMap(wb);
        t1.setCellValue(2, 1, new Value.DecimalValue("1.3"));
        checkAssetMap(wb);
        t1.setCellValue(2, 2, new Value.DecimalValue("1.1"));
        checkAssetMap(wb);

        DefaultChart chart = (DefaultChart) s1.addAsset(Chart.class, new ChartData("chart1", "Line",
                new DynamicValue("\"Chart Title 1\""),
                new DynamicValue("sheet1!table1!$B$1:$C$1"),
                Arrays.asList(
                        new ChartData.SeriesImpl(
                                new DynamicValue("sheet1!table1!$A$2"),
                                null,
                                new DynamicValue("sheet1!table1!$B$2:$C$2")),
                        new ChartData.SeriesImpl(
                                new DynamicValue("sheet1!table1!$A$3"),
                                null,
                                new DynamicValue("sheet1!table1!$B$3:$C$3"))
                )));
        checkAssetMap(wb);

        chart.addSeries(new DynamicValue("sheet1!table1!$A$4"), null, new DynamicValue("sheet1!table1!$B$4:$C$4"));
        checkAssetMap(wb);

        s1.addAsset(Chart.class, new ChartData("chart2", "Line",
                new DynamicValue("\"Chart Title 2\""),
                new DynamicValue("sheet1!table1!$B$1:$C$1"),
                Arrays.asList(
                        new ChartData.SeriesImpl(
                                new DynamicValue("sheet1!table1!$A$2"),
                                new DynamicValue("sheet1!table1!$B$2:$C$2"),
                                null),
                        new ChartData.SeriesImpl(
                                new DynamicValue("sheet1!table1!$A$3"),
                                new DynamicValue("sheet1!table1!$B$3:$C$3"),
                                null)
                )));
        checkAssetMap(wb);

        // test update chart
        s1.updateChart("chart2", new ChartData("chart2", "Line",
                new DynamicValue("\"Chart New Title 2\""),
                new DynamicValue("sheet1!table1!$B$1:$D$1"),
                Arrays.asList(
                        new ChartData.SeriesImpl(
                                new DynamicValue("sheet1!table1!$A$2"),
                                new DynamicValue("sheet1!table1!$B$2:$D$2"),
                                null),
                        new ChartData.SeriesImpl(
                                new DynamicValue("sheet1!table1!$A$3"),
                                new DynamicValue("sheet1!table1!$B$3:$D$3"),
                                null)
                )));
        checkAssetMap(wb);

        s1.removeAsset("chart-1");
        checkAssetMap(wb);

        s1.removeAsset("chart2");
        checkAssetMap(wb);
    }

    void checkAssetMap(DefaultWorkbook wb) {
        Map<Long, DefaultAssetManager.AssetReference> assetMap = wb.getAssetManager().assetMap;
        HashSet<Long> pendingAssetIds = new HashSet<>(assetMap.keySet());
        long id = wb.getAssetId();
        assertTrue(pendingAssetIds.remove(id));
        assertEquals(1, assetMap.get(id).referenceCount.get());
        for (int i = 0; i < wb.getSheetCount(); i++) {
            DefaultSheet sheet = wb.getSheet(i);
            id = sheet.getAssetId();
            assertEquals(wb, sheet.getWorkbook());
            assertTrue(pendingAssetIds.remove(id));
            assertEquals(1, assetMap.get(id).referenceCount.get());
            checkAssetMap(assetMap, pendingAssetIds, sheet);
        }
        assertTrue(pendingAssetIds.isEmpty());
    }

    void checkAssetMap(Map<Long, DefaultAssetManager.AssetReference> assetMap,
                       Set<Long> pendingAssetIds, DefaultSheet sheet) {
        for (SheetAsset asset : sheet) {
            if (asset == null) {
                continue;
            }
            if (asset instanceof DefaultTable) {
                DefaultTable table = (DefaultTable) asset;
                assertEquals(sheet, table.getSheet());
                assertTrue(pendingAssetIds.remove(table.getAssetId()));
                assertEquals(1, assetMap.get(table.getAssetId()).referenceCount.get());
                checkAssetMap(assetMap, pendingAssetIds, table);
            } else if (asset instanceof DefaultChart) {
                DefaultChart cm = (DefaultChart) asset;
                assertEquals(sheet, cm.getSheet());
                assertTrue(pendingAssetIds.remove(cm.getAssetId()));
                assertEquals(1, assetMap.get(cm.getAssetId()).referenceCount.get());
                checkAssetMap(assetMap, pendingAssetIds, cm);
            }
        }
    }

    void checkAssetMap(Map<Long, DefaultAssetManager.AssetReference> assetMap,
                       Set<Long> pendingAssetIds, DefaultTable table) {
        for (Map.Entry<Integer, Row> rowEntry : table) {
            DefaultRow row = (DefaultRow) rowEntry.getValue();
            assertEquals(table, row.getTable());
            assertTrue(pendingAssetIds.remove(row.getAssetId()));
            assertEquals(1, assetMap.get(row.getAssetId()).referenceCount.get());
            checkAssetMap(assetMap, pendingAssetIds, row);
        }

        Automaton automaton = table.getAutomaton();
        if (automaton != null) {
            assertEquals(table, ((AssetNode) automaton).getParent());
            assertTrue(pendingAssetIds.remove(((AssetNode) automaton).getAssetId()));
            assertEquals(1, assetMap.get(((AssetNode) automaton).getAssetId()).referenceCount.get());
            checkAssetMap(assetMap, pendingAssetIds, automaton);
        }
    }

    void checkAssetMap(Map<Long, DefaultAssetManager.AssetReference> assetMap,
                       Set<Long> pendingAssetIds, Row row) {
        for (Map.Entry<Integer, Cell> cellEntry : row) {
            Cell cell = cellEntry.getValue();
            assertEquals(row, ((DefaultCell) cell).getRow());
            assertTrue(pendingAssetIds.remove(((DefaultCell) cell).getAssetId()));
            assertEquals(1, assetMap.get(((DefaultCell) cell).getAssetId()).referenceCount.get());
        }
    }

    void checkAssetMap(Map<Long, DefaultAssetManager.AssetReference> assetMap,
                       Set<Long> pendingAssetIds, Automaton automaton) {
        if (automaton instanceof DefaultQueryAutomaton) {
            DefaultQueryTemplate template = ((DefaultQueryAutomaton) automaton).getTemplate();
            assertEquals(automaton, template.getParent());
            assertTrue(pendingAssetIds.remove(template.getAssetId()));
            assertEquals(1, assetMap.get(template.getAssetId()).referenceCount.get());

            for (String name : template.getBuiltinParamNames()) {
                ValueNode param = (ValueNode) template.getBuiltinParam(name).getValue();
                assertEquals(template, param.getParent());
                assertTrue(pendingAssetIds.remove(param.getAssetId()));
                assertEquals(1, assetMap.get(param.getAssetId()).referenceCount.get());
            }

            //} else if (automaton instanceof PivotTableAutomaton) {

        } else {
            throw new RuntimeException("Unknown automaton: " + automaton.getClass() + ", probably a bug.");
        }
    }

    void checkAssetMap(Map<Long, DefaultAssetManager.AssetReference> assetMap,
                       Set<Long> pendingAssetIds, DefaultChart chart) {
        assertEquals(chart, chart.getTitle().getParent());
        assertTrue(pendingAssetIds.remove(chart.getTitle().getAssetId()));
        assertEquals(1, assetMap.get(chart.getTitle().getAssetId()).referenceCount.get());
        assertEquals(chart, chart.getCategories().getParent());
        assertTrue(pendingAssetIds.remove(chart.getCategories().getAssetId()));
        assertEquals(1, assetMap.get(chart.getCategories().getAssetId()).referenceCount.get());

        for (int i = 0; i < chart.getSeriesCount(); i++) {
            DefaultDataSeries series = chart.getSeries(i);
            assertEquals(chart, series.getChart());
            assertTrue(pendingAssetIds.remove(series.getAssetId()));
            assertEquals(1, assetMap.get(series.getAssetId()).referenceCount.get());
            checkAssetMap(assetMap, pendingAssetIds, series);
        }
    }

    void checkAssetMap(Map<Long, DefaultAssetManager.AssetReference> assetMap,
                       Set<Long> pendingAssetIds, DefaultDataSeries series) {
        if (series.getName() != null) {
            assertEquals(series, series.getName().getParent());
            assertTrue(pendingAssetIds.remove(series.getName().getAssetId()));
            assertEquals(1, assetMap.get(series.getName().getAssetId()).referenceCount.get());
        }
        if (series.getxValues() != null) {
            assertEquals(series, series.getxValues().getParent());
            assertTrue(pendingAssetIds.remove(series.getxValues().getAssetId()));
            assertEquals(1, assetMap.get(series.getxValues().getAssetId()).referenceCount.get());
        }
        if (series.getyValues() != null) {
            assertEquals(series, series.getyValues().getParent());
            assertTrue(pendingAssetIds.remove(series.getyValues().getAssetId()));
            assertEquals(1, assetMap.get(series.getyValues().getAssetId()).referenceCount.get());
        }
    }
}
